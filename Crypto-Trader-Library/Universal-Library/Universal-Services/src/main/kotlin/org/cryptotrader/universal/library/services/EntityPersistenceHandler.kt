package org.cryptotrader.universal.library.services

import org.cryptotrader.universal.library.entity.Identifiable
import org.cryptotrader.universal.library.model.exception.EntityNotFoundException
import kotlin.jvm.Throws

interface EntityPersistenceHandler<Entity : Identifiable<Id>, Id : Any> {
    fun save(entity: Entity): Entity

    fun saveAll(entities: List<Entity>): List<Entity>

    fun delete(entity: Entity): Boolean

    fun deleteById(id: Id): Boolean

    fun deleteAll(entities: List<Entity>): Boolean

    @Throws(EntityNotFoundException::class)
    fun update(entity: Entity): Entity

    @Throws(EntityNotFoundException::class)
    fun updateAll(entities: List<Entity>): List<Entity>
}
