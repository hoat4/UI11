package ui11;

import ui11.provide.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <a href="https://api.flutter.dev/flutter/widgets/ParentDataWidget-class.html">Same concept in Flutter</a>
 */
public abstract class ParentDataWidget extends SubstitutedWidget {

    private final Widget next;

    protected ParentDataWidget(Widget next) {
        this.next = Objects.requireNonNull(next);
    }

    public static ParentDataWidget of(SubstitutedWidget parentData, Widget content) {
        if (parentData instanceof ParentDataWidget)
            throw new IllegalArgumentException("already a " + ParentDataWidget.class.getSimpleName() + ": " + parentData);
        return new CombinerParentDataWidget(parentData, content);
    }

    @Override
    protected final Widget fallbackContent() {
        return new Provider<>(ParentDataCollection.class, new ParentDataCollection(List.of(this)), next);
    }

    static class CombinerParentDataWidget extends ParentDataWidget {

        final SubstitutedWidget parentData;

        protected CombinerParentDataWidget(SubstitutedWidget parentData, Widget next) {
            super(next);
            this.parentData = parentData;
        }
    }

    static class ParentDataCollection implements Provider.Mergeable<ParentDataCollection> {

        static final ParentDataCollection CLEAR = new ParentDataCollection(List.of()) {
            @Override
            public ParentDataCollection mergeWith(ParentDataCollection defaults) {
                return this;
            }
        };

        // TODO ezeknek az equalsjébe beleszámít ParentDataWidget.next is
        final List<? extends ParentDataWidget> parentDataList;

        ParentDataCollection(List<? extends ParentDataWidget> parentDataList) {
            this.parentDataList = List.copyOf(parentDataList);
        }

        @Override
        public ParentDataCollection mergeWith(ParentDataCollection defaults) {
            List<ParentDataWidget> l = new ArrayList<>(defaults.parentDataList);
            l.addAll(parentDataList);
            return new ParentDataCollection(l);
        }
    }
}
