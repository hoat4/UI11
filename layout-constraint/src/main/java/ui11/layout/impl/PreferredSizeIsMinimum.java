package ui11.layout.impl;

import ui11.Expose;
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
        Widget w = new Expose<>(r, new BoxLayoutResult.OfChosenSize(r.constraints().min()));
        for (int i = 1; i < sizeRequests.length; i++) {
            r = sizeRequests[i];
            BoxLayoutResult peer = new BoxLayoutResult.OfChosenSize(r.constraints().min());
            w = new Expose<>(r, peer, w);
        }
        return w;
    }
}
