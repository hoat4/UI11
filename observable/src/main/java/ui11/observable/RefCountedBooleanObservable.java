package ui11.observable;

import ui11.observable.MutableObservable.ChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO jobb név

public class RefCountedBooleanObservable implements Observable<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(RefCountedBooleanObservable.class);
    private final MutableObservable<Boolean> acquired = MutableObservable.withInitial(false);
    private int acquiredCount;

    @Override
    public Boolean get() {
        return acquired.get();
    }

    @Override
    public EventSource<ChangeEvent<Boolean>> changes() {
        return acquired.changes();
    }

    public void trueWhile(Scope scope) {
        acquiredCount++;
        boolean[] released = {false};
        try {
            scope.onClose(() -> {
                if (released[0]) {
                    // ez csak akkor lehet, ha hibásan van implementálva a Scope
                    logger.error("Already released: " + scope);
                    return;
                }

                acquiredCount--;
                released[0] = true;
                acquired.set(acquiredCount != 0);
            });
        } catch (Error | RuntimeException e) {
            released[0] = true;
            acquiredCount--;
            throw e;
        }
        acquired.set(acquiredCount != 0);
    }

    public void trueIf(Observable<Boolean> condition, Scope scope) {
        SimpleScope[] childScope = {null};
        boolean[] first = {true};
        condition.getAndSubscribe(val -> {
            logger.trace(condition + ": " + val);
            try {
                if (val) {
                    if (childScope[0] == null) {
                        trueWhile(childScope[0] = new SimpleScope(scope));
                    } else {
                        // hibásan küldött ChangeEventet
                        logger.error("Condition was already true: " + condition);
                    }
                } else {
                    if (childScope[0] == null) {
                        if (!first[0]) {
                            // hibásan küldött ChangeEventet
                            logger.error("Condition was already false: " + condition);
                        }
                    } else {
                        childScope[0].close();
                        childScope[0] = null;
                    }
                }
            } finally {
                first[0] = false;
            }
        }, scope);
    }
}
