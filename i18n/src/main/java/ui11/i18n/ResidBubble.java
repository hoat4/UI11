package ui11.i18n;

import ui11.Key;
import ui11.Widget;
import ui11.color.Color;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Alignment;
import ui11.layout.singlechild.PassiveSize;
import ui11.text.Text;

import static ui11.css.CSSClassTag.cssClass;
import static ui11.decoration.Background.withBackground;
import static ui11.graphics.effect.Overlay.overlay;

// TODO ez live-localization-editor modulba tartozna, nem ide
class ResidBubble extends Widget {

    private final String resid;
    private final Widget content;

    @Inject(required = false) private LocalizableTextEditingContext editingContext;

    @Remember private Key contentKey;

    public ResidBubble(String resid, Widget content) {
        this.resid = resid;
        this.content = content;
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
    }

    @Override
    protected Widget build() {
        if (editingContext == null || !editingContext.isEditing.get())
            return content.withKey(contentKey);

        // ideiglenesen CSS, mert a CSS-ben felülírt boldot meg egyebeket nem tudjuk felülírni, ha DOMPeerBase nem
        // tud róla, mert CSS-ben állították
        Widget residLabel = cssClass("resid-label", new Text(resid));
        return overlay(
                withBackground(Color.YELLOW.withAlpha(0.3), content.withKey(contentKey)),
                Align.centerBottom(
                        new PassiveSize(
                                new Align(
                                        withBackground(Color.YELLOW, residLabel),
                                        Alignment.CENTER_TOP,
                                        true
                                )
                        )
                )
        );
    }
}
