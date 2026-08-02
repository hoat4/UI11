package ui11;

import org.jspecify.annotations.NonNull;
import ui11.provide.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <a href="https://api.flutter.dev/flutter/widgets/ParentDataWidget-class.html">Same concept in Flutter</a>
 */
public final class ParentDataWidget extends Widget {

    final ParentData parentData;
    private final Widget next;

    public ParentDataWidget(@NonNull ParentData parentData, @NonNull Widget next) {
        this.parentData = Objects.requireNonNull(parentData);
        this.next = Objects.requireNonNull(next);
    }

    @Override
    protected Widget build() {
        return new Provider<>(ParentDataCollection.class, new ParentDataCollection(List.of(this)), next);
    }

    public interface ParentData {
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
