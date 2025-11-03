package com.jelly.farmhelperv2.config.custom;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.utils.InputHandler;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class UpdateButton extends BasicOption {
    private final Supplier<String> textSupplier; // dynamic text
    private final String button1Label;
    private final String button2Label;
    private final Runnable onButton1Click;
    private final Runnable onButton2Click;

    public UpdateButton(Field field, Object parent, String name,
            String category, String subcategory,
            Supplier<String> textSupplier,
            String button1Label, Runnable onButton1Click,
            String button2Label, Runnable onButton2Click
    ) {
        super(field, parent, name, category, subcategory, null,0);
        this.textSupplier = textSupplier;
        this.button1Label = button1Label;
        this.button2Label = button2Label;
        this.onButton1Click = onButton1Click;
        this.onButton2Click = onButton2Click;
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    public void draw(long vg, int x, int y, InputHandler inputHandler) {

    }
}
