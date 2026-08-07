package ui11.control;

import org.jspecify.annotations.NonNull;
import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.text.Text;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

// TODO selectedTab kívülről módosítható legyen
public final class TabbedPane extends SubstitutedWidget {

    private final List<Tab> tabs;
    private final TabSide side;

    @Inject private Key.ListKeyCache tabTitleSlots;
    @Inject private Key.ListKeyCache tabContentSlots;

    public TabbedPane(Tab... tabs) {
        this(List.of(tabs), TabSide.TOP);
    }

    private TabbedPane(@NonNull List<@NonNull Tab> tabs, @NonNull TabSide side) {
        this.tabs = List.copyOf(tabs);
        this.side = side;
    }

    @Override
    protected void initState() {
        tabTitleSlots = new Key.ListKeyCache();
        tabContentSlots = new Key.ListKeyCache();
    }

    @Override
    protected TabbedPane forSubstitution() {
        List<? extends Widget> tabTitles = tabTitleSlots.with(tabs.stream().map(Tab::title).toList());
        List<? extends Widget> tabContents = tabContentSlots.with(tabs.stream().map(Tab::content).toList());

        Tab[] slottedTabs = new Tab[tabs.size()];
        for (int i = 0; i < slottedTabs.length; i++) {
            Tab tab = tabs.get(i);
            slottedTabs[i] = new Tab(tabTitles.get(i), tabContents.get(i),
                    tab.enabled, tab.isInitiallySelected);
        }
        return new TabbedPane(List.of(slottedTabs), side);
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
    public record Tab(@NonNull Widget title, @NonNull Widget content, boolean enabled, boolean isInitiallySelected) {

        public Tab {
            Objects.requireNonNull(title);
            Objects.requireNonNull(content);
        }

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
    }
}
