package ui11.geom;

import java.util.ArrayList;
import java.util.List;

class PathBounds {
    static Rect computePathBounds(Path path) {
        List<Vec2> points = new ArrayList<>();
        for (Path.PathElement e : path.items()) {
            switch (e) {
                // TODO lehetséges olyan, hogy egymás után jön több moveto?
                //      ha igen, az beletartozik boundsba?
                case Path.MoveTo(Vec2 p) -> points.add(p);
                case Path.LineTo(Vec2 p) -> points.add(p);
                case Path.Close() -> {
                }
                default -> {
                    throw new RuntimeException("TODO " + e);
                }
            }
        }
        return Rect.of(points.toArray(Vec2[]::new));
    }
}
