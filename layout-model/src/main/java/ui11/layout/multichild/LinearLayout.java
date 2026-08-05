package ui11.layout.multichild;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.*;
import ui11.geom.Axis;
import ui11.geom.Length;
import ui11.layout.Gap;
import ui11.layout.Gone;
import ui11.layout.Insets;
import ui11.layout.singlechild.Padding;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.stream.Collectors.joining;

// felmerült, hogy 0 db childet nem kéne engedni. De akkor meg special case-elni kéne folyton
// ha valami adathalmazból mappelünk widgetekre és abból csinálni LinearLayoutot.

// flex-grow-ra css-tricks.com ír egy ilyet:
// that child would take up twice as much of the space as either one of the others (or it will try, at least).
// ilyen a Flutter-féle layout modellben értelmezhető, hogy "or it will try"?

/**
 * A layout container, which arranges its children next to each other in horizontal or vertical direction. The available
 * space will be filled by the children, without overlapping each other. No space will remain unused if no
 * {@link #withGap(Length) gap}, {@linkplain JustifyContent justification} other than {@link JustifyContent#STRETCH} or
 * {@linkplain AlignChildren child alignment} other than {@link AlignChildren#STRETCH} is set, so if the container is
 * forced to stretch, then the children will also grow.
 * <p>
 * By default, the free space will be distributed equally to all children. This can be overridden using
 * {@link #expanded(Widget)} which specifies one child to receive all the remaining space, or using
 * {@link #withWeight(double, Widget)} which specifies a proportion that that dictates what amount of the available
 * space inside the container the item should take up. For example, if one of the children has weight set to 2 while
 * each other has weight set to 1, then that widget would take up twice as much of the distributable space as either one
 * of the others.
 * <p>
 * A LinearLayout has a main axis and a cross axis. The main axis is the primary axis along which flex items are laid
 * out. The cross axis is the axis perpendicular to the main axis is called the cross axis.
 * <p>
 * If a LinearLayout has zero children, it behaves the same as an {@link ui11.graphics.Empty} widget instead.
 * <p>
 * For similar alternatives in other environments, see
 * <a href="https://css-tricks.com/snippets/css/a-guide-to-flexbox/">Flexbox</a> in CSS and
 * <a href="https://api.flutter.dev/flutter/widgets/Flex-class.html">Row/Column/Flex</a> in Flutter.
 */
public final class LinearLayout extends SubstitutedWidget {

    private final @NonNull Axis mainAxis;
    private final @NonNull List<? extends @NonNull Widget> items;
    private final @NonNull Length gap;
    private final @NonNull JustifyContent mainAxisAlignment;
    private final @NonNull AlignChildren crossAxisAlignment;

    @Remember private Slot.SlotList slots;

    /**
     * @param items ebben nullok helyett {@link Gone Gone-ok} szerepeljenek
     */
    private LinearLayout(@NonNull Axis mainAxis, @NonNull List<? extends Widget> items,
                         @NonNull Length gap,
                         @NonNull JustifyContent mainAxisAlignment,
                         @NonNull AlignChildren crossAxisAlignment) {
        this.items = items;
        this.mainAxis = mainAxis;
        this.gap = gap;
        this.mainAxisAlignment = mainAxisAlignment;
        this.crossAxisAlignment = crossAxisAlignment;
    }

    /**
     * Returns a layout container that lays out the specified widgets horizontally side by side.
     * <p>
     * Same as {@code rowBuilder().add(children).build()}
     *
     * @see #column(Widget...)
     */
    public static @NonNull LinearLayout row(@Nullable Widget @NonNull ... children) {
        Objects.requireNonNull(children, "children");
        return new LinearLayout(Axis.HORIZONTAL, Gone.replaceNullsWithGone(children), Length.zero(),
                JustifyContent.STRETCH, AlignChildren.STRETCH);
    }

