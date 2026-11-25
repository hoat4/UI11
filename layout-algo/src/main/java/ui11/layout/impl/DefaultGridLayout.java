/*
package ui11.layout.impl;

import ui11.observable.InvalidationPoint;
import ui11.Node;
import ui11.geom.Axis;
import ui11.geom.Vec2;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.graphics.effect.Group.GroupBuilder;
import ui11.layout.multichild.AbstractGrid.InsetWeights;
import ui11.layout.multichild.AbstractGrid.TrackSettings;
import ui11.layout.Insets;
import ui11.layout.helper.MultiChildLayout;
import ui11.layout.protocol.Sizing;
import ui11.geom.Length;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;

// TODO childek törlése elements törlésekor

class DefaultGridLayout extends MultiChildLayout {

    private final DefaultGrid g;

    private final List<Track> columns = new ArrayList<>();
    private final List<Track> rows = new ArrayList<>();

    // ezt most nem használjuk, majd törölni lehetne, vagy inkább használatba venni
    private final InvalidationPoint invalidationPoint = new InvalidationPoint();

    DefaultGridLayout(DefaultGrid g) {
        this.g = g;
    }

    @Override
    public Sizing sizingImpl() {
        return new Sizing() {
            @Override
            public Size preferredSize() {
                assert isActive();
                invalidationPoint.subscribe();
                //System.out.println("Grid preferredSize: " + elements);
                Axis firstAxis = g.orientationBias.get();
                double w = computeTrackSizes(firstAxis, null, false, false);
                double h = computeTrackSizes(firstAxis.cross(), null, true, false);
                return Size.of(firstAxis, w, h);
            }

            @Override
            public double preferredSize(Axis axis, double crossAxisFixedLength) {
                assert isActive();
                invalidationPoint.subscribe();
                //System.out.println("Grid preferredSize " + axis);
                computeTrackSizes(axis.cross(), crossAxisFixedLength, false, false);
                return computeTrackSizes(axis, null, true, false);
            }
        };
    }

    @Override
    public void layout(Size size) {
        Rect containerBounds = Rect.of(size);
        invalidationPoint.subscribe();
        //System.out.println("Grid doLayout " + containerBounds);

        Axis firstAxis = g.orientationBias.get();
        computeTrackSizes(firstAxis, size.length(firstAxis), false, g.ignorePrefSizes.get());
        computeTrackSizes(firstAxis.cross(), size.length(firstAxis.cross()), true, g.ignorePrefSizes.get());

        Length gap = g.gap.get();
        for (GridElement ge : g.elements) {
            double w = (ge.colspan - 1) * val(gap, containerBounds.size().length(Axis.HORIZONTAL)),
                    h = (ge.rowspan - 1) * val(gap, containerBounds.size().length(Axis.VERTICAL));
            for (int i = ge.col; i < ge.col + ge.colspan && i < columns.size(); i++)
                w += columns.get(i).size;
            for (int i = ge.row; i < ge.row + ge.rowspan && i < rows.size(); i++)
                h += rows.get(i).size;

//            assert rows.get(ge.row).pos + h <= layoutSize.height() + 0.05 /* kerekítési hibák miatt * /:
//                    "bottom: " + (rows.get(ge.row).pos + h) + ", h: " + layoutSize.height() + ", " + this;

            place(ge.e, GroupBuilder.ChildPosition.of(new Rect(
                    new Vec2(columns.get(ge.col).pos, rows.get(ge.row).pos),
                    new Size(w, h)
            )));
        }
    }

    protected double computeTrackSizes(Axis axis, Double fixedSum, boolean hasCrossSizes, boolean ignorePreferredSizes) {
        final boolean ROUND = true; // ez lehetne akár egy állítható mező is. JavaFX-ben asszem snapToPixelsnek hívják.
        final boolean ENABLE_EXCEED_FIXED_SIZE = true;

        List<Track> tracks = tracks(axis);
        tracks.clear();

        Insets margin = g.margin.get();

        normalizeSpans();

        List<GridElement> sortedElements = new ArrayList<>(g.elements);
        // ha nem rendeznénk, nem lenne optimális a helykihasználás bizonyos esetekben
        sortedElements.sort(comparing(e -> e.span(axis)));

        double sum = 0; // sávok összesített szélessége, gapek nélkül

        for (var settingsEntry : g.model.get().tracks(axis).entrySet()) {
            int index = settingsEntry.getKey();
            Length size = settingsEntry.getValue().size().orElse(null);

            if (size == null)
                continue;
            if (size.rel() != 0)
                throw new UnsupportedOperationException("TODO");

            sum += track(index, axis).size = abs(size);
        }

        for (GridElement ge : sortedElements) {
            int i = ge.cellPos(axis), span = ge.span(axis);

            if (axis == Axis.VERTICAL && ge.passiveHeight)
                continue;
            //if (ge.passiveHeight)
            //    System.out.println();

            double prevSum = 0;
            for (int k = i; k < i + span; k++)
                prevSum += track(k, axis).size;

            double elementMainSize;
            if (ignorePreferredSizes)
                elementMainSize = 0;
            else if (hasCrossSizes) {
                double elementCrossSize = 0;
                int crossPos = ge.cellPos(axis.cross()), crossSpan = ge.span(axis.cross());
                for (int k = crossPos; k < crossPos + crossSpan; k++) {
                    double crossTrackSize = track(k, axis.cross()).size;
                    assert crossTrackSize >= 0;
                    elementCrossSize += crossTrackSize;
                }
                elementMainSize = sizingOf(ge.e).preferredSize(axis, elementCrossSize);
            } else
                elementMainSize = sizingOf(ge.e).preferredSize().length(axis);

            if (elementMainSize > prevSum) {
                sum += elementMainSize - prevSum;

                double weightSum = 0;
                for (int k = i; k < i + span; k++) {
                    TrackSettings trackSettings = trackSettings(k, axis);
                    if (trackSettings != null)
                        weightSum += trackSettings.weight();
                }

                double addedSpacePerTrack = (elementMainSize - prevSum) / (weightSum == 0 ? span : weightSum);
                assert addedSpacePerTrack >= 0;
                for (int k = i; k < i + span; k++) {
                    double weight;
                    if (weightSum == 0)
                        weight = 1;
                    else {
                        TrackSettings trackSettings = trackSettings(k, axis);
                        weight = trackSettings == null ? 0 : trackSettings.weight();
                    }

                    track(k, axis).size += addedSpacePerTrack * weight;
                }
            }
        }

        if (fixedSum == null) {
            Length bm = margin.begin(axis), em = margin.end(axis), gap = g.gap.get();
            int gapCount = Math.max(0, tracks.size() - 1);
            double ga = gapCount * abs(gap), gr = gapCount * gap.rel();
            double h = (abs(bm) + sum + ga + abs(em)) / (1 - bm.rel() - gr - em.rel());
            double gapVal = val(gap, h);
            double pos = val(bm, h);
            for (Track track : tracks) {
                double cSize;
                if (ROUND) {
                    double cPos = Math.ceil(pos);
                    cSize = Math.ceil(track.size);
                    h += cPos - pos + cSize - track.size;
                    pos = cPos;
                } else
                    cSize = track.size;

                track.pos = pos;
                pos += cSize + gapVal;
            }
            return ROUND ? Math.ceil(h) : h;
        }

        double beginMargin = val(margin.begin(axis), fixedSum), endMargin = val(margin.end(axis), fixedSum);
        double gap = val(g.gap.get(), fixedSum);

        // Ekkorára kell szét-/összehúzni a sávok gapek összesített méretét.
        // De mit csináljunk, ha ez negatív?
        double fixedSumWithoutGaps = fixedSum - beginMargin - endMargin -
                (tracks.isEmpty() ? 0 : (tracks.size() - 1) * gap);

        double grow = fixedSumWithoutGaps - sum; // ez lehet negatív is, ekkor grow helyett shrink

        InsetWeights marginGrow = g.marginGrow.get();
        double weightSum = marginGrow.sum(axis);
        for (int i = 0; i < tracks.size(); i++) {
            TrackSettings trackSettings = trackSettings(i, axis);
            if (trackSettings != null)
                weightSum += trackSettings.weight();
        }

        boolean equalDistribution = weightSum == 0;

        double growPerWeightUnit = grow / (equalDistribution ? tracks.size() : weightSum);

        double pos = marginGrow.begin(axis) * growPerWeightUnit + beginMargin;
        int i = 0;
        for (Track track : tracks) {
            TrackSettings trackSettings = trackSettings(i++, axis);
            double weight = equalDistribution ? 1 :
                    trackSettings == null ? 0 : trackSettings.weight();
            track.pos = pos;
            track.size += growPerWeightUnit * weight;
            if (track.size < 0) {
                if (!ENABLE_EXCEED_FIXED_SIZE)
                    throw new RuntimeException("fixed size " + fixedSum + " too small, content (" + sum + ") " +
                            "doesn't fits (track size=" + track.size + "): " + i + " " + axis + "\n" + this +
                            "\nTracks: " + tracks);
                else {
                    growPerWeightUnit += track.size / weight;
                    track.size = 0;
                }
            }
            pos += track.size + gap;
        }

        if (!ENABLE_EXCEED_FIXED_SIZE && !tracks.isEmpty()) {
            double endMargin2 = endMargin + marginGrow.end(axis) * growPerWeightUnit;
            double actualEnd = pos + endMargin2 - (tracks.isEmpty() ? 0 : gap);
            assert Math.abs(actualEnd - fixedSum) < 0.01 : "expected " + fixedSum + " but actual end is " + actualEnd + ", " + axis + ", " + this;
        }

        if (ROUND)
            for (int j = tracks.size() - 1; j >= 0; j--) {
                Track track = tracks.get(j);
                double end = track.pos + track.size;
                track.pos = Math.round(track.pos);
                track.size = Math.round(end) - track.pos; // ha nagyon kicsi (pl. < 1), akkor nem kéne kerekíteni
            }

        return fixedSum;
    }

    private void normalizeSpans() {
        int colCount = 0, rowCount = 0;
        for (GridElement e : g.elements) {
            if (e.colspan != Integer.MAX_VALUE)
                colCount = Math.max(colCount, e.col + e.colspan);
            if (e.rowspan != Integer.MAX_VALUE)
                rowCount = Math.max(rowCount, e.row + e.rowspan);
        }
        for (GridElement e : g.elements) {
            if (e.colspan == Integer.MAX_VALUE)
                e.colspan = colCount;
            if (e.rowspan == Integer.MAX_VALUE)
                e.rowspan = rowCount;
        }
    }

    protected List<Track> tracks(Axis axis) {
        return switch (axis) {
            case VERTICAL -> rows;
            case HORIZONTAL -> columns;
        };
    }

    private Track track(int i, Axis axis) {
        List<Track> tracks = tracks(axis);
        while (tracks.size() <= i) {
            tracks.add(new Track());
        }
        return tracks.get(i);
    }

    private TrackSettings trackSettings(int i, Axis axis) {
        return switch (axis) {
            case HORIZONTAL -> g.columnSettings.get(i);
            case VERTICAL -> g.rowSettings.get(i);
        };
    }

    private double val(Length len, double base) {
        return len.rel() * base + abs(len);
    }

    private double abs(Length len) {
        return len.em() * 14 + len.px();
    }

    @Override
    public String toString() {
        return "Grid" + hashCode() + " " + g.elements;
    }

    static class GridElement {

        public Node e;
        public int col;
        public int row;
        public int colspan;
        public int rowspan;
//        int z;

        boolean passiveHeight;

        GridElement(Node e, int col, int row, int colspan, int rowspan) {
            assert e != null && col >= 0 && row >= 0 && colspan >= 1 && rowspan >= 1;
            this.e = e;
            this.col = col;
            this.row = row;
            this.colspan = colspan;
            this.rowspan = rowspan;
        }

        int cellPos(Axis axis) {
            return switch (axis) {
                case HORIZONTAL -> col;
                case VERTICAL -> row;
            };
        }

        int span(Axis axis) {
            return switch (axis) {
                case HORIZONTAL -> colspan;
                case VERTICAL -> rowspan;
            };
        }

        @Override
        public String toString() {
            // TODO itt e.toShortString() volt
            return e + " (at " + col + "," + row + " " + colspan + "×" + rowspan + ")";
        }
    }

    private static class Track {

        double pos;
        double size;

        @Override
        public String toString() {
            return "Track{" + "pos=" + pos + ", size=" + size + '}';
        }
    }
}
*/