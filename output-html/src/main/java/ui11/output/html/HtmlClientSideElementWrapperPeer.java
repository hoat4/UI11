/*
package ui11.output.html;

import com.flyordie.code.js.JSCodeMap.JSClassMap.MethodNameAndType;
import ui11.output.html.util.UTF8Writer;
import ui11.Node;

import java.lang.constant.MethodTypeDesc;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class HtmlClientSideElementWrapperPeer extends HtmlPeer {

    private static final byte[] EMBED_BEGIN = "<script src=\"".getBytes(US_ASCII);
    private static final byte[] EMBED_MID = "\"></script><div id=\"ce".getBytes(US_ASCII);
    private static final byte[] EMBED_END = "\"></div>".getBytes(US_ASCII);
    private static final int BEFORE_ID = UTF8Writer.makeIntLE(",\"ce");
    private static final int AFTER_ID = UTF8Writer.makeIntLE("\");");
    public static final MethodNameAndType showAt_desc = new MethodNameAndType(
            "showAt",
            MethodTypeDesc.of(void.class.describeConstable().get(),
                    Node.class.describeConstable().get(),
                    String.class.describeConstable().get()));

    @Model private ClientSideElementWrapper clientSideElementWrapper;

    @Override
    protected void update() {
    }

    @Override
    public void writeImpl(UTF8Writer out, HTMLElementRequirements req) {
        throw new RuntimeException("TODO");
    }

    /*
    @Override
    public HTMLLayoutElement makeHTMLElement() {
        return new HTMLLayoutElement(elementFactory()) {

            {
                children = this;
            }

            @Override
            public void writeChildren(UTF8Writer out) {
                ClientSideElementInfo cseInfo =
                        inherited(ClientSideElementURLFactory.class).forElement(e.element());

                int id = ((OutputContext) out).jsFooter.size();

                out.writeBytes(EMBED_BEGIN);
                HTMLOutputUtil.attrValue(out, cseInfo.scriptURL());
                out.writeBytes(EMBED_MID);
                out.writeInt(id);
                out.writeBytes(EMBED_END);
                ((OutputContext) out).jsFooter.add(footerOut -> {
                    JSCodeMap codeMap = cseInfo.codeMap();
                    footerOut.write(codeMap.clazz(DOMEnvironment.class.getName()).
                            methodNames.get(showAt_desc));
                    footerOut.writeByte('(');
                    JSSerialization.serialize(e.element(), cseInfo.codeMap(), footerOut);
                    footerOut.write4BytesLE(BEFORE_ID);
                    footerOut.writeInt(id);
                    footerOut.write4BytesLE(AFTER_ID);
                });
            }
        };
    }
}
*/
