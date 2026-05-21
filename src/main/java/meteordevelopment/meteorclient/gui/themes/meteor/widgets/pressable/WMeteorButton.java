/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.themes.meteor.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.render.Easing;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.Mth;

public class WMeteorButton extends WButton implements MeteorWidget {
    private double hoverAnim;
    private final Color bgColor = new Color(70, 119, 255);

    public WMeteorButton(String text, GuiTexture texture) {
        super(text, texture);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorGuiTheme theme = theme();
        double pad = pad();

        hoverAnim += delta * 10 * ((mouseOver || pressed) ? 1 : -1);
        hoverAnim = Mth.clamp(hoverAnim, 0, 1);

        // Accent-colored background, slightly darkened on hover (LiquidBounce style)
        Color accent = theme.accentColor.get();
        double darken = pressed ? 0.70 : (1.0 - Easing.easeOutCubic(hoverAnim) * 0.18);
        bgColor.r = (int) (accent.r * darken);
        bgColor.g = (int) (accent.g * darken);
        bgColor.b = (int) (accent.b * darken);
        bgColor.a = accent.a;

        renderer.quad(this, bgColor);

        if (text != null) {
            renderer.text(text, x + width / 2 - textWidth / 2, y + pad, theme.textColor.get(), false);
        } else {
            double ts = theme.textHeight();
            renderer.quad(x + width / 2 - ts / 2, y + pad, ts, ts, texture, theme.textColor.get());
        }
    }
}
