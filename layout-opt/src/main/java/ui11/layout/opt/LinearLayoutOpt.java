package ui11.layout.opt;

import org.jspecify.annotations.Nullable;
import ui11.PeerRequest;
import ui11.Widget;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.multichild.LinearLayout.WeightMarker;

import java.util.*;

import static ui11.layout.multichild.LinearLayout.withWeight;

// Flattens nested LinearLayout into the parent, recursively.
// As of now, this only works if the nested LinearLayout is not provided by a generic resolver,
// since peer requests with default values don't work with generic resolvers.
abstract class LinearLayoutOpt extends Widget {

    protected final LinearLayout linearLayout;

    public LinearLayoutOpt(LinearLayout linearLayout) {
        this.linearLayout = linearLayout;
    }

    @Override
    protected Widget build() {
        System.out.println(getClass().getSimpleName());
        // TODO ha collapsedLLRequest == null, akkor weightokat nem is kéne lekérdezni
        return PeerRequest.requestMultiple(
                linearLayout.items(),
                Set.of(CollapsedLLRequest.INSTANCE, WeightMarker.WeightRequest.INSTANCE),
                this::processResults
        );
    }

    private Widget processResults(Map<PeerRequest<?>, ? extends List<?>> results) {
        List<? extends Widget> items = linearLayout.items();

        @SuppressWarnings("unchecked")
        List<Double> weights = ((List<WeightMarker>) results.get(WeightMarker.WeightRequest.INSTANCE)).
                stream().map(WeightMarker::weight).toList();
        if (weights.stream().allMatch(w -> w == 0.0))
            weights = Collections.nCopies(weights.size(), 1.0);

        List<Widget> newItems = new ArrayList<>();
        List<Double> newWeights = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            CollapsedLLOrNothing collapsedLLOrNothing =
                    (CollapsedLLOrNothing) results.get(CollapsedLLRequest.INSTANCE).get(i);
            System.out.println(i+": "+collapsedLLOrNothing);

            if (collapsedLLOrNothing == NoCollapsedLL.NO_COLLAPSED_LL) {
                newItems.add(withWeight(weights.get(i), items.get(i)));
                newWeights.add(weights.get(i));
            } else {
                CollapsedLL nestedLL = (CollapsedLL) collapsedLLOrNothing;
                if (nestedLL.l().mainAxis() == linearLayout.mainAxis() &&
                        nestedLL.l().gap().equals(linearLayout.gap()) &&
                        nestedLL.l().crossAxisAlignment() == linearLayout.crossAxisAlignment()) {
                    for (int j = 0; j < nestedLL.l().items().size(); j++) {
                        double w = weights.get(i) * nestedLL.weights.get(j) / nestedLL.wSum;
                        newItems.add(withWeight(w, nestedLL.l().items().get(j)));
                        newWeights.add(w);
                    }
                } else {
                    newItems.add(withWeight(weights.get(i), nestedLL.l() /* vagy items.get(i)? */));
                    newWeights.add(weights.get(i));
                }
            }
        }

        LinearLayout newLL = linearLayout.withItems(newItems);
        return finish(newLL, newWeights);
    }

    abstract Widget finish(LinearLayout newLL, List<Double> newWeights);

    static class LLFinder extends LinearLayoutOpt {

        @Inject private CollapsedLLRequest[] collapsedLLRequests;

        public LLFinder(LinearLayout linearLayout) {
            super(linearLayout);
        }

        @Override
        Widget finish(LinearLayout newLL, List<Double> newWeights) {
            Widget w = newLL;
            System.out.println("LLFinder result: "+newLL);
            if (linearLayout.mainAxisAlignment() == LinearLayout.JustifyContent.STRETCH)
                for (CollapsedLLRequest req : collapsedLLRequests)
                    w = req.createResponse(new CollapsedLL(newLL, newWeights), w);
            return w;
        }
    }

    static class LLTransformer extends LinearLayoutOpt {

        private final PeerRequest<Widget> transformedWidgetRequest;

        public LLTransformer(LinearLayout linearLayout, PeerRequest<Widget> transformedWidgetRequest) {
            super(linearLayout);
            this.transformedWidgetRequest = transformedWidgetRequest;
        }

        @Override
        Widget finish(LinearLayout newLL, List<Double> newWeights) {
            return transformedWidgetRequest.createResponse(newLL);
        }
    }

    static class CollapsedLLRequest extends PeerRequest<CollapsedLLOrNothing> {

        static final CollapsedLLRequest INSTANCE = new CollapsedLLRequest();

        private CollapsedLLRequest() {
            super(CollapsedLLOrNothing.class);
        }

        @Override
        protected @Nullable CollapsedLLOrNothing defaultValue() {
            return NoCollapsedLL.NO_COLLAPSED_LL;
        }
    }

    sealed interface CollapsedLLOrNothing {
    }

    /**
     * mainAxisAlignment csak {@link ui11.layout.multichild.LinearLayout.JustifyContent#STRETCH STRETCH} lehet
     */
    private record CollapsedLL(LinearLayout l, List<Double> weights, double wSum) implements CollapsedLLOrNothing {
        CollapsedLL(LinearLayout l, List<Double> weights) {
            this(l, weights, weights.stream().mapToDouble(d -> d).sum());
        }
    }

    private enum NoCollapsedLL implements CollapsedLLOrNothing {NO_COLLAPSED_LL}
}
