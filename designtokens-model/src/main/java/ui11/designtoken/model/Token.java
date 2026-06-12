package ui11.designtoken.model;

import ui11.designtoken.model.tokenvalue.TokenValue;

public final class Token<V extends TokenValue> extends Node {

    public ValueOrRef<V> value;
}
