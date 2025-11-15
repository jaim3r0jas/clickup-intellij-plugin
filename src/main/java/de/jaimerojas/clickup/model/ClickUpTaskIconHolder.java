package de.jaimerojas.clickup.model;

import com.intellij.ui.scale.ScaleContext;
import com.intellij.util.ImageLoader;
import com.intellij.util.ui.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.Objects;

public class ClickUpTaskIconHolder {
    private static final int ICON_SIZE = 7;

    public static final BufferedImage clickUpIcon =
            ImageUtil.toBufferedImage(
                    ImageUtil.resize(
                            Objects.requireNonNull(ImageLoader.loadFromResource("/icons/logo-v3-clickup-symbol-only.svg", ClickUpTaskIconHolder.class)),
                            ICON_SIZE,
                            ScaleContext.createIdentity()
                    )
            );
}