    public static @NonNull LinearLayout row(@NonNull JustifyContent justifyContent,
                                            @Nullable Widget @NonNull ... children) {
        Objects.requireNonNull(children, "justifyContent");
        Objects.requireNonNull(children, "children");
        return new LinearLayout(Axis.HORIZONTAL, Gone.replaceNullsWithGone(children), Length.zero(),
                justifyContent, AlignChildren.STRETCH);
    }

    public static @NonNull LinearLayout row(
            @NonNull Length gap,
            @Nullable Widget @NonNull ... children) {
        Objects.requireNonNull(gap, "gap size");
        Objects.requireNonNull(children, "children");
        return new LinearLayout(Axis.HORIZONTAL, Gone.replaceNullsWithGone(children), gap,
                JustifyContent.STRETCH, AlignChildren.STRETCH);
    }

    public static @NonNull LinearLayout row(@NonNull AlignChildren crossAxisAlignment,
                                            @Nullable Widget @NonNull ... children) {
        Objects.requireNonNull(crossAxisAlignment, "crossAxisAlignment");
        Objects.requireNonNull(children, "children");
        return new LinearLayout(Axis.HORIZONTAL, Gone.replaceNullsWithGone(children), Length.zero(),
                JustifyContent.STRETCH, crossAxisAlignment);
    }

    public static @NonNull LinearLayout row(
            @NonNull JustifyContent justifyContent,
            @NonNull AlignChildren alignChildren,
            @Nullable Widget @NonNull ... children) {
        Objects.requireNonNull(justifyContent, "justifyContent");
        Objects.requireNonNull(alignChildren, "alignChildren");
        Objects.requireNonNull(children, "children");
        return new LinearLayout(Axis.HORIZONTAL, Gone.replaceNullsWithGone(children), Length.zero(),
                justifyContent, alignChildren);
    }

    public static @NonNull LinearLayout row(
            @NonNull Length gap,
            @NonNull AlignChildren crossAxisAlignment,
            @Nullable Widget @NonNull ... children) {
        Objects.requireNonNull(gap, "gap size");
        Objects.requireNonNull(crossAxisAlignment, "crossAxisAlignment");
        Objects.requireNonNull(children, "children");
        return new LinearLayout(Axis.HORIZONTAL, Gone.replaceNullsWithGone(children), gap,
                JustifyContent.STRETCH, crossAxisAlignment);
    }

    /**
     * Returns a layout container that lays out the specified widgets horizontally.
     * <p>
     * Same as {@code columnBuilder().add(children).build()}
     *
     * @see #row(Widget...)
     */
    public static @NonNull LinearLayout column(@Nullable Widget @NonNull ... children) {
        Objects.requireNonNull(children, "children");
        return new LinearLayout(Axis.VERTICAL, Gone.replaceNullsWithGone(children), Length.zero(),
                JustifyContent.STRETCH, AlignChildren.STRETCH);
    }

    public static @NonNull LinearLayout column(@NonNull JustifyContent justifyContent,
                                               @Nullable Widget @NonNull ... children) {
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(justifyContent, "justifyContent");
        return new LinearLayout(Axis.VERTICAL, Gone.replaceNullsWithGone(children), Length.zero(),
                justifyContent, AlignChildren.STRETCH);
    }

    public static @NonNull LinearLayout column(@NonNull Length gap,
                                               @Nullable Widget @NonNull ... children) {
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(gap, "gap size");
        return new LinearLayout(Axis.VERTICAL, Gone.replaceNullsWithGone(children), gap,
                JustifyContent.STRETCH, AlignChildren.STRETCH);
    }

    public static @NonNull LinearLayout column(AlignChildren crossAxisAlignment,
                                               @Nullable Widget @NonNull ... children) {
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(crossAxisAlignment, "crossAxisAlignment");
        return new LinearLayout(Axis.VERTICAL, Gone.replaceNullsWithGone(children), Length.zero(),
                JustifyContent.STRETCH, crossAxisAlignment);
    }

