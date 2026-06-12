package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.MultiSlot;
import ui11.geom.Axis;
import ui11.geom.Length;
import ui11.layout.multichild.Grid;
import ui11.layout.multichild.Grid.Item;
import ui11.layout.multichild.Grid.TrackSettings;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMLayoutPeerBase;

import java.util.*;
import java.util.Map.Entry;

public class DOMGridPeer extends DOMLayoutPeerBase {

    private static final String CLASS_GRID_CONTAINER = "gP";

    private final Grid grid;

    @Inject private MultiSlot<GridItemKey> slots;

    @Remember private BitSet goneItems;

    public DOMGridPeer(Grid grid) {
        super(false, true);
        this.grid = grid;
    }

    private record GridItemKey(int col, int row, int overlayIndex) {}

    @Override
    protected void initState() {
        goneItems = new BitSet();
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(CLASS_GRID_CONTAINER);
    }

    @Override
    protected List<? extends HTMLElement> children() {
        Grid e = grid;
        HTMLElement htmlElement = elem();

        List<HTMLElement> childrenPeers = new ArrayList<>();
        List<Item> items = e.items();
        goneItems.clear();

        record GridPos(int col, int row) {}
        Map<GridPos, Integer> overlayCounts = new HashMap<>();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            int overlayIndex = overlayCounts.compute(new GridPos(item.col(), item.row()),
                    (p, j) -> j == null ? 0 : j + 1);
            GridItemKey key = new GridItemKey(item.col(), item.row(), overlayIndex);
            DOMElementHolder childH = peerOf(slots.get(key), item.widget());

            if (childH.isHidden())
                // TODO ez így hibás, mert hozzá kéne adni a DOM-hoz valahová
                goneItems.set(i);
            else
                childrenPeers.add(childH.element());
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

        StringBuilder gridTemplateRows = new StringBuilder();
        if (rowCount == 0)
            gridTemplateRows.append("0"); // csak hogy ne legyen érvénytelen a css
        else
            for (int i = 0; i < rowCount; i++) {
                TrackSettings trackSettings = rows.get(i);

                if (trackSettings != null && trackSettings.size().isPresent()) {
                    gridTemplateRows.append(" ").append(trackSettings.size().get());
                    // TODO ilyenkor a weight nincs figyelembe véve
                    continue;
                }

                // ez az fr nem helyes működés, de egyelőre nem tudom hogy mit lehetne csinálni helyettük,
                // mert CSS gridben nem lehet egyszerre figyelembe venni az intrinsic size-ot
                // és maradék terület valahányad részét.

                // hogy a minmax(0, ...) miért kell, azt nem tudom, meg kéne nézni a specben.
                // https://css-tricks.com/preventing-a-grid-blowout/
                // ha nincs benne, akkor az oszlopok túlcsordulnak ha hosszú szó van bennük (akkor is, ha
                // word-wrap:break-word be van kapcsolva). ez meg valamiért megoldja. soroknál nem tudom, hogy számít-e
                // valamit, de valszeg igen.
                // DesktopHUD-ba ágyazott cseten jött elő a probléma, ha nincs itt ez.

                if (trackSettings != null && trackSettings.weight() != 0)
                    gridTemplateRows.append(" minmax(0, ").append(trackSettings.weight()).append("fr)");
                else
                    gridTemplateRows.append(" minmax(0, auto)");
            }
        htmlElement.getStyle().setProperty("grid-template-rows", gridTemplateRows.toString());

        StringBuilder gridTemplateColumns = new StringBuilder();
        if (colCount == 0)
            gridTemplateColumns.append("0"); // ld. fenti komment
        else
            for (int i = 0; i < colCount; i++) {
                TrackSettings trackSettings = cols.get(i);

                if (trackSettings != null && trackSettings.size().isPresent()) {
                    gridTemplateColumns.append(" ").append(trackSettings.size().get());
                    // TODO ilyenkor a weight nincs figyelembe véve
                    continue;
                }

                // ld. fenti kommentek
                if (trackSettings != null && trackSettings.weight() != 0)
                    gridTemplateColumns.append(" minmax(0, ").append(trackSettings.weight()).append("fr)");
                else
                    gridTemplateColumns.append(" minmax(0, auto)");
            }
        htmlElement.getStyle().setProperty("grid-template-columns", gridTemplateColumns.toString());

        Length gap = e.gap();
        if (gap.isZero())
            htmlElement.getStyle().removeProperty("gap");
        else
            htmlElement.getStyle().setProperty("gap", lengthToCSS(gap));

        int j = 0;
        for (int i = 0; i < e.items().size(); i++) {
            if (goneItems.get(i))
                continue;

            Item item = e.items().get(i);

            // css-ben az első line 1-es sorszámú, nem 0-ás
            int rowStart = 1 + item.row();
            int colStart = 1 + item.col();
            int rowEnd = rowStart + item.rowspan();
            int colEnd = colStart + item.colspan();
            childrenPeers.get(j++).getStyle().setProperty("grid-area",
                    rowStart + "/" + colStart + "/" + rowEnd + "/" + colEnd);
        }

        return childrenPeers;
    }
}
