package ui11.layout.impl;

import ui11.Widget;
import ui11.layout.protocol.BoxLayoutResult;

public class PreferredSizeIsMinimum extends Widget {

    public static final PreferredSizeIsMinimum INSTANCE = new PreferredSizeIsMinimum();

    @Inject private BoxLayoutResult.SizeRequest[] sizeRequests;

    private PreferredSizeIsMinimum() {
    }

    @Override
    protected Widget build() {
        if (sizeRequests.length == 0)
            throw new RuntimeException("P b sR 0");

        BoxLayoutResult.SizeRequest r = sizeRequests[0];
        Widget w = r.createResponse(new BoxLayoutResult.OfChosenSize(r.constraints().min()));
        for (int i = 1; i < sizeRequests.length; i++) {
            r = sizeRequests[i];
            w = r.createResponse(new BoxLayoutResult.OfChosenSize(r.constraints().min()), w);
        }
        return w;
    }
}