    public static @NonNull LinearLayout column(@NonNull JustifyContent justifyContent,
                                               @NonNull AlignChildren crossAxisAlignment,
                                               @Nullable Widget @NonNull ... children) {
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(justifyContent, "justifyContent");
        Objects.requireNonNull(crossAxisAlignment, "crossAxisAlignment");
        return new LinearLayout(Axis.VERTICAL, Gone.replaceNullsWithGone(children), Length.zero(),
                justifyContent, crossAxisAlignment);
    }

    public static @NonNull LinearLayout column(@NonNull Length gap,
                                               @NonNull AlignChildren crossAxisAlignment,
                                               @Nullable Widget @NonNull ... children) {
        Objects.requireNonNull(children, "children");
        Objects.requireNonNull(gap, "gap size");
        Objects.requireNonNull(crossAxisAlignment, "crossAxisAlignment");
        return new LinearLayout(Axis.VERTICAL, Gone.replaceNullsWithGone(children), gap,
                JustifyContent.STRETCH, crossAxisAlignment);
    }

    public static @NonNull LinearLayout row(@NonNull Consumer<@NonNull Builder> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        Builder b = new Builder(Axis.HORIZONTAL);
        consumer.accept(b);
        return b.build();
    }

    // TODO ez mindenképpen azonnal hívja meg a consumert, vagy lehet lazy is?
    public static @NonNull LinearLayout column(@NonNull Consumer<@NonNull Builder> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        Builder b = new Builder(Axis.VERTICAL);
        consumer.accept(b);
        return b.build();
    }

    /**
     * Same as {@code withMainAxis(Axis.HORIZONTAL)}
     */
    public static @NonNull Builder rowBuilder() {
        return new Builder(Axis.HORIZONTAL);
    }

    /**
     * Same as {@code withMainAxis(Axis.VERTICAL)}
     */
    public static @NonNull Builder columnBuilder() {
        return new Builder(Axis.VERTICAL);
    }

    /**
     * Returns a {@link Builder} that can be used to create a layout container that places the specified widgets side by
     * side along the specified axis. If {@code mainAxis} is {@linkplain Axis#VERTICAL} the container will be a
     * {@link #column(Widget...) column}, if {@code mainAxis} is {@linkplain Axis#HORIZONTAL} the container will be a
     * {@link #row(Widget...) row}.
     */
    public static @NonNull Builder withMainAxis(@NonNull Axis mainAxis) {
        Objects.requireNonNull(mainAxis, "mainAxis");
        return new Builder(mainAxis);
    }

    /**
     * Returns a {@link Builder} that can be used to make a layout container that can be used to create a layout
     * container that places the specified widgets side by side, perpendicular to the specified axis. If
     * {@code crossAxis} is {@linkplain Axis#VERTICAL} the container will be a {@link #row(Widget...) row}, if
     * {@code crossAxis} is {@linkplain Axis#HORIZONTAL} the container will be a {@link #column(Widget...) column}.
     */
    public static @NonNull Builder withCrossAxis(@NonNull Axis crossAxis) {
        Objects.requireNonNull(crossAxis, "crossAxis");
        return new Builder(crossAxis.cross());
    }

    /**
     * Returns a Collector that accumulates Widgets into a horizontally arranged LinearLayout.
     * <p>
     * Same as {@code rowBuilder().toCollector()}
     */
    @SuppressWarnings("unchecked")
    public static <W extends Widget> Collector<@Nullable W, ?, @NonNull LinearLayout> toRow() {
        return (Collector<W, ?, LinearLayout>) LinearLayoutCollector.ROW_DEFAULT;
    }

    /**
     * Returns a Collector that accumulates Widgets into a vertically arranged LinearLayout.
     * <p>
     * Same as {@code columnBuilder().toCollector()}
     */
    @SuppressWarnings("unchecked")
    public static <W extends Widget> Collector<@Nullable W, ?, @NonNull LinearLayout> toColumn() {
        return (Collector<W, ?, LinearLayout>) LinearLayoutCollector.COLUMN_DEFAULT;
    }

