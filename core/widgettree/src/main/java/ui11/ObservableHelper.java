package ui11;

import ui11.observable.ObservableBase;
import ui11.observable.ObserverCollection;
import ui11.observable.ObserverHolder;
import ui11.Element.ElementState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

class ObservableHelper implements ObserverCollection {

    private static final Logger logger = LoggerFactory.getLogger(ObservableHelper.class);

    final Element node;
    private Object observed; // ObservableBase[] vagy Set
    private long[] stateAtPause;
    private int[] pausedObservablesObservers;
    private State state = State.NORMAL;

    public ObservableHelper(Element node) {
        this.node = node;
    }

    @Override
    public void invalidate(int observerMask, Supplier<String> debugMessageSupplier) {
        if (ObserverHolder.current().obsC == this)
            return;
        switch (state) {
            case NORMAL -> {
                if (node.elementState == ElementState.REFRESHING_SELF_BEFORE_CHILDREN ||
                        node.elementState == ElementState.REFRESHING_SELF_AFTER_CHILDREN ||
                        node.elementState == ElementState.REFRESHING_CHILDREN_AFTER_SELF ||
                        node.elementState == ElementState.REFRESHING_CHILDREN_SECOND) {
                    // ez valójában elvileg egy általánosított esete a fenti current.obsC == this-nek.
                    // azért került ide, mert előjött tabbedpanenél jött elő az, hogy MutableTabbedPane no-arg
                    // konstruktorában
                    // ki volt olvasni egy
                    // önmaga által beállított üres érték, arra ezáltal feliratkozottt a MutableTabbedPane-t
                    // létrehozó HTMLAlignPeer, ami utána refresheltette lookup-pal MutableTabbedPane-t,
                    // ami beírta a tabs listába egy értéket, amitől invalidálódott HTMLAlignPeer.
                    // előjött 2024-07-24-kor is, de REFRESHING_CHILDREN-nel, mert ott csak delegate lánc részeként
                    // hozódott létre a MutableTabbedPane, és a children refreshkor lett volna végrehajt az elindulás.
                    // TODO ez dobhatna inkább egy exceptiont, és akkor a hívó elkaphatná és plusz információval
                    //      továbbdobhatná (pl. invalidateChangedUpValues esetén)
                    logger.warn("Observed value was invalidated, " +
                                    "but node is in " + node.elementState + " state: " + node + "\n" +
                                    (debugMessageSupplier != null ?
                                            "Information about the change: " + debugMessageSupplier.get() + "\n" : "") +
                                    "Refresh stack: \n" + node.refreshStackToString(Map.of()),
                            new Exception());
                    return;
                }

                state = State.INVALIDATED;
                removeObservers();
                node.requestRefresh();
            }
            case INVALIDATED -> {
            }
            default /* PAUSED, PAUSED_INVALIDATED */ -> throw new IllegalStateException();
        }
    }

    @Override
    public void subscribedTo(ObservableBase newO) {
        assert newO.findObservers(this) != 0;

        ObservableBase[] arr;
        if (observed == null)
            observed = arr = new ObservableBase[5];
        else if (observed.getClass() == ObservableBase[].class)
            // instanceof Set<?> set volt korábban, de az lassabb
            arr = (ObservableBase[]) observed;
        else {
            ((Set<ObservableBase>) observed).add(newO);
            return;
        }

        for (int i = 0; i < 5; i++) {
            ObservableBase o = arr[i];
            if (o == newO)
                return;
            if (o == null) {
                for (int j = i + 1; j < 5; j++) {
                    o = arr[j];
                    if (o == newO)
                        return;
                }
                arr[i] = newO;
                return;
            }
        }

        Set<Object> s = Collections.newSetFromMap(new IdentityHashMap<>());
        s.add(arr[0]);
        s.add(arr[1]);
        s.add(arr[2]);
        s.add(arr[3]);
        s.add(arr[4]);
        s.add(newO);
        observed = s;
    }

    @Override
    public void checkObserver(int mask) {
    }

    public void removeObserversIfNeeded() {
        if (state == State.NORMAL)
            removeObservers();
    }

