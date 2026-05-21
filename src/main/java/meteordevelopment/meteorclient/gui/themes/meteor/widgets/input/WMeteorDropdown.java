/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.themes.meteor.widgets.input;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WMeteorDropdown<T> extends WDropdown<T> implements MeteorWidget {
    public WMeteorDropdown(T[] values, T value) {
        super(values, value);
    }

    @Override
    protected WDropdownRoot createRootWidget() {
        return new WRoot();
    }

    @Override
    protected WDropdownValue createValueWidget() {
        return new WValue();
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorGuiTheme theme = theme();
        double pad = pad();
        double s = theme.textHeight();
        double bw = theme.scale(2);
        double r = theme.scale(5);

        Color outline = theme.outlineColor.get(pressed, mouseOver);
        Color bg = theme.backgroundColor.get(pressed, mouseOver);
        renderer.roundedRect(x, y, width, height, r, outline);
        renderer.roundedRect(x + bw, y + bw, width - bw * 2, height - bw * 2, Math.max(0, r - bw), bg);

        String text = get().toString();
        double w = theme.textWidth(text);
        renderer.text(text, x + pad + maxValueWidth / 2 - w / 2, y + pad, theme.textColor.get(), false);

        renderer.rotatedQuad(x + pad + maxValueWidth + pad, y + pad, s, s, 0, GuiRenderer.TRIANGLE, theme.textColor.get());
    }

    private static class WRoot extends WDropdownRoot implements MeteorWidget {
        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            MeteorGuiTheme theme = theme();
            double bw = theme.scale(2);
            double r = theme.scale(5);

            Color bg = theme.backgroundColor.get(true, false);
            Color outline = theme.outlineColor.get();
            renderer.roundedRect(x, y, width, height, r, outline);
            renderer.roundedRect(x + bw, y + bw, width - bw * 2, height - bw * 2, Math.max(0, r - bw), bg);
        }
    }

    private class WValue extends WDropdownValue implements MeteorWidget {
        private double hoverAnim;
        private final Color hoverOverlay = new Color(255, 255, 255, 0);

        @Override
        protected void onCalculateSize() {
            double pad = pad();

            width = pad + theme.textWidth(value.toString()) + pad;
            height = pad + theme.textHeight() + pad;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            MeteorGuiTheme theme = theme();

            // Smooth hover highlight (WRoot draws background)
            hoverAnim += delta * 12 * ((mouseOver || pressed) ? 1 : -1);
            hoverAnim = net.minecraft.util.Mth.clamp(hoverAnim, 0, 1);
            if (hoverAnim > 0) {
                hoverOverlay.a = (int) (meteordevelopment.meteorclient.utils.render.Easing.easeOutCubic(hoverAnim) * 22);
                renderer.quad(this, hoverOverlay);
            }

            // Selected value uses accent color
            Color textCol = value.equals(get()) ? theme.accentColor.get() : theme.textSecondaryColor.get();
            String text = value.toString();
            renderer.text(text, x + width / 2 - theme.textWidth(text) / 2, y + pad(), textCol, false);
        }
    }
}
