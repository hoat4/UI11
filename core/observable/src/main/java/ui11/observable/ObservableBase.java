package ui11.observable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ObservableBase {

    private ObserverCollection obs1, obs2, obs3;
    private int obs1i, obs2i, obs3i;
    private Map<ObserverCollection, Integer> observers;
    // TODO ideiglenesen publikus
    public long invalidationCount;

    protected void onRead() {
        final ObserverHolder h = ObserverHolder.current();
        ObserverCollection observerObserverCollection = h.obsC;
        if (observerObserverCollection != null && addObserver(observerObserverCollection, h.obsI))
            observerObserverCollection.subscribedTo(this);
    }

    protected void onWrite() {
        onWrite(null);
    }

    @SuppressWarnings("unchecked")
    void onWrite(Supplier<String> debugMessageSupplier) {
        invalidationCount++;

        if (obs1 == null && obs2 == null && obs3 == null && observers == null)
            return;

        if (observers == null) {
            ObserverCollection obs1 = this.obs1, obs2 = this.obs2, obs3 = this.obs3;
            if (obs1 != null)
                obs1.invalidate(obs1i, debugMessageSupplier);
            if (obs2 != null)
                obs2.invalidate(obs2i, debugMessageSupplier);
            if (obs3 != null)
                obs3.invalidate(obs3i, debugMessageSupplier);
        } else {
            Map.Entry<ObserverCollection, Integer>[] c = observers.entrySet().toArray(Map.Entry[]::new);
            for (Map.Entry<ObserverCollection, Integer> e : c)
                    /*System.err.println("BEGIN "+observer);
                    for (Exception e : e.getOrDefault(observer, Collections.emptyList()))
                        e.printStackTrace();
                    System.err.println("END "+observer);*/
                e.getKey().invalidate(e.getValue(), debugMessageSupplier);
        }
    }

    // ezt már ki lehetne törölni
    private void check() {
        assert obs1 == null || obs1i != 0;
        assert obs2 == null || obs2i != 0;
        assert obs3 == null || obs3i != 0;
    }

    // TODO ideiglenesen publikus
    public boolean addObserver(ObserverCollection observer, int i) {
        check();
        observer.checkObserver(i);
        assert i != 0 : observer + ", " + this;
        if (observers == null) {
            if (obs1 == null) {
                obs1 = observer;
                obs1i = i;
                return true;
            } else if (obs1 == observer) {
                return obs1i != (obs1i |= i);
            } else if (obs2 == null) {
                obs2 = observer;
                obs2i = i;
                return true;
            } else if (obs2 == observer) {
                obs2i |= i;
                return obs2i != (obs2i |= i);
            } else if (obs3 == null) {
                obs3 = observer;
                obs3i = i;
                return true;
            } else if (obs3 == observer) {
                obs3i |= i;
                return obs3i != (obs3i |= i);
            } else {
                observers = new HashMap<>();
                observers.put(obs1, obs1i);
                observers.put(obs2, obs2i);
                observers.put(obs3, obs3i);
                observers.put(observer, i);
                obs1 = null;
                obs2 = null;
                obs3 = null;
                return true;
            }
        } else {
            Integer existing = observers.get(observer);
            if (existing == null) {
                observers.put(observer, i);
                return true;
            } else {
                int existingI = existing;
                int newBS = existingI | i;
                if (newBS == existingI)
                    return false;
                else {
                    observers.put(observer, newBS);
                    return true;
                }
            }
        }

        //if (observers.size() > 1000)
        //System.out.println("." + (observerSet == null ?
        //        obs2 == null ? 1 : obs3 == null ? 2 : 3 : observerSet.size()));

        // e.computeIfAbsent(observer, __ -> new ArrayList<>()).add(new Exception(toString()));
    }

    // TODO ideiglenesen publikus, DynamicElementDatanak kell
    //      valszeg publikusnak is kell maradnia, de legalább akkor kerüjön egy külön osztályba, hogy ne
    //      legyen ilyennel szemetelve az Observable API
    public int removeObserver(ObserverCollection c, int i) {
        check();
        if (observers == null) {
            // assert this.firstObserver == observer; firstObserver = null;
            if (obs1 == c) {
                int prev = obs1i;
                if ((obs1i &= ~i) == 0) {
                    if (obs3 != null) {
                        obs1 = obs3;
                        obs1i = obs3i;
                        obs3 = null;
                    } else if (obs2 != null) {
                        obs1 = obs2;
                        obs1i = obs2i;
                        obs2 = null;
                    } else
                        obs1 = null;
                }
                return prev;
            } else if (obs2 == c) {
                int prev = obs2i;
                if ((obs2i &= ~i) == 0) {
                    if (obs3 != null) {
                        obs2 = obs3;
                        obs2i = obs3i;
                        obs3 = null;
                    } else
                        obs2 = null;
                }
                return prev;
            } else {
                assert obs3 == c : c + " not in observers (" + obs1 + ", " + obs2 + ", " + obs3 + ")";
                int prev = obs3i;
                if ((obs3i &= ~i) == 0)
                    obs3 = null;
                return prev;
            }
        } else {
            int bs = observers.get(c); // implicit null-check
            bs &= ~i;
            if (bs == 0) {
                return observers.remove(c);
            } else {
                return observers.put(c, bs);
            }
        }
    }

    // TODO ld. komment removeObservernél
    public int findObservers(ObserverCollection c) {
        check();
        if (observers == null) {
            // assert this.firstObserver == observer; firstObserver = null;
            if (obs1 == c) {
                return obs1i;
            } else if (obs2 == c) {
                return obs2i;
            } else {
                assert obs3 == c;
                return obs3i;
            }
        } else {
            return observers.get(c); // implicit null-check
        }
    }
}
