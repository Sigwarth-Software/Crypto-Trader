package org.cryptotrader.universal.library.services

import org.cryptotrader.universal.library.entity.Identifiable
import java.util.Optional

interface EntityLookup<Entity : Identifiable<Id>, Id : Any> {
    fun findById(id: Id): Optional<Entity>

    fun findByIds(ids: List<Id>): List<Entity>

    fun findAll(): List<Entity>

    fun exists(entity: Entity): Boolean

    fun existsById(id: Id): Boolean

    fun count(): Long
}
