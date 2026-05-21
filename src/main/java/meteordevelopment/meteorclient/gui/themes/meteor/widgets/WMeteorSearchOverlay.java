/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WSearchOverlay;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.Easing;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.utils.Utils.getWindowHeight;
import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;

public class WMeteorSearchOverlay extends WSearchOverlay implements MeteorWidget {
    private WTextBox textBox;
    private final List<WSearchResult> resultWidgets = new ArrayList<>();
    final List<Module> filteredModules = new ArrayList<>();
    int selectedIndex = 0;
    private boolean open = false;

    private static final Color DIM_COLOR = new Color(0, 0, 0, 110);

    @Override
    public void init() {
        textBox = theme().textBox("");
        textBox.parent = this;
        textBox.action = this::updateResults;
        textBox.init();
    }

    @Override
    public void open() {
        if (open) return;
        open = true;
        textBox.set("");
        textBox.setFocused(true);
        filteredModules.clear();
        resultWidgets.clear();
        selectedIndex = 0;
        invalidate();
    }

    @Override
    public void close() {
        if (!open) return;
        open = false;
        textBox.setFocused(false);
        filteredModules.clear();
        resultWidgets.clear();
        selectedIndex = 0;
        invalidate();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void navigate(int delta) {
        if (filteredModules.isEmpty()) return;
        selectedIndex = Mth.clamp(selectedIndex + delta, 0, filteredModules.size() - 1);
    }

    @Override
    public void toggleSelected() {
        if (selectedIndex < filteredModules.size()) {
            filteredModules.get(selectedIndex).toggle();
        }
    }

    private int fuzzyScore(String query, String target) {
        String t = target.toLowerCase();
        int qi = 0, score = 0, consecutive = 0;
        for (int ti = 0; ti < t.length() && qi < query.length(); ti++) {
            if (t.charAt(ti) == query.charAt(qi)) {
                score += 1 + consecutive * 2;
                if (ti == 0 || t.charAt(ti - 1) == ' ' || Character.isUpperCase(target.charAt(ti))) score += 4;
                consecutive++;
                qi++;
            } else {
                consecutive = 0;
            }
        }
        return qi == query.length() ? score : -1;
    }

    private void updateResults() {
        resultWidgets.clear();
        filteredModules.clear();
        selectedIndex = 0;

        String query = textBox.get().toLowerCase().trim();
        if (!query.isEmpty()) {
            record Scored(Module module, int score) {}
            List<Scored> scored = new ArrayList<>();
            for (Module module : Modules.get().getAll()) {
                int s = fuzzyScore(query, module.name);
                if (s >= 0) scored.add(new Scored(module, s));
            }
            scored.sort((a, b) -> Integer.compare(b.score(), a.score()));
            for (Scored s : scored) filteredModules.add(s.module());

            int limit = Math.min(filteredModules.size(), 10);
            for (int i = 0; i < limit; i++) {
                WSearchResult rw = new WSearchResult(i);
                rw.theme = theme;
                rw.parent = this;
                rw.init();
                resultWidgets.add(rw);
            }
        }

        invalidate();
    }

    @Override
    protected void onCalculateSize() {
        if (!open) {
            width = 0;
            height = 0;
            return;
        }

        MeteorGuiTheme theme = theme();
        double pad = theme.scale(14);

        textBox.calculateSize();
        for (WSearchResult rw : resultWidgets) rw.calculateSize();

        width = theme.scale(480);
        double h = pad + textBox.height + pad;

        if (!resultWidgets.isEmpty()) {
            h += theme.scale(2); // separator
            for (WSearchResult rw : resultWidgets) h += rw.height;
            h += theme.scale(8); // bottom padding
        }

        height = h;
    }

    @Override
    protected void onCalculateWidgetPositions() {
        if (!open) return;

        MeteorGuiTheme theme = theme();
        double pad = theme.scale(14);

        x = Math.round((getWindowWidth() - width) / 2.0);
        y = Math.round(theme.scale(80));

        textBox.x = x + pad;
        textBox.y = y + pad;
        textBox.width = width - pad * 2;
        textBox.calculateWidgetPositions();

        double ry = y + pad + textBox.height + pad;
        if (!resultWidgets.isEmpty()) ry += theme.scale(2);
        for (WSearchResult rw : resultWidgets) {
            rw.x = x + pad;
            rw.y = Math.round(ry);
            rw.width = width - pad * 2;
            rw.calculateWidgetPositions();
            ry += rw.height;
        }
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!open) return;

        renderer.absolutePost(() -> {
            MeteorGuiTheme theme = theme();
            double pad = theme.scale(14);
            double bw = theme.scale(2);
            double r = theme.scale(10);
            double screenW = getWindowWidth();

            double screenH = getWindowHeight();
            renderer.scissorStart(0, 0, screenW, screenH);

            renderer.quad(0, 0, screenW, screenH, DIM_COLOR);

            Color outline = theme.outlineColor.get();
            Color bg = theme.backgroundColor.get(true, false);
            renderer.roundedRect(x, y, width, height, r, outline);
            renderer.roundedRect(x + bw, y + bw, width - bw * 2, height - bw * 2, Math.max(0, r - bw), bg);

            textBox.render(renderer, mouseX, mouseY, delta);

            if (!resultWidgets.isEmpty()) {
                double sepY = y + pad + textBox.height + pad - bw * 0.5;
                renderer.quad(x + pad * 2, sepY, width - pad * 4, bw, theme.accentColor.get());

                for (WSearchResult rw : resultWidgets) {
                    rw.render(renderer, mouseX, mouseY, delta);
                }
            }

            renderer.scissorEnd();
        });
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (!open) return false;
        if (!isOver(click.x(), click.y())) return false;
        if (textBox.mouseClicked(click, doubled)) return true;
        for (WSearchResult rw : resultWidgets) if (rw.mouseClicked(click, doubled)) return true;
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY, double lmX, double lmY) {
        mouseOver = isOver(mouseX, mouseY);
        if (!open) return;
        textBox.mouseMoved(mouseX, mouseY, lmX, lmY);
        for (WSearchResult rw : resultWidgets) rw.mouseMoved(mouseX, mouseY, lmX, lmY);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (!open) return false;
        return textBox.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (!open) return false;
        return textBox.keyPressed(input);
    }