    /**
     * This only takes effect is {@linkplain #withJustifyContent(JustifyContent) justifyContent} is set to
     * {@link JustifyContent#STRETCH}. In case of other JustifyContent values, the weights will be ignored.
     * <p>
     * Similar to <a
     * href="https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/flex-grow">{@code flex-grow}</a> in
     * CSS or <a href="https://api.flutter.dev/flutter/widgets/Flexible/flex.html">Flexible.flex</a> in Flutter or
     * <a href="https://developer.android.com/reference/android/widget/LinearLayout.LayoutParams#weight">weight</a>
     * in Android.
     *
     * @param weight must be a positive and {@link Double#isFinite(double) finite} number
     * @param w      if null, this method will return null
     */
    // TODO Gone-nál mit jelent a withWeight?
    // ez annyiban különbözik flex-growtól, hogy ott justify-content != stretch esetén is működik, gondolom ekkor
    // a justify-content értéke ignorálva van
    public static @Nullable Widget withWeight(double weight, @Nullable Widget w) {
        // ha e == null de weight érvénytelen, akkor kéne exceptiont dobni?
        if (w == null)
            return null;
        // lehetne ellenőrizni hogy w instanceof WeightMarker és akkor el lehet dobni a belsőt, de valszeg kevésszer fordul
        // elő ilyen.
        // vagy lehetne csinálni egy ilyen factory methodot ParentDataWidgetbe ami ezt csinálja
        return ParentData.attach(new WeightMarker(weight), w);
    }

    // TODO legális expanded-et használni több childre? és ha van már weight beállítva?

    /**
     * If no other widgets have a set weight and this method is only applied for one of the widgets in a LinearLayout,
     * then the {@linkplain WeightMarker} produced by this method will have the same behavior as
     * {@link #withWeight(double, Widget)} with any positive finite number as weight.
     *
     * @param w if {@code null}, this method will return {@code null}
     */
    // TODO sok helyen talán meg lehetne szüntetni az expanded-et. pl. RunningMatchRow.player2 esetén
    //      a playerimageview nem tud nyúlni, de az alignchildrenes column viszont igen.
    public static @Nullable Widget expanded(@Nullable Widget w) {
        return withWeight(1, w);
    }

    // Flutter és CSS-ben is van ez a main axis / cross axis terminológia

    /**
     * Returns the main axis of this layout container, i.e. the axis along which the children are placed (horizontal or
     * vertical).
     *
     * @return the axis perpendicular to {@link #crossAxis()}
     */
    public @NonNull Axis mainAxis() {
        return mainAxis;
    }

    /**
     * Returns the cross axis of this layout container, i.e. the axis along which the children's size are same.
     *
     * @return the axis perpendicular to {@link #mainAxis()}
     */
    public @NonNull Axis crossAxis() {
        return mainAxis.cross();
    }

    public @NonNull JustifyContent mainAxisAlignment() {
        return mainAxisAlignment;
    }

    public @NonNull AlignChildren crossAxisAlignment() {
        return crossAxisAlignment;
    }

    /**
     *
     * @return an {@link List#of() unmodifiable list} of non-null Widgets
     */
    public @NonNull List<? extends Widget> items() { // név inkább "children"?
        return items;
    }

    /**
     * Returns the gap that will be put between each child.
     */
    public @NonNull Length gap() {
        return gap;
    }

    /**
     * A megadott hossz lesz az elemek közti egyenkénti helyköz is, és az elemek összességét körülvevő szegély is.
     */
    // TODO ez biztos jó ide? belekever cross-axis irányú paddingot is
    public Widget withPadAndGap(Length padAndGap) {
        if (padAndGap.isZero())
            return this;

        return new Padding(
                Insets.all(padAndGap),
                withGap(padAndGap)
        );
    }

