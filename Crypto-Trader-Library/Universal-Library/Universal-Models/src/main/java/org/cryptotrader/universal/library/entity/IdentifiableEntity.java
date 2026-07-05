package org.cryptotrader.universal.library.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class IdentifiableEntity<T> extends Identifiable<T> {
    @Id
    @Column(name = "id")
    protected T id;

    public IdentifiableEntity() {
        this.id = null;
    }
    public IdentifiableEntity(T id) {
        this.id = id;
    }
}
