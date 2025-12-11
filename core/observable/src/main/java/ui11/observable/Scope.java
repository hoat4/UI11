package ui11.observable;

public interface Scope {

    void onClose(Runnable closeListener);

    static Scope global() {
        class Holder {
            static final Scope GLOBAL_SCOPE = new Scope() {
                @Override
                public void onClose(Runnable cl) {
                }

                @Override
                public String toString() {
                    return "Global Scope";
                }
            };
        }
        return Holder.GLOBAL_SCOPE;
    }
}
