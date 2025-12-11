package ui11;

import ui11.text.Text;

public class ElementIdentityPrintingWidget extends Widget {

    private static int id;

    @Override
    protected Widget build() {
        return new Text(++id);
    }
}
