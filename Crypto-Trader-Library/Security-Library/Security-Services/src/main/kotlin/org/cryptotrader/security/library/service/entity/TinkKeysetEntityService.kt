package org.cryptotrader.security.library.service.entity

import org.cryptotrader.security.library.entity.keyset.TinkKeyset
import org.cryptotrader.security.library.repository.keyset.TinkKeysetRepository
import org.cryptotrader.security.library.service.model.TinkKeysetStore
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class TinkKeysetEntityService @Autowired constructor(
    repository: TinkKeysetRepository
) : BaseEntityService<TinkKeyset, String, TinkKeysetRepository>(repository),
    TinkKeysetStore {

    override fun load(id: String): String? {
        return this.repository.findById(id)
            .map { it.keysetJson }
            .orElse(null)
    }

    override fun saveIfAbsent(id: String, keysetJson: String): String {
        val existingKeyset = this.load(id)
        if (existingKeyset != null) {
            return existingKeyset
        }

        return try {
            this.repository.insert(id, keysetJson)
            keysetJson
        } catch (_: DataIntegrityViolationException) {
            this.load(id)
                ?: throw IllegalStateException("Tink keyset was inserted concurrently but cannot be loaded: $id")
        }
    }
}