    /**
     * Sets the gap between all children.
     */
    // TODO hogyan viszonyul a gap JustifyContent.SPACE_...-hoz?
    public @NonNull LinearLayout withGap(@NonNull Length gap) {
        Objects.requireNonNull(gap, "gap size");
        return new LinearLayout(mainAxis, items, gap, mainAxisAlignment, crossAxisAlignment);
    }

    /**
     * Makes a new LinearLayout where every item will be aligned as specified in the parameter along the cross axis.
     * <p>
     * If the specified alignment is not {@link AlignChildren#STRETCH}, and a child had cross axis alignment set with
     * {@link ui11.layout.singlechild.Align}, this alignment will overwrite it.
     */
    // ehhez az "overrideoláshoz" írjuk oda hogy következik a layout modellből
    public @NonNull LinearLayout withAlignChildren(AlignChildren crossAxisAlignment) {
        Objects.requireNonNull(crossAxisAlignment, "crossAxisAlignment");
        return new LinearLayout(mainAxis, items, gap, mainAxisAlignment, crossAxisAlignment);
    }

    /**
     * If mainAxisAlignment is not {@link JustifyContent#STRETCH} then the {@link #withWeight(double, Widget) weight} of
     * the children will be ignored.
     */
    public @NonNull LinearLayout withJustifyContent(@NonNull JustifyContent mainAxisAlignment) {
        Objects.requireNonNull(mainAxisAlignment, "mainAxisAlignment");
        return new LinearLayout(mainAxis, items, gap, mainAxisAlignment, crossAxisAlignment);
    }

    /**
     * If the parameter is {@code true}, returns a LinearLayout that displays the children in the reverse order of the
     * children's current order (with the same gap, direction, etc.). If the parameter is {@code false}, returns a
     * LinearLayout that behaves exactly the same as this LinearLayout.
     */
    public @NonNull LinearLayout reversedIf(boolean reverse) {
        if (!reverse)
            return this;

        List<Widget> l = new ArrayList<>(items);
        Collections.reverse(l);
        return new LinearLayout(mainAxis, List.copyOf(l), Length.zero(), mainAxisAlignment, crossAxisAlignment);
    }

    @Override
    protected void initState() {
        slots = new Slot.SlotList();
    }

    @Override
    protected LinearLayout forSubstitution() {
        return new LinearLayout(
                mainAxis,
                slots.with(items),
                gap,
                mainAxisAlignment,
                crossAxisAlignment
        );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + mainAxis + (items.isEmpty() ? " {}" : " {\n" +
                items.stream().
                        map(w -> "  " + w.toString().replace("\n", "\n  ")).
                        collect(joining(", \n"))
                + "\n}");
    }

    public static class Builder {

        private final @NonNull Axis axis;
        /**
         * Ha {@linkplain Builder#itemsMutable} true, akkor ez egy ArrayList. Ha viszont false, akkor ez vagy null, vagy
         * egy List.of-féle immutable collection.
         */
        // ArrayList helyett lehetne Widget[] is, és akkor megspórolnánk Arrays.asList-eket.
        // de nem tudom, mennyit számít az.
        private List<@NonNull Widget> items;
        private boolean itemsMutable;
        private @NonNull Length gap = Length.zero();
        private JustifyContent mainAxisAlignment = JustifyContent.STRETCH;
        private AlignChildren crossAxisAlignment = AlignChildren.STRETCH;

        private Builder(@NonNull Axis axis) {
            this.axis = Objects.requireNonNull(axis, "axis");
        }

        public Builder add(@Nullable Widget w) {
            if (!itemsMutable) {
                List<Widget> newItems = new ArrayList<>();
                if (items != null)
                    newItems.addAll(items);
                items = newItems;
            }
            items.add(Gone.goneIfNull(w));
            return this;
        }

        // addig, amíg nem tértünk vissza a Widget klónozásra. utána ezt a függvényt ki lehet törölni
        public Builder addRepeatedly(int n, Supplier<? extends Widget> supplier) {
            for (int i = 0; i < n; i++) {
                add(supplier.get());
            }
            return this;
        }

