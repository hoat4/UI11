package ui11.designtoken.model;

public abstract class Element {

    public LocationInSource locationInSource;

    public static class LocationInSource {

        public String filename;
        public int col;
        public int row;
    }

    public static abstract class ValueElement extends Element {
    }
}
