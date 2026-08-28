package ui11.text;

import ui11.*;

import static ui11.text.TextModifiers.withLineWrapping;

public class TextErrorWidgetResolverProvider implements ResolverProvider {

    @Override
    public void configure(ResolverRegistry r) {
        r.add(ErrorWidget.class, errorWidget -> {
            Throwable throwable = errorWidget.throwable();
            // TODO kéne detektálni, ha ennek a delegatecreationje se sikerül
            return withLineWrapping(new Text(throwable.toString()));
        });
    }
}
