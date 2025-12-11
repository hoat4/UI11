package ui11.input.focus;

import ui11.observable.MutableObservable;
import ui11.provide.SupplyDefaultInstance;

/**
 * Nyilvántartja, hogy melyik az épp fókuszált elem a képernyőn.
 * <p>
 * Ha van több ablak is, akkor a tartalmukhoz külön FocusRootok tartoznak.
 */
// TODO ez félkész
@SupplyDefaultInstance
public class FocusRoot {

    private final MutableObservable<FocusHolder> focusedElement = MutableObservable.ofNullable();

    public boolean isFocused(FocusHolder e) {
        return focusedElement.get() == e;
    }

    public void requestFocus(FocusHolder e) {
        focusedElement.set(e);
    }
}

/*
    régi FocusRoot:
    public final Observable<List<Node>> focus = Observable.ofNullable();

    public void focus(Node element) {
        if (element == null) {
            focus.set(null);
            return;
        }
        List<Node> l = new ArrayList<>();
        Node e = element;
        while (e != null) {
            if (e == element || e.focusNestmates().stream().anyMatch(l::contains))
                l.add(e);
            e = e.tmp_parent();
        }
        for (int i = 0; i < l.size(); i++)
            l.get(i).focusNestmates().forEach(e2 -> {
                if (!l.contains(e2))
                    l.add(e2);
            });
        l.sort(Comparator.comparing(e2 -> -e2.tmp_depth()));
        focus.set(l);
    }

    public boolean isFocused(Node element) {
        return focus.get() != null && focus.get().contains(element);
    }

    public Node focusedElement() {
        return focus.get() == null ? null : focus.get().stream().
                filter(Node::isActive).findFirst().orElse(null);
    }
*/
