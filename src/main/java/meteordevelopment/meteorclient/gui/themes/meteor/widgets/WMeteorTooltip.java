/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WTooltip;

public class WMeteorTooltip extends WTooltip implements MeteorWidget {
    public WMeteorTooltip(String text) {
        super(text);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorGuiTheme theme = theme();
        double s = theme.scale(2);

        // Dark background (90% black)
        renderer.quad(x + s, y + s, width - s * 2, height - s * 2, theme.backgroundColor.get(true, false));

        // Accent-colored border (LiquidBounce tooltip style)
        renderer.quad(x, y, width, s, theme.accentColor.get());
        renderer.quad(x, y + height - s, width, s, theme.accentColor.get());
        renderer.quad(x, y + s, s, height - s * 2, theme.accentColor.get());
        renderer.quad(x + width - s, y + s, s, height - s * 2, theme.accentColor.get());
    }
}
