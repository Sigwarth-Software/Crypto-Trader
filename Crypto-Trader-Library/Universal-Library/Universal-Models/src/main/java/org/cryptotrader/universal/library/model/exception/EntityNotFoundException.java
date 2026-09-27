package org.cryptotrader.universal.library.model.exception;

import org.cryptotrader.universal.library.entity.Identifiable;

public class EntityNotFoundException extends RuntimeException {
    private static String DEFAULT_MESSAGE = "Entity not found";

    public EntityNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public EntityNotFoundException(final String message) {
        super(message);
    }

    public <T> EntityNotFoundException(final String message, final Identifiable<T> entity) {
        super(message + ": ID " + entity.getId() + " (" + entity.getClass().getSimpleName() + ")" );
    }

    public <T> EntityNotFoundException(final Identifiable<T> entity) {
        super(DEFAULT_MESSAGE + ": ID " + entity.getId() + " (" + entity.getClass().getSimpleName() + ")" );
    }
}