    @Override
    public boolean keyRepeated(KeyEvent input) {
        if (!open) return false;
        return textBox.keyRepeated(input);
    }

    @Override
    public boolean isFocused() {
        return open && textBox != null && textBox.isFocused();
    }

    private class WSearchResult extends WWidget implements MeteorWidget {
        private final int index;
        private double hoverAnim;
        private final Color hoverColor = new Color(255, 255, 255, 0);
        private final Color selectionColor = new Color(70, 119, 255, 20);
        private final Color catColor = new Color(211, 211, 211, 80);

        WSearchResult(int index) {
            this.index = index;
        }

        @Override
        protected void onCalculateSize() {
            MeteorGuiTheme t = theme();
            double pad = t.scale(8);
            height = t.textHeight() + pad * 2;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            MeteorGuiTheme theme = theme();
            double pad = theme.scale(10);
            if (index >= filteredModules.size()) return;
            Module module = filteredModules.get(index);
            boolean selected = index == selectedIndex;

            if (selected) renderer.quad(this, selectionColor);

            hoverAnim += delta * 12 * (mouseOver ? 1 : -1);
            hoverAnim = Mth.clamp(hoverAnim, 0, 1);
            if (hoverAnim > 0) {
                hoverColor.a = (int) (Easing.easeOutCubic(hoverAnim) * 18);
                renderer.quad(this, hoverColor);
            }

            Color textColor = module.isActive() ? theme.accentColor.get() : theme.textSecondaryColor.get();
            double indent = selected ? theme.scale(6) : 0;
            renderer.text(module.name, x + indent, y + (height - theme.textHeight()) / 2, textColor, false);

            String catText = module.category.name;
            renderer.text(catText, x + width - theme.textWidth(catText), y + (height - theme.textHeight()) / 2, catColor, false);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            if (mouseOver && click.button() == 0) {
                if (index < filteredModules.size()) {
                    filteredModules.get(index).toggle();
                    selectedIndex = index;
                }
                return true;
            }
            return false;
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY, double lmX, double lmY) {
            mouseOver = isOver(mouseX, mouseY);
            if (mouseOver) selectedIndex = index;
        }
    }
}
