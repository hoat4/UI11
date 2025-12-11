package ui11.geom;

import java.util.Objects;

/**
 * Egy két dimenziós pont olyan ábrázolása, mely nem koordináta-rendszerhez kötött, hanem a koordinátáinak
 * megismeréséhez meg kell adnia a hívónak, hogy melyik {@link CoordinateSpace koordináta-rendszerben} szeretné megkapni
 * az értéket.
 * <p>
 * Ez például hasznos egéresemények feldolgozásnál, ahol minden résztvevő elem ugyanazt az {@linkplain Location}
 * példányt kaphatja meg, nem kell folyton konvertálgatni a koordináta-rendszerek közt.
 */
public final class Location {

    private final CoordinateSpace coordinateSpace;
    private final Vec4 p;

    public Location(CoordinateSpace coordinateSpace, Vec2 p) {
        Objects.requireNonNull(coordinateSpace);
        this.coordinateSpace = coordinateSpace;
        this.p = new Vec4(p.x(), p.y(), 0, 1);
    }

    /**
     * Megadja, hogy az adott koordináta-rendszerben ennek a pontnak mik a koordinátái.
     */
    public Vec2 in(CoordinateSpace coordinateSpace) {
        Objects.requireNonNull(coordinateSpace);
        if (coordinateSpace.base != this.coordinateSpace.base)
            throw new IllegalArgumentException();
        if (coordinateSpace.equals(this.coordinateSpace))
            return p.to2D();
        Vec4 v = this.coordinateSpace.transformationTo(coordinateSpace).mul(new Vec4(p.x(), p.y(), 0, 1));
        return v.to2D();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;
        return coordinateSpace.base.equals(location.coordinateSpace.base) &&
                coordinateSpace.transformationToBase.mul(p).equals(
                        location.coordinateSpace.transformationToBase.mul(location.p));

        // p.equals(location.in(coordinateSpace)) nem lenne konzisztens a hashCodedal kerekítési hibák miatt
    }

    @Override
    public int hashCode() {
        int result = coordinateSpace.base.hashCode();
        result = 31 * result + coordinateSpace.transformationToBase.mul(p).hashCode();
        return result;
    }

    /**
     * Két dimenziós koordinátarendszer, amely meg tudja mondani, hogy egy adott pontot hogy lehet áttranszormálni egy
     * másik koordináta rendszerbe. Egyik koordináta rendszer egy affin transzformációval származtatható egy másikból.
     *
     * @param transformationToBase Megadja, hogy milyen transzformációval tudunk egy pontot ebből a
     *                             koordináta-rendszerből a {@link #base bázis} koordináta-rendszerbe transzformálni.
     */
    public record CoordinateSpace(CoordinateSpaceRoot base, Mat4 transformationToBase) {

        /**
         * Megadja, hogy milyen transzformációval tudunk egy pontot ebből a koordináta-rendszerből a megadott másik
         * koordináta-rendszerbe transzformálni. A két koordináta-rendszernek a bázisának meg kell egyeznie.
         */
        public Mat4 transformationTo(CoordinateSpace other) {
            Objects.requireNonNull(other);
            if (other.equals(this))
                return Mat4.IDENTITY;
            if (other.base != this.base)
                throw new IllegalArgumentException();
            return other.transformationToBase().inverseOrThrow().mul(transformationToBase());
        }

        /**
         * Létrehoz egy másik koordináta-rendszert, melynek pontjait a paraméterben megadott transzformációval lehet
         * transzformálni a jelenlegi koordináta-rendszerbe.
         */
        public CoordinateSpace withTransformation(Mat4 transformation) {
            return new CoordinateSpace(base, transformationToBase.mul(transformation));
        }
    }

    public static class CoordinateSpaceRoot {
        public final CoordinateSpace origin = new CoordinateSpace(this, Mat4.IDENTITY);
    }
}
