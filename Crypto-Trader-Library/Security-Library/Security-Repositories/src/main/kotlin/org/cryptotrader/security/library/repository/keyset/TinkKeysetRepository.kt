package org.cryptotrader.security.library.repository.keyset

import org.cryptotrader.security.library.entity.keyset.TinkKeyset
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface TinkKeysetRepository : JpaRepository<TinkKeyset, String> {

    @Modifying
    @Transactional
    @Query(
        value = """
            insert into tink_keysets (id, keyset_json, created_at, updated_at)
            values (:id, :keysetJson, current_timestamp, current_timestamp)
        """,
        nativeQuery = true
    )
    fun insert(
        @Param("id") id: String,
        @Param("keysetJson") keysetJson: String
    ): Int
}