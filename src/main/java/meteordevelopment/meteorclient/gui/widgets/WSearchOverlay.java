/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets;

public abstract class WSearchOverlay extends WWidget {
    public abstract void open();
    public abstract void close();
    public abstract boolean isOpen();
    public abstract void navigate(int delta);
    public abstract void toggleSelected();
}
