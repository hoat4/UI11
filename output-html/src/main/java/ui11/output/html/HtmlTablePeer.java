package ui11.output.html;

import ui11.Widget;
import ui11.control.Table;
import ui11.control.Table.Column;
import ui11.output.html.util.UTF8Writer;

import java.util.ArrayList;
import java.util.List;

import static ui11.output.html.HTMLOutputUtil.bytes;

public class HtmlTablePeer<T> extends HtmlPeerBase {

    private static final byte[] THEAD = bytes("><thead><tr>");
    private static final byte[] THEAD_END = bytes("</tr></thead>\n");
    private static final byte[][] TH_BEGIN = {bytes("<th class=l>"), bytes("<th class=c>"), bytes("<th class=r>")};
    private static final byte[] TH_END = bytes("</th>");
    private static final byte[] TR_BEGIN = bytes("<tr>");
    private static final byte[] TR_END = bytes("</tr>\n");
    private static final byte[] TABLE_END = bytes("</table>");

    private final Table<T> table;

    public HtmlTablePeer(Table<T> table) {
        this.table = table;
    }

    @Override
    protected Widget build() {
        List<Widget> cells = new ArrayList<>();
        for (T row : table.rows())
            for (Column<? super T> col : table.columns()) {
                cells.add(col.cellContent(row));
            }
        return makePeers(cells, cellPeers ->
            htmlElement((out, req) -> writeImpl(out, req, cellPeers)));
    }

    private void writeImpl(UTF8Writer out, HTMLElementRequirements req,
                           List<? extends WriteableHtmlElement> childrenPeers) {
        Table<T> e = table;

        HTMLElementType wrapper = HTMLOutputUtil.beginElementOrWrap(HTMLElementType.TABLE, out, req);
        HTMLOutputUtil.writeClassAttr(req, out);
        HTMLOutputUtil.writeStyleAttr(req, out);

        out.writeBytes(THEAD);
        for (Column<? super T> col : e.columns()) {
            out.writeBytes(TH_BEGIN[col.horizontalAlignment().ordinal()]);
            HTMLOutputUtil.writeEscapedText(out, col.title(), false /* ? */);
            out.writeBytes(TH_END);
        }
        out.writeBytes(THEAD_END);

        int i = 0;
        for (T row : e.rows()) {
            out.writeBytes(TR_BEGIN);
            for (Column<? super T> col : e.columns()) {
                // TODO horizontalAlignment?
                HTMLElementRequirements req2 = new HTMLElementRequirements();
                req2.type = HTMLElementType.TD;
                childrenPeers.get(i++).write(out, req2);
            }
            out.writeBytes(TR_END);
        }

        out.writeBytes(TABLE_END);

        if (wrapper != null)
            wrapper.writeEndElement(out);
    }
}