        public Builder add(Widget... children) {
            return add(Arrays.asList(children));
        }

        public Builder add(List<? extends @Nullable Widget> children) {
            Objects.requireNonNull(children, "Child list must be non-null. Only elements of it can be null.");

            // azért nem csinálunk rögtön items.toArray-t, mert ha items egy List.of-os immutable collection,
            // akkor List.copyOf nop

            for (Widget w : children) {
                if (w == null) {
                    Widget[] widgetsArray = children.toArray(Widget[]::new);
                    for (int i = 0; i < widgetsArray.length; i++)
                        widgetsArray[i] = Gone.goneIfNull(widgetsArray[i]);

                    addNullFreeTrustedWidgetArray(widgetsArray);

                    return this;
                }
            }

            // nem volt köztük null

            addNullFreeList((List<? extends @NonNull Widget>) children);

            return this;
        }

        // trusted array = nem lesz módosítva a jövőben a hívó által
        private void addNullFreeTrustedWidgetArray(@NonNull Widget[] children) {
            // azért ilyen bonyolult ez (meg az addNullFreeList), mert arra az esetre optimalizálunk, hogy
            // egyetlen add(List) hívás lesz, és azon kívül nem hívnak más add/gap függvényt.

            if (items == null)
                items = List.of(children);
            else if (itemsMutable)
                items.addAll(Arrays.asList(children));
            else
                childListTransitionToMutable(children.length, Arrays.asList(children));
        }

        private void addNullFreeList(List<? extends @NonNull Widget> children) {
            if (items == null)
                items = List.copyOf(children);
            else if (itemsMutable)
                items.addAll(children);
            else {
                List<@NonNull Widget> copy = List.copyOf(children);
                childListTransitionToMutable(copy.size(), copy);
            }
        }

        private void childListTransitionToMutable(int newItemCount, List<@NonNull Widget> newItems) {
            List<@NonNull Widget> newList = new ArrayList<>(items.size() + newItemCount);
            // lehet hogy az új mérethez hozzá kéne még adni egy keveset, hogy következő addkor ne rögtön arraycopy
            // legyen
            newList.addAll(items);
            newList.addAll(newItems);
            items = newList;
            itemsMutable = true;
        }

        public Builder add(double weight, @Nullable Widget w) {
            add(withWeight(weight, Gone.goneIfNull(w)));
            return this;
        }

        public Builder addGap(@NonNull Length gap) {
            Objects.requireNonNull(gap, "gap size");
            add(new Gap(axis, gap));
            return this;
        }

        public Builder addExpanded(@Nullable Widget w) {
            add(LinearLayout.expanded(Gone.goneIfNull(w)));
            return this;
        }

        public Builder overallGap(@NonNull Length gap) {
            Objects.requireNonNull(gap, "gap size");
            this.gap = gap;
            return this;
        }

        public Builder justify(JustifyContent mainAxisAlignment) {
            Objects.requireNonNull(mainAxisAlignment);
            this.mainAxisAlignment = mainAxisAlignment;
            return this;
        }

        public Builder align(AlignChildren crossAxisAlignment) {
            Objects.requireNonNull(crossAxisAlignment);
            this.crossAxisAlignment = crossAxisAlignment;
            return this;
        }

        public @NonNull LinearLayout build() {
            return new LinearLayout(axis, items == null ? List.of() : List.copyOf(items),
                    gap, mainAxisAlignment, crossAxisAlignment);
        }

        public <W extends Widget> Collector<@Nullable W, ?, @NonNull LinearLayout> toCollector() {
            return new LinearLayoutCollector<>(axis, items == null ? List.of() : List.copyOf(items),
                    gap, mainAxisAlignment, crossAxisAlignment);
        }
    }

    public static record WeightMarker(double weight) implements ParentData {

        public WeightMarker {
            if (weight < 0 || !Double.isFinite(weight))
                throw new IllegalArgumentException("invalid weight: " + weight);
        }

