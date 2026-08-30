package ui11.layout.impl;

import ui11.Widget;
import ui11.layout.protocol.BoxLayoutResult;

public class GoneImpl extends Widget {

    public static final GoneImpl INSTANCE = new GoneImpl();

    @Inject private BoxLayoutResult.SizeRequest[] sizeRequests;

    private GoneImpl() {
    }

    @Override
    protected Widget build() {
        if (sizeRequests.length == 0)
            throw new RuntimeException("G b sR 0");

        BoxLayoutResult.SizeRequest r = sizeRequests[0];
        Widget w = r.createResponse(new BoxLayoutResult.OfGone());
        for (int i = 1; i < sizeRequests.length; i++) {
            r = sizeRequests[i];
            w = r.createResponse(new BoxLayoutResult.OfGone(), w);
        }
        return w;
    }
}
