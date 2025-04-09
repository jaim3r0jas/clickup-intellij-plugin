package de.jaimerojas.clickup;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class ClickUpBundle extends AbstractBundle {
    private static final String BUNDLE = "messages.ClickUpBundle";
    private static final ClickUpBundle INSTANCE = new ClickUpBundle();

    private ClickUpBundle() {
        super(BUNDLE);
    }

    @NotNull
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}