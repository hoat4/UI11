package ui11;

import ui11.reflectutil.ReflectionUtil;

import java.util.*;

abstract class IVCombiner<T> {

    abstract void add(T value, Deque<WidgetTree.IVStackItem> origin);

    abstract T finish();

    @SuppressWarnings("unchecked")
    static <T> IVCombiner<T> forType(Class<T> type) {
        if (type == ResolutionRequestCollection.class)
            return (IVCombiner<T>) new ReqCollIVCombiner();
        else
            return new SelectingIVCombiner<>("inherited value "+ReflectionUtil.simpleName(type));
    }

    private static class SelectingIVCombiner<T> extends IVCombiner<T> {

        private final Map<T, List<IVOrigin>> differentValues = new IdentityHashMap<>();
        private final String typeDebugName;

        SelectingIVCombiner(String typeDebugName) {
            this.typeDebugName = typeDebugName;
        }

        @Override
        void add(T value, Deque<WidgetTree.IVStackItem> origin) {
            differentValues.computeIfAbsent(value, __ -> new ArrayList<>()).add(IVOrigin.from(origin));
        }

        @Override
        T finish() {
            assert !differentValues.isEmpty();

            if (differentValues.size() == 1)
                return differentValues.keySet().iterator().next();

            if (differentValues.size() == 2) {
                Object[] arr = differentValues.keySet().toArray(Object[]::new);
                @SuppressWarnings("unchecked")
                T a = (T) arr[0], b = (T) arr[1];
                IVOrigin aOrigin = singleOrigin(a), bOrigin = singleOrigin(b);
                return selectMostRecent(a, aOrigin, b, bOrigin);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(differentValues.size()).append(" different values for ");
            sb.append(typeDebugName).append(":");
            for (Map.Entry<T, List<IVOrigin>> e : differentValues.entrySet()) {
                sb.append("\n- ").append(e.getKey()).append(" ").append(e.getKey().hashCode());
                for (IVOrigin origin : e.getValue())
                    origin.printOrigin(sb);
            }
            throw new RuntimeException(sb.toString());
        }

        private IVOrigin singleOrigin(T t) {
            List<IVOrigin> origins = differentValues.get(t);
            if (origins.size() == 1)
                return origins.getFirst();
            else
                throw new RuntimeException("TODO more than one origin for IV value: " + t);
        }

        private T selectMostRecent(T a, IVOrigin aOrigin, T b, IVOrigin bOrigin) {
            // prefix = gyökérhez közelebbi, suffix = w-hez közelebbi
            int commonSuffix = 0;
            while (commonSuffix < aOrigin.origin().size() && commonSuffix < bOrigin.origin().size() &&
                aOrigin.origin().get(aOrigin.origin().size() - 1 - commonSuffix).parent == bOrigin.origin().get(bOrigin.origin().size() - 1 - commonSuffix).parent)
                commonSuffix++;

            assert commonSuffix > 0; // legalább w-nek egyeznie kell

            long timeA = -77777776666666L; // bármi jó ha <=0, csak azért ilyen hogy debuggerben esetén észre lehessen venni
            for (int i = 0; i < aOrigin.origin().size() - commonSuffix; i++)
                timeA = Math.max(timeA, aOrigin.origin().get(i).edgeCreationTime);
            long timeB = -66666667777777L;
            for (int i = 0; i < bOrigin.origin().size() - commonSuffix; i++)
                timeB = Math.max(timeB, bOrigin.origin().get(i).edgeCreationTime);
            if (timeA == -77777776666666L || timeB == -66666667777777L) {
                StringBuilder sb = new StringBuilder("TODO common suffix consumed whole IV origin stack: ");
                sb.append(timeA).append(", ").append(timeB).append(": ").append(aOrigin.origin);
                sb.append("\nA: ").append(a);
                aOrigin.printOrigin(sb);
                sb.append("\nB: ").append(b);
                bOrigin.printOrigin(sb);
                throw new RuntimeException(sb.toString());
            }

            if (timeA < timeB)
                return b;
            if (timeA > timeB)
                return a;

            // TODO itt List.of és Map.of nem jó, mert a és b lehet null
            throw new RuntimeException("Can't decide between two reqs (times: " + timeA + ", " + timeB + ", " +
                "common suffix: " + commonSuffix + "):" +
                toString(List.of(a, b), Map.of(a, aOrigin, b, bOrigin)));
        }

        private String toString(Collection<T> reqs,
                                Map<T, IVOrigin> times) {
            StringBuilder sb = new StringBuilder();
            for (T req : reqs) {
                sb.append("\n- ").append(req.toString());
                times.get(req).printOrigin(sb);
            }
            return sb.toString();
        }

        private record IVOrigin(List<WidgetTree.IVStackItem> origin) {

            static IVOrigin from(Deque<WidgetTree.IVStackItem> stack) {
                return new IVOrigin(List.copyOf(stack));
            }

            void printOrigin(StringBuilder sb) {
                sb.append("\n  Found at: ");
                int i = origin.size();
                for (WidgetTree.IVStackItem item : origin.reversed()) {
                    sb.append("\n   ").append(--i).append(".: ").append(item.parent).
                        append(" (time: ").append(item.edgeCreationTime).
                        append(", parents: ").append(item.parent.parents.size()).append(")");
                }
            }
        }
    }

    private static class ReqCollIVCombiner extends IVCombiner<ResolutionRequestCollection> {

        private final Set<ResolutionRequest<?>> combined = new HashSet<>();
        private final Map<Class<? extends ExposeRequest<?>>, SelectingIVCombiner<ResolutionRequest<?>>> combiners =
            new HashMap<>();

        @Override
        void add(ResolutionRequestCollection value, Deque<WidgetTree.IVStackItem> origin) {
            for (ResolutionRequest<?> req : value.requests()) {
                if (req.metadata().baseForAtMostOnce == null) {
                    combined.add(req);
                } else {
                    String typeDebugName = ReflectionUtil.simpleName(req.metadata().baseForAtMostOnce) +
                        " based " + ExposeRequest.class.getSimpleName() + "s";
                    combiners.computeIfAbsent(req.metadata().baseForAtMostOnce,
                        __ -> new SelectingIVCombiner<>(typeDebugName)).
                        add(req, origin);
                }
            }
        }

        @Override
        ResolutionRequestCollection finish() {
            combiners.forEach((base, combiner) -> {
                ResolutionRequest<?> req = combiner.finish();
                assert base.isAssignableFrom(req.requestData.getClass());
                boolean added = combined.add(req);
                // duplikált nem lehet, mert akkor a baseForAtMostOnce más lenne
                assert added;
            });
            return new ResolutionRequestCollection(combined);
        }
    }
}
