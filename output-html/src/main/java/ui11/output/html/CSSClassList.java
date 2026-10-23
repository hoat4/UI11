package ui11.output.html;

import ui11.provide.Provider.Mergeable;

import java.util.HashSet;
import java.util.Set;

// Provider/@Inherited-del használhandó típus. CSSClassTag van erre decomposeolva DOM backend esetén.

record CSSClassList(Set<String> classes) implements Mergeable<CSSClassList> {
    @Override
    public CSSClassList mergeWith(CSSClassList defaults) {
        if (this == CLEAR || defaults.classes.isEmpty())
            return this;
        Set<String> s = new HashSet<>(classes);
        s.addAll(defaults.classes);
        return new CSSClassList(Set.copyOf(s));
    }

    static final CSSClassList CLEAR = new CSSClassList(Set.of());
}
