package ui11.observable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ObservableBase {

    private ObserverCollection obs1, obs2, obs3;
    private Set<ObserverCollection> observers;
    // TODO ideiglenesen publikus
    public long invalidationCount;

    protected void onRead() {
        final ObserverHolder h = ObserverHolder.current();
        ObserverCollection observerObserverCollection = h.obsC;
        if (observerObserverCollection != null && addObserver(observerObserverCollection))
            observerObserverCollection.subscribedTo(this);
    }

    protected void onWrite() {
        onWrite(null);
    }

    void onWrite(Supplier<String> debugMessageSupplier) {
        invalidationCount++;

        if (obs1 == null && obs2 == null && obs3 == null && observers == null)
            return;

        if (observers == null) {
            ObserverCollection obs1 = this.obs1, obs2 = this.obs2, obs3 = this.obs3;
            if (obs1 != null && ObserverHolder.current().obsC != obs1)
                obs1.invalidate(debugMessageSupplier);
            if (obs2 != null && ObserverHolder.current().obsC != obs2)
                obs2.invalidate(debugMessageSupplier);
            if (obs3 != null && ObserverHolder.current().obsC != obs3)
                obs3.invalidate(debugMessageSupplier);
        } else {
            observers.forEach(o -> {
                if (o != ObserverHolder.current().obsC)
                    o.invalidate(debugMessageSupplier);
            });
                    /*System.err.println("BEGIN "+observer);
                    for (Exception e : e.getOrDefault(observer, Collections.emptyList()))
                        e.printStackTrace();
                    System.err.println("END "+observer);*/
        }
    }

    // TODO ideiglenesen publikus
    public boolean addObserver(ObserverCollection observer) {
        if (observers == null) {
            if (obs1 == null) {
                obs1 = observer;
                return true;
            } else if (obs1 == observer) {
                return false;
            } else if (obs2 == null) {
                obs2 = observer;
                return true;
            } else if (obs2 == observer) {
                return false;
            } else if (obs3 == null) {
                obs3 = observer;
                return true;
            } else if (obs3 == observer) {
                return false;
            } else {
                observers = new HashSet<>();
                observers.add(obs1);
                observers.add(obs2);
                observers.add(obs3);
                observers.add(observer);
                obs1 = null;
                obs2 = null;
                obs3 = null;
                return true;
            }
        } else {
            return observers.add(observer);
        }

        //if (observers.size() > 1000)
        //System.out.println("." + (observerSet == null ?
        //        obs2 == null ? 1 : obs3 == null ? 2 : 3 : observerSet.size()));

        // e.computeIfAbsent(observer, __ -> new ArrayList<>()).add(new Exception(toString()));
    }

    // TODO ideiglenesen publikus, DynamicElementDatanak kell
    //      valszeg publikusnak is kell maradnia, de legalább akkor kerüjön egy külön osztályba, hogy ne
    //      legyen ilyennel szemetelve az Observable API
    public void removeObserver(ObserverCollection c, int i) {
        if (observers == null) {
            // assert this.firstObserver == observer; firstObserver = null;
            if (obs1 == c) {
                if (obs3 != null) {
                    obs1 = obs3;
                    obs3 = null;
                } else {
                    obs1 = obs2;
                    obs2 = null;
                }
            } else if (obs2 == c) {
                obs2 = obs3;
                obs3 = null;
            } else if (obs3 == c) {
                obs3 = null;
            } else {
                throw new RuntimeException(c + " not in observers (" + obs1 + ", " + obs2 + ", " + obs3 + ")");
            }
        } else {
            if (!observers.remove(c))
                throw new RuntimeException(c + " not in observers (" + observers + ")");
        }
    }
}