        /**
         * {@link PeerRequestor#withInterestedParentDataType(Class[])}-et meg kell hívni {@link WeightMarker}-rel ahhoz,
         * hogy ez működjön
         */
        public static double weight(PeerRequestor.Result<?> peerResult) {
            LinearLayout.WeightMarker weightM = (LinearLayout.WeightMarker)
                    peerResult.parentDataList().get(ParentData.class);
            return weightM == null ? 0 : weightM.weight();
        }
    }

    private static class LinearLayoutCollector<W extends Widget> implements Collector<W, List<Widget>, LinearLayout> {

        static final LinearLayoutCollector<Widget> COLUMN_DEFAULT = new LinearLayoutCollector<>(Axis.VERTICAL,
                List.of(), Length.zero(), JustifyContent.STRETCH, AlignChildren.STRETCH);
        static final LinearLayoutCollector<Widget> ROW_DEFAULT = new LinearLayoutCollector<>(Axis.HORIZONTAL,
                List.of(), Length.zero(), JustifyContent.STRETCH, AlignChildren.STRETCH);

        private final @NonNull Axis axis;
        private final @NonNull List<? extends @NonNull Widget> firstWidgets;
        private final @NonNull JustifyContent justifyContent;
        private final @NonNull AlignChildren alignChildren;
        private final @NonNull Length gap;

        LinearLayoutCollector(@NonNull Axis axis, @NonNull List<? extends @NonNull Widget> firstWidgets,
                              @NonNull Length gap, @NonNull JustifyContent justifyContent, @NonNull AlignChildren alignChildren) {
            this.axis = axis;
            this.firstWidgets = firstWidgets;
            this.justifyContent = justifyContent;
            this.alignChildren = alignChildren;
            this.gap = gap;
        }

        @Override
        public Supplier<List<Widget>> supplier() {
            return ArrayList::new; // vagy SpinedBuffer-szerűség?
        }

        @Override
        public BiConsumer<List<Widget>, W> accumulator() {
            return (list, w) -> {
                list.add(Gone.goneIfNull(w));
            };
        }

        @Override
        public BinaryOperator<List<Widget>> combiner() {
            return (a, b) -> {
                a.addAll(b);
                return a;
            };
        }

        @Override
        public Function<List<Widget>, LinearLayout> finisher() {
            return l -> {
                List<? extends Widget> children;
                if (firstWidgets.isEmpty())
                    children = List.copyOf(l);
                else {
                    List<Widget> combinedChildren = new ArrayList<>(firstWidgets.size() + l.size());
                    combinedChildren.addAll(firstWidgets);
                    combinedChildren.addAll(l);
                    children = List.copyOf(combinedChildren);
                }
                return new LinearLayout(axis, children, gap, justifyContent, alignChildren);
            };
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of();
        }
    }

    /**
     * Defines how a LinearLayout will distribute space between and around content items along the main axis.
     * <p>
     * Here is an example with a horizontal layout container with three items that are smaller than the available
     * size:<br>
     * <img src="doc-files/JustifyContent.svg" width=746 height=458 alt="Visualization of each content justification
     * type">
     */
    public enum JustifyContent {

        /**
         * The children will be stretched to the containers edges along the
         * {@linkplain LinearLayout#mainAxis() main axis}, no space will be left on the start and end of the container.
         * The children will grow as much as specified using {@link LinearLayout#withWeight(double, Widget)} or
         * {@link LinearLayout#expanded(Widget)}.
         */
        STRETCH,

        // ez a következő 3 nem biztos hogy tényleg kell-e ide, mert redundáns Align.left, stb.-vel.
        // de talán valamennyivel olvashatóbb így a használó kódok, mint egy plusz wrappeléssel

        /**
         * Place the children as close to the start of the main axis as possible.
         * <p>
         * This causes the safe effect as setting
         * {@linkplain LinearLayout#withJustifyContent(JustifyContent) justifyContent} to {@link #STRETCH} then wrapping
         * the LinearLayout in {@link ui11.layout.singlechild.Align#left(Widget) Align.left} in case of horizontal
         * layout or {@link ui11.layout.singlechild.Align#top(Widget) Align.top} in case of vertical layout.
         */
        START,