    private void removeObservers() {
        if (observed == null)
            return;
        if (observed.getClass() == ObservableBase[].class) {
            ObservableBase[] a = (ObservableBase[]) observed;
            for (int i = 0; i < a.length; i++) {
                ObservableBase o = a[i];
                if (o != null) {
                    o.removeObserver(this, 1);
                    a[i] = null;
                }
            }
        } else {
            @SuppressWarnings("unchecked")
            Set<ObservableBase> s = (Set<ObservableBase>) observed;
            s.forEach(o -> o.removeObserver(this, 1));
            s.clear();
        }
    }

    public void pause() {
        if (ObserverHolder.current().obsC == node.observableHelper)
            throw new UnsupportedOperationException("TODO");

        assert stateAtPause == null;
        switch (state) {
            case INVALIDATED -> state = State.PAUSED_INVALIDATED;
            case NORMAL -> {
                state = State.PAUSED;
                doPause();
            }
            default /* PAUSED, PAUSED_INVALIDATED*/ -> throw new IllegalStateException();
        }
    }

    private void doPause() {
        if (observed instanceof Set<?>) {
            Set<ObservableBase> s = (Set<ObservableBase>) observed;
            stateAtPause = new long[s.size()];
            pausedObservablesObservers = new int[s.size()];
            int i = 0;
            for (ObservableBase obs : s) {
                stateAtPause[i] = obs.invalidationCount;
                int observers = obs.removeObserver(this, -1);
                assert observers != 0;
                pausedObservablesObservers[i++] = observers;
            }
        } else if (observed != null) {
            ObservableBase[] arr = (ObservableBase[]) observed;
            stateAtPause = new long[arr.length];
            pausedObservablesObservers = new int[arr.length];
            for (int i = 0; i < arr.length; i++) {
                ObservableBase obs = arr[i];
                if (obs == null) {
                    continue;
                }
                stateAtPause[i] = obs.invalidationCount;
                int observers = obs.removeObserver(this, -1);
                assert observers != 0;
                pausedObservablesObservers[i] = observers;
            }
        }
    }

    public boolean resume() {
        switch (state) {
            case PAUSED_INVALIDATED -> {
                state = State.NORMAL;
                return true;
            }
            case PAUSED -> {
                state = State.NORMAL;
                return doResume();
            }
            default /* NORMAL, INVALIDATED*/ -> {
                throw new IllegalStateException();
            }
        }
    }

    private boolean doResume() {
        int invalidation = 0;
        if (observed instanceof Set<?>) {
            Set<ObservableBase> s = (Set<ObservableBase>) observed;
            int i = 0;
            for (ObservableBase obs : s) {
                int o = pausedObservablesObservers[i];
                if (obs.invalidationCount != stateAtPause[i])
                    invalidation |= o;
                obs.addObserver(this, o);
                assert obs.findObservers(this) != 0;
                i++;
            }
        } else if (observed == null)
            return false;
        else {
            ObservableBase[] arr = (ObservableBase[]) observed;
            stateAtPause = new long[arr.length];
            for (int i = 0; i < arr.length; i++) {
                ObservableBase obs = arr[i];
                if (obs == null)
                    continue;
                int o = pausedObservablesObservers[i];
                if (obs.invalidationCount != stateAtPause[i])
                    invalidation |= o;
                obs.addObserver(this, o);
            }
        }

        stateAtPause = null;
        pausedObservablesObservers = null;
        return invalidation != 0;
    }

    /**
     * Ez törli az invalidated státuszt, ha az volt eddig
     */
    public boolean haveBeenInvalidated() {
        return switch (state) {
            case NORMAL -> false;
            case INVALIDATED -> {
                state = State.NORMAL;
                yield true;
            }
            case PAUSED, PAUSED_INVALIDATED -> throw new IllegalStateException();
        };
    }

    void debug_assertStateNormal() {
        assert state == State.NORMAL : node + ", " + state + ", " + node.elementState;
    }

    private enum State {
        NORMAL, INVALIDATED, PAUSED, PAUSED_INVALIDATED
    }

    @Override
    public String toString() {
        return "OH for " + node;
    }
}
