package ui11.i18n.editor;

import ui11.Key;
import ui11.Widget;
import ui11.color.Color;
import ui11.control.EditablePlainText;
import ui11.control.PlainTextEditor;
import ui11.i18n.LocalizableTextEditingContext;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Padding;
import ui11.layout.singlechild.PassiveSize;
import ui11.layout.singlechild.Scrollable;
import ui11.text.Text;

import static ui11.decoration.Background.withBackground;
import static ui11.geom.Length.px;
import static ui11.layout.multichild.Grid.grid;
import static ui11.layout.singlechild.FixedSize.withWidth;
import static ui11.text.TextModifiers.withFontSize;
import static ui11.text.TextModifiers.withLineWrapping;

public class LiveLocalizationEditor extends Widget {

    private final LocalizableTextEditingContext context;

    public LiveLocalizationEditor(LocalizableTextEditingContext context) {
        this.context = context;
    }

    @Override
    protected Widget build() {
        Widget tableContent = Padding.allSides(px(8),
                grid(2, grid -> {
                    context.localizationResources.forEach((name, valueObs) -> {
                        grid.add(
                                Align.leftCenter(
                                        withFontSize(10d,
                                                withLineWrapping(new Text(name))
                                        )
                                ).withKey(Key.of("residLabel", name))
                        );
                        grid.add(
                                new PlainTextEditor(new EditablePlainText(valueObs)).
                                        withKey(Key.of("valueField", name))
                        );
                    });
                    grid.setGap(px(4));
                    grid.columnWeights(1, 2);
                })
        );
        return withBackground(Color.sRGB(1, 216 / 255.0, 199 / 255.0), withWidth(px(450),
                new PassiveSize(new Scrollable(
                        Align.top(tableContent)
                ))
        ));
    }
}