        /**
         * Place the children as close to the middle of the main axis as possible.
         * <p>
         * This causes the safe effect as setting
         * {@linkplain LinearLayout#withJustifyContent(JustifyContent) justifyContent} to {@link #STRETCH} then wrapping
         * the LinearLayout in {@link ui11.layout.singlechild.Align#hcenter(Widget) Align.hcenter} in case of horizontal
         * layout or {@link ui11.layout.singlechild.Align#vcenter(Widget) Align.vcenter} in case of vertical layout.
         */
        CENTER,

        /**
         * Place the children as close to the end of the main axis as possible.
         * <p>
         * This causes the safe effect as setting
         * {@linkplain LinearLayout#withJustifyContent(JustifyContent) justifyContent} to {@link #STRETCH} then wrapping
         * the LinearLayout in {@link ui11.layout.singlechild.Align#right(Widget) Align.right} in case of horizontal
         * layout or {@link ui11.layout.singlechild.Align#bottom(Widget) Align.bottom} in case of vertical layout.
         */
        END,

        /**
         * The space will be distributed so that the spacing between any two items and the space to the edges are equal
         * (along the main axis).
         * <p>
         * If any child has a {@link #withWeight(double, Widget) weight}, the weights will be ignored.
         */
        SPACE_EVENLY,

        /**
         * Means that items are evenly distributed in the line with equal space around them. Note that visually the
         * spaces aren’t equal, since all the items have equal space on both sides. The first item will have one unit of
         * space against the container edge, but two units of space between the next item because that next item has its
         * own spacing that applies.
         * <p>
         * If any child has a {@link #withWeight(double, Widget) weight}, the weights will be ignored.
         */
        SPACE_AROUND,

        /**
         * items are evenly distributed in the line; first item is on the start line, last item on the end line
         * <p>
         * If any child has a {@link #withWeight(double, Widget) weight}, the weights will be ignored.
         */
        SPACE_BETWEEN
    }

    public enum AlignChildren {

        /**
         * Children will fill space from the begin to end in the cross axis of the container.
         */
        STRETCH,

        /**
         * Every child will be aligned at the start edge (top or left) along the cross axis. If the container is a
         * {@linkplain #row(Widget...) row}, then this setting will align every child to the top edge of the container.
         * If the container is a {@linkplain #column(Widget...) column}, then this setting will align every child to the
         * left edge of the container.
         * <p>
         * Note: If a child has an alignment in the container's by the {@link ui11.layout.singlechild.Align} widget,
         * this alignment will overwrite it.
         */
        // TODO írjuk bele a dokba hogy RTL nyelveknél megfordul. vagy csináljuk azt mint JavaFX hogy pl. leftek hívjuk
        //      de RTL esetén right-nak  vesszük?
        START,

        /**
         * Every child will be aligned at the start edge (top or left) along the cross axis. If the container is a
         * {@linkplain #row(Widget...) row}, then this setting will align every child to the top edge of the container.
         * If the container is a {@linkplain #column(Widget...) column}, then this setting will align every child to the
         * left edge of the container.
         * <p>
         * Note: If a child has an alignment in the container's by the {@link ui11.layout.singlechild.Align} widget,
         * this alignment will overwrite it.
         */
        CENTER,

        /**
         * Every child will be aligned at the start edge (top or left) along the cross axis. If the container is a
         * {@linkplain #row(Widget...) row}, then this setting will align every child to the top edge of the container.
         * If the container is a {@linkplain #column(Widget...) column}, then this setting will align every child to the
         * left edge of the container.
         * <p>
         * Note: If a child has an alignment in the container's by the {@link ui11.layout.singlechild.Align} widget,
         * this alignment will overwrite it.
         */
        END
    }
}
