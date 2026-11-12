/*
 * Copyright 2026 Jaime Enrique Rojas Almonte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.jaimerojas.clickup.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * A ClickUp API call that came back with a non-2xx status. ClickUp's error body (<code>{"err": ..., "ECODE": ...}</code>)
 * becomes the message and the error code.
 */
public class ClickUpApiException extends IOException {
    private final int statusCode;
    private final String errorCode;

    public ClickUpApiException(int statusCode, @Nullable String errorCode, @Nullable String error) {
        super(buildMessage(statusCode, errorCode, error));
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public @Nullable String getErrorCode() {
        return errorCode;
    }

    /**
     * The token is missing, invalid or expired (401), or it has no access to what was asked for (403).
     */
    public boolean isAuthenticationError() {
        return statusCode == 401 || statusCode == 403;
    }

    public boolean isNotFound() {
        return statusCode == 404;
    }

    private static @NotNull String buildMessage(int statusCode, @Nullable String errorCode, @Nullable String error) {
        StringBuilder message = new StringBuilder("ClickUp API error (HTTP ").append(statusCode);
        if (errorCode != null && !errorCode.isBlank()) {
            message.append(", ").append(errorCode);
        }
        message.append(")");
        if (error != null && !error.isBlank()) {
            message.append(": ").append(error);
        }
        return message.toString();
    }
}
