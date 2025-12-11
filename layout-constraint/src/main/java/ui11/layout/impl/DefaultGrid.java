/*
package ui11.layout.impl;

import ui11.observable.ObservableList;
import ui11.observable.ObservableMap;
import ui11.observable.ReadableObservable;
import ui11.Node;
import ui11.Peer;
import ui11.geom.Axis;
import ui11.layout.multichild.AbstractGrid;
import ui11.layout.multichild.AbstractGrid.InsetWeights;
import ui11.layout.multichild.AbstractGrid.Item;
import ui11.layout.multichild.AbstractGrid.TrackSettings;
import ui11.layout.Insets;
import ui11.layout.impl.DefaultGridLayout.GridElement;
import ui11.geom.Length;

import java.util.List;
import java.util.Map;

// TODO childek törlése elements törlésekor

public class DefaultGrid extends Peer<AbstractGrid> {

    final List<GridElement> elements = new ObservableList<>();
    final Map<Integer, TrackSettings> columnSettings = new ObservableMap<>();
    final Map<Integer, TrackSettings> rowSettings = new ObservableMap<>();

    final ReadableObservable<Insets> margin = Insets::zero; // fromModelRO(AbstractGrid::margin);
    final ReadableObservable<Length> gap = fromModelRO(AbstractGrid::gap);
    final ReadableObservable<InsetWeights> marginGrow = () -> InsetWeights.ZERO;// fromModelRO(AbstractGrid::marginGrow);
    final ReadableObservable<Boolean> ignorePrefSizes = fromModelRO(AbstractGrid::ignorePrefSizes);
    final ReadableObservable<Axis> orientationBias = fromModelRO(AbstractGrid::orientationBias);

    @Content final DefaultGridLayout l = new DefaultGridLayout(this);

    @Update
    void updateElements() {
        List<Item> items = model().items();
        List<GridElement> prevElements = List.copyOf(elements);
        elements.clear();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            Node prev = i < prevElements.size() ? prevElements.get(i).e : null;
            elements.add(new GridElement(toElement(item.element(), prev),
                    item.col(), item.row(), item.colspan(), item.rowspan()));
        }

        columnSettings.clear();
        columnSettings.putAll(model().tracks(Axis.HORIZONTAL));
        rowSettings.clear();
        rowSettings.putAll(model().tracks(Axis.VERTICAL));
    }
}
*/