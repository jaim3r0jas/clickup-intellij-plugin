package de.jaimerojas.clickup.model;

import com.intellij.ui.scale.ScaleContext;
import com.intellij.util.ImageLoader;
import com.intellij.util.ui.ImageUtil;

import javax.swing.*;
import java.util.Objects;

public class ClickUpTaskIconHolder {
    private static final int ICON_SIZE = 7;
    private static final String CLICKUP_ICON_PATH = "/icons/logo-v3-clickup-symbol-only.svg";

    public static final ImageIcon CLICKUP_ICON = new ImageIcon(ImageUtil.toBufferedImage(ImageUtil.resize(
            Objects.requireNonNull(
                    ImageLoader.loadFromResource(CLICKUP_ICON_PATH, ClickUpTaskIconHolder.class)
            ), ICON_SIZE, ScaleContext.createIdentity()
    )));
}
