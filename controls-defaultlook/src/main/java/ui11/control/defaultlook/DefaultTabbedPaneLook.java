package ui11.control.defaultlook;

import ui11.Widget;
import ui11.control.TabbedPane;
import ui11.control.TabbedPane.Tab;
import ui11.control.TabbedPane.TabSide;
import ui11.input.gesture.ClickListener;
import ui11.layout.multichild.BorderLayout;
import ui11.layout.multichild.BorderLayout.Side;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.singlechild.Align;
import ui11.observable.MutableObservable;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.IntStream;

import static ui11.css.CSSClassTag.cssClass;
import static ui11.graphics.Empty.empty;

public class DefaultTabbedPaneLook extends Widget {

    private final TabbedPane tabbedPane;

    @Remember private MutableObservable<Integer> selectedTabIndex;

    public DefaultTabbedPaneLook(TabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    @Override
    protected void initState() {
        selectedTabIndex = MutableObservable.withInitial(-1/* TODO , value -> {
        if (value < 0 && !tabs.isEmpty() || value >= tabs.size())
            throw new IllegalArgumentException();
    }*/);
    }

    @Override
    protected Widget build() {
        TabSide tabSide = tabbedPane.side();
        List<Tab> tabs = tabbedPane.tabs();

        // ez nem look hanem
        if (tabs.isEmpty())
            selectedTabIndex.set(-1);
        else if (selectedTabIndex.get() == -1) {
            int initiallySelectedTabIndex = tabbedPane.initiallySelected();
            selectedTabIndex.set(initiallySelectedTabIndex == -1 ?
                    tabs.isEmpty() ? -1 : 0 : initiallySelectedTabIndex);
        } else if (selectedTabIndex.get() >= tabs.size())
            selectedTabIndex.set(tabs.size() - 1);

        int originalSelectedTab = selectedTabIndex.get();
        int selectedTabIndex = originalSelectedTab;
        //System.out.println("tabbedpanelook " + selectedTabIndex);
        Tab selectedTab;
        while (true) {
            selectedTab = selectedTabIndex == -1 ? null : tabs.get(selectedTabIndex);
            if (selectedTab == null || selectedTab.enabled())
                break;
            else {
                selectedTabIndex = (selectedTabIndex + 1) % tabs.size();
                if (selectedTabIndex == originalSelectedTab) {
                    selectedTabIndex = -1;
                    selectedTab = null;
                    break;
                }
            }
        }
        if (selectedTabIndex != originalSelectedTab)
            this.selectedTabIndex.set(selectedTabIndex);

        Widget selectedTabContent = selectedTab == null ? empty() : selectedTab.content();
        selectedTabContent = cssClass(
                "tabbed-pane-content", "tabbed-pane-content-" + tabSide,
                selectedTabContent);

        // TODO minek kell a collectorba a kaszt?

        LinearLayout tabHeaders = IntStream.range(0, tabs.size()).
                mapToObj(i -> new TabHeader(this, i)).
                collect((Collector<Widget, ?, LinearLayout>) switch (tabSide) {
                    case TOP, BOTTOM -> LinearLayout.toRow();
                    case LEFT, RIGHT -> LinearLayout.toColumn();
                });
        Widget tabHeadersWrapped = cssClass(
                "tab-headers", "tab-headers-" + tabSide, tabHeaders);

        Side side = switch (tabSide) {
            case TOP -> Side.TOP;
            case BOTTOM -> Side.BOTTOM;
            case LEFT -> Side.LEFT;
            case RIGHT -> Side.RIGHT;
        };
        return new BorderLayout().
                center(selectedTabContent).
                with(side, tabHeadersWrapped);
    }

    static final class TabHeader extends Widget {

        private final DefaultTabbedPaneLook l;
        private final int tabIndex;

        TabHeader(DefaultTabbedPaneLook l, int tabIndex) {
            this.l = l;
            this.tabIndex = tabIndex;
        }

        @Override
        protected Widget build() {
            TabbedPane tabbedPane = l.tabbedPane;
            Tab tab = tabbedPane.tabs().get(tabIndex);
            boolean active = l.selectedTabIndex.get() == tabIndex;
            System.out.println("th " + tab.title() + ": " + l.selectedTabIndex.snoop() + " vs " + tabIndex);
            Widget c = Align.center(tab.title());
            if (active)
                c = cssClass("tab-button-active", c);
            if (!tab.enabled())
                c = cssClass("tab-button-disabled", c);
            return new ClickListener(c, () -> {
                if (tab.enabled())
                    l.selectedTabIndex.set(tabbedPane.tabs().indexOf(tab));
            });
        }
    }
}

