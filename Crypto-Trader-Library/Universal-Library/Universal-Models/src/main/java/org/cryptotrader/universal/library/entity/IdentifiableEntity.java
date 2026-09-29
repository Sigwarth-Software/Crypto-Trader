package org.cryptotrader.universal.library.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@MappedSuperclass
public class IdentifiableEntity<T> extends Identifiable<T> {
    @Id
    @Column(name = "id")
    protected @Nullable T id;

    public IdentifiableEntity() {
        this.id = null;
    }
    public IdentifiableEntity(final T id) {
        this.id = id;
    }
}
