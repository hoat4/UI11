package ui11;

/**
 * ListenerProxyGenerator által generált osztályok ebből származnak le
 */
abstract class ListenerProxyBase<T> {

    final Element element;
    private boolean toString;

    protected ListenerProxyBase(Element element) {
        this.element = element;
    }

    @Override
    public final String toString() {
        String s = "Event Listener Proxy 0x" + Integer.toHexString(System.identityHashCode(this));
        // TODO kéne recordcomponent név is.
        //      most nem túl sokatmondó a szöveg, például ilyen egy @Content-es widget esetén:
        //      Event Listener Proxy 7219ada0 for build() of W1[i=130, r=Event Listener Proxy 7219ada0], W1[i=130, r=ui11.InterfaceProxyTest$$Lambda/0x0000020d0f0dd4c8@7626373b]

        // element.toString lehet hogy tartalmazni fogja replacedModel toStringjét, ami
        // pedig ennek a proxynak a toStringjét fogja meghívni, és ez végtelen rekurzióteredményezne.
        // ezért element.toString()-et csak akkor szabad meghívni, ha nem vagyunk már ezen ListenerProxyBase
        // toString()-jében.
        if (toString)
            return s;
        toString = true;
        try {
            // lehet hogy element nem is kéne, mert inkább csak zavarosabbá teszi
            return s + " for " + element + ", " + element.model;
        } finally {
            toString = false;
        }
    }

    @Override
    public final boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass() && element == ((ListenerProxyBase<?>) obj).element;
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode() ^ element.hashCode() * 7;
    }
}
