package ui11;

// for now, only one peer creation request at a time
final class PeerCreationRequestCollection {

    public final PeerCreationRequest<?> request;

    public PeerCreationRequestCollection(PeerCreationRequest<?> request) {
        this.request = request;
    }
}
