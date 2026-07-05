package org.cryptotrader.universal.library.services

import org.cryptotrader.universal.library.entity.Identifiable
interface EntityHandler<Entity : Identifiable<Id>, Id : Any> : EntityLookup<Entity, Id>, EntityPersistenceHandler<Entity, Id> {
    fun saveIfNew(entity: Entity): Entity
    fun deleteIfExists(entity: Entity): Boolean
}
