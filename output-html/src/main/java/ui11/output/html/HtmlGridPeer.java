package ui11.output.html;

import ui11.Widget;
import ui11.output.html.util.UTF8Writer;
import ui11.output.html.HTMLElementRequirements.StyleProp;
import ui11.geom.Axis;
import ui11.geom.Length;
import ui11.layout.multichild.Grid;
import ui11.layout.multichild.Grid.Item;
import ui11.layout.multichild.Grid.TrackSettings;

import java.util.*;
import java.util.Map.Entry;

import static ui11.output.html.HTMLOutputUtil.bytes;

class HtmlGridPeer extends HtmlPeerBase {

    private static final byte[] CLASS = bytes(" class=\"gP");
    private static final byte[] STYLE_GRID_TEMPLATE_ROWS = bytes("\" style=\"grid-template-rows:");
    private static final byte[] GRID_TEMPLATE_COLUMNS = bytes(";grid-template-columns:");
    private static final byte[] GAP = bytes(";gap:");
    private static final byte[] GRID_AREA = bytes("grid-area:");

    private final Grid grid;

    public HtmlGridPeer(Grid grid) {
        this.grid = grid;
    }

    private record GridItemKey(int row, int col, int overlayIndex) {}

    private record GridPos(int col, int row) {}

    @Override
    protected Widget build() {
        Map<GridItemKey, Widget> widgets = new HashMap<>();
        Map<GridPos, Integer> overlayCounts = new HashMap<>();
        for (Item item : grid.items()) {
            int overlayIndex = overlayCounts.compute(new GridPos(item.col(), item.row()),
                (p, j) -> j == null ? 0 : j + 1);
            GridItemKey key = new GridItemKey(item.col(), item.row(), overlayIndex);
            widgets.put(key, item.widget());
        }
        return makePeers(widgets, peers -> {
            return htmlElement((out, req) -> writeImpl(out, req, peers));
        });
    }

    private void writeImpl(UTF8Writer out, HTMLElementRequirements req,
                           Map<GridItemKey, ? extends WriteableHtmlElement> peers) {
        Grid e = grid;

        List<WriteableHtmlElement> childrenPeers = new ArrayList<>();
        List<Item> items = e.items();
        BitSet goneItems = new BitSet();

        Map<GridPos, Integer> overlayCounts = new HashMap<>();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            int overlayIndex = overlayCounts.compute(new GridPos(item.col(), item.row()),
                (p, j) -> j == null ? 0 : j + 1);
            GridItemKey key = new GridItemKey(item.col(), item.row(), overlayIndex);
            WriteableHtmlElement childH = peers.get(key);

            // TODO if (childH.isHidden())
            //     // TODO ez így hibás, mert hozzá kéne adni a DOM-hoz valahová
            //    goneItems.set(i);
            // else
            childrenPeers.add(childH);
        }

        Map<Integer, TrackSettings> rows = e.tracks(Axis.VERTICAL);
        Map<Integer, TrackSettings> cols = e.tracks(Axis.HORIZONTAL);

        int childCount = e.items().size();
        int rowCount = 0, colCount = 0;
        for (int i = 0; i < childCount; i++) {
            Item item = e.items().get(i);

            if (item.row() + item.rowspan() > rowCount)
                rowCount = item.row() + item.rowspan();
            if (item.col() + item.colspan() > colCount)
                colCount = item.col() + item.colspan();
        }

        int nonZeroWeightCount_rows = 0, nonZeroWeightCount_cols = 0;
        for (Entry<Integer, TrackSettings> rowEntry : e.tracks(Axis.VERTICAL).entrySet())
            if (rowEntry.getKey() < rowCount && rowEntry.getValue().weight() != 0)
                nonZeroWeightCount_rows++;
        for (Entry<Integer, TrackSettings> colEntry : e.tracks(Axis.HORIZONTAL).entrySet())
            if (colEntry.getKey() < colCount && colEntry.getValue().weight() != 0)
                nonZeroWeightCount_cols++;

        HTMLOutputUtil.beginLayoutDisplayModeElement(out, req);
        out.writeBytes(CLASS);
        HTMLOutputUtil.writeClassNames(req, out);

        out.writeBytes(STYLE_GRID_TEMPLATE_ROWS);
        if (rowCount == 0)
            out.append("0"); // csak hogy ne legyen érvénytelen a css
        else
            for (int i = 0; i < rowCount; i++) {
                TrackSettings trackSettings = rows.get(i);
                // TODO trackSettings.size() figyelembe vétele

                // ez hülyeségek, de egyelőre nem tudom hogy mit lehetne csinálni helyettük,
                // mert CSS gridben nem lehet egyszerre figyelembe venni az intrinsic size-ot
                // és maradék terület valahányad részétű
                if (nonZeroWeightCount_rows == 1 && trackSettings.weight() != 0)
                    out.append(" 1fr");
                else
                    out.append(" auto");
            }
        out.writeBytes(GRID_TEMPLATE_COLUMNS);
        if (colCount == 0)
            out.append("0"); // ld. fenti komment
        else
            for (int i = 0; i < colCount; i++) {
                TrackSettings trackSettings = cols.get(i);
                // ld. fenti kommentek
                if (nonZeroWeightCount_cols == 1 && trackSettings.weight() != 0)
                    out.append(" 1fr");
                else
                    out.append(" auto");
            }

        Length gap = e.gap();
        if (!gap.isZero()) {
            out.writeBytes(GAP);
            HTMLOutputUtil.writeLength(gap, out);
        }
        out.writeByte(';');

        HTMLOutputUtil.writePassedStyleProps(req, out);
        out.writeBytes('"', '>');

        int i = 0;
        for (Item item : e.items()) {
            if (goneItems.get(i))
                continue;

            HTMLElementRequirements req2 = new HTMLElementRequirements();
            req2.styleProps = new StyleProp(req2.styleProps) {
                @Override
                public void write(UTF8Writer out) {
                    // css-ben az első line 1-es sorszámú, nem 0-ás
                    int rowStart = 1 + item.row();
                    int colStart = 1 + item.col();
                    int rowEnd = rowStart + item.rowspan();
                    int colEnd = colStart + item.colspan();
                    out.writeBytes(GRID_AREA);
                    HTMLOutputUtil.writeInt(rowStart, out);
                    out.writeByte('/');
                    HTMLOutputUtil.writeInt(colStart, out);
                    out.writeByte('/');
                    HTMLOutputUtil.writeInt(rowEnd, out);
                    out.writeByte('/');
                    HTMLOutputUtil.writeInt(colEnd, out);
                }
            };
            childrenPeers.get(i++).write(out, req2);
        }

        HTMLOutputUtil.endLayoutDisplayModeElement(out, req);
    }
}
