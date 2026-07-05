package org.cryptotrader.security.library.entity.keyset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.cryptotrader.universal.library.entity.Identifiable;
import org.cryptotrader.universal.library.model.annotation.Loggable;

import java.time.Instant;

@Entity
@Table(name = "tink_keysets")
@Getter
@Setter
public class TinkKeyset extends Identifiable<String> {

    @Id
    @Loggable
    @Column(name = "id", nullable = false, length = 128)
    private String id;

    @Loggable(redact = true)
    @Column(name = "keyset_json", nullable = false, columnDefinition = "jsonb")
    private String keysetJson;

    @Loggable
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Loggable
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TinkKeyset() {
        this(null, null);
    }

    public TinkKeyset(String id, String keysetJson) {
        Instant now = Instant.now();
        this.id = id;
        this.keysetJson = keysetJson;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
