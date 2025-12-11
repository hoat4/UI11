package ui11.control;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.text.Text;

import java.util.List;

// TODO selectedTab kívülről módosítható legyen
public final class TabbedPane extends SubstitutedWidget {

    private final List<Tab> tabs;
    private final TabSide side;

    public TabbedPane(List<Tab> tabs, TabSide side) {
        this.tabs = tabs;
        this.side = side;
    }

    public TabbedPane(Tab... tabs) {
        this(List.of(tabs), TabSide.TOP);
    }

    public int initiallySelected() {
        for (int i = 0; i < tabs.size(); i++)
            if (tabs.get(i).isInitiallySelected)
                return i;
        return -1;
    }

    public TabbedPane withSide(TabSide tabSide) {
        return new TabbedPane(tabs, tabSide);
    }

    public List<Tab> tabs() {
        return tabs;
    }

    public TabSide side() {
        return side;
    }

    public enum TabSide {
        TOP, BOTTOM, LEFT, RIGHT
    }

    /**
     * Ha egy tab nem enabled, akkor nem szabad aktívnak maradnia hozzá tartozó elemnek (mert pl. lehet hogy olvas olyan
     * IV-t ami elérhetősége ugyanattól a feltételtől függ mint a tab enabledsége)
     */
    public record Tab(Widget title, Widget content, boolean enabled, boolean isInitiallySelected) {

        public Tab(String title, Widget content) {
            this(new Text(title), content);
        }

        public Tab(Widget title, Widget content) {
            this(title, content, true, false);
        }

        public Tab enabled(boolean value) {
            return new Tab(title, content, value, isInitiallySelected);
        }

        public Tab initiallySelected() {
            return new Tab(title, content, enabled, true);
        }

        @Override
        public boolean equals(Object object) {
            return this == object;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }
}
