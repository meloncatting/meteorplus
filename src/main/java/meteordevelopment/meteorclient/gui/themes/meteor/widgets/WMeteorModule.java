/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.Easing;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.Mth;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WMeteorModule extends WPressable implements MeteorWidget {
    private final Module module;
    private final String title;

    private double titleWidth;

    private double hoverAnim;
    private double activeAnim;

    // Reusable Color for animated text interpolation
    private final Color textColorLerp = new Color(211, 211, 211);
    // Reusable Color for hover background with runtime alpha
    private final Color hoverBgColor = new Color(0, 0, 0, 0);

    public WMeteorModule(Module module, String title) {
        this.module = module;
        this.title = title;
        this.tooltip = module.description;

        activeAnim = module.isActive() ? 1 : 0;
        hoverAnim = 0;
    }

    @Override
    public double pad() {
        return theme.scale(6);
    }

    @Override
    protected void onCalculateSize() {
        double pad = pad();

        if (titleWidth == 0) titleWidth = theme.textWidth(title);

        width = pad + titleWidth + pad;
        height = pad + theme.textHeight() + pad;
    }

    @Override
    protected void onPressed(int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) module.toggle();
        else if (button == GLFW_MOUSE_BUTTON_RIGHT) mc.setScreen(theme.moduleScreen(module));
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorGuiTheme theme = theme();
        double pad = pad();

        // Full-width hover background (like LiquidBounce's base-85-color overlay)
        hoverAnim += delta * 14 * (mouseOver ? 1 : -1);
        hoverAnim = Mth.clamp(hoverAnim, 0, 1);

        if (hoverAnim > 0) {
            Color bg = theme.moduleBackground.get();
            hoverBgColor.r = bg.r;
            hoverBgColor.g = bg.g;
            hoverBgColor.b = bg.b;
            hoverBgColor.a = (int) (bg.a * Easing.easeOutCubic(hoverAnim));
            renderer.quad(x, y, width, height, hoverBgColor);
        }

        // Text color: textSecondaryColor (#d3d3d3) → accentColor (#4677FF) when active
        activeAnim += delta * 12 * (module.isActive() ? 1 : -1);
        activeAnim = Mth.clamp(activeAnim, 0, 1);

        double easedActive = Easing.easeOutCubic(activeAnim);
        Color dimmed = theme.textSecondaryColor.get();
        Color accent = theme.accentColor.get();
        textColorLerp.r = (int) (dimmed.r + (accent.r - dimmed.r) * easedActive);
        textColorLerp.g = (int) (dimmed.g + (accent.g - dimmed.g) * easedActive);
        textColorLerp.b = (int) (dimmed.b + (accent.b - dimmed.b) * easedActive);
        textColorLerp.a = 255;

        double tx = this.x + pad;
        double w = width - pad * 2;

        if (theme.moduleAlignment.get() == AlignmentX.Center) {
            tx += w / 2 - titleWidth / 2;
        } else if (theme.moduleAlignment.get() == AlignmentX.Right) {
            tx += w - titleWidth;
        }

        renderer.text(title, tx, y + pad, textColorLerp, false);
    }
}
