package ui11.provide;

import javax.annotation.Nullable;

// TODO ennek a kezelését (főleg Element.retrieveNonDirectIVValue-ben) alaposabban át kéne gondolni

public interface DynamicProvider {

    // TODO kezelni kéne az ennek a végrehajtása során keletkezett subscribeokat
    // TODO ez minden descendant Element refreshjekor folyton újra meghívódik
    @Nullable
    <T> T provideOrNull(Class<T> type);
}
