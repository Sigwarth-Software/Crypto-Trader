package org.cryptotrader.universal.library.services

import org.cryptotrader.universal.library.entity.Identifiable
import org.cryptotrader.universal.library.model.annotation.Loggable
import org.cryptotrader.universal.library.model.exception.EntityNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.support.AopUtils
import org.springframework.core.ResolvableType
import org.springframework.data.jpa.repository.JpaRepository
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

abstract class BaseEntityService<Entity : Identifiable<Id>, Id : Any, Repository : JpaRepository<Entity, Id>>(
    val repository: Repository
) : EntityHandler<Entity, Id> {

    // TODO: Move to logging or universal.
    private object Ansi {
        const val RESET = "\u001B[0m"
        const val BOLD = "\u001B[1m"
        const val MAGENTA = "\u001B[35m"
        const val BLUE = "\u001B[34m"
        const val WHITE = "\u001B[37m"
        const val RED = "\u001B[31m"
        const val GRAY = "\u001B[90m"
    }

    protected val log: Logger = LoggerFactory.getLogger(javaClass)
    private val entityType: Class<out Entity> by lazy { this.resolveEntityType() }
    private val loggableFieldsByType: MutableMap<Class<*>, List<Field>> = ConcurrentHashMap()

    override fun saveIfNew(entity: Entity): Entity {
        if (!this.exists(entity)) {
            return this.save(entity)
        }
        log.info("{} already exists, skipping save. {}", this.formatEntityName(entity), this.getEntityContent(entity))
        return entity
    }

    override fun deleteIfExists(entity: Entity): Boolean {
        if (this.exists(entity)) {
            return this.delete(entity)
        }
        log.warn("{} does not exist, cannot delete. {}", this.formatEntityName(entity), this.getEntityContent(entity))
        return false
    }

    override fun findById(id: Id): Optional<Entity> {
        log.info("Finding {} with ID {}.", this.getEntityName(), id)
        return this.repository.findById(id)
    }

    override fun findByIds(ids: List<Id>): List<Entity> {
        log.info("Finding {} {}.", ids.size, this.getEntityName())
        return this.repository.findAllById(ids)
    }

    override fun findAll(): List<Entity> {
        val entities: List<Entity> = this.repository.findAll()
        log.info("Finding all ({}) {} entities.", entities.size, this.getEntityName())
        return entities
    }

    override fun exists(entity: Entity): Boolean {
        log.info("Checking existence of {}.{}", this.formatEntityName(entity), this.getEntityContent(entity))
        return this.repository.existsById(entity.id)
    }

    override fun existsById(id: Id): Boolean {
        log.info("Checking existence of {} with ID {}.", this.getEntityName(), id)
        return this.repository.existsById(id)
    }

    override fun count(): Long {
        log.info("Counting {} entities.", this.getEntityName())
        return this.repository.count()
    }

    override fun save(entity: Entity): Entity {
        return try {
            log.info("Saving new {}.{}", this.formatEntityName(entity), this.getEntityContent(entity))
            this.repository.save(entity)
        } catch (exception: IllegalArgumentException) {
            log.warn("Failed to save {}. {}", this.formatEntityName(entity), this.getEntityContent(entity))
            throw exception
        }
    }

    override fun saveAll(entities: List<Entity>): List<Entity> {
        return try {
            log.info("Saving {} {} entities.", entities.size, this.getEntityName(entities.first()))
            this.repository.saveAll(entities)
        } catch (exception: IllegalArgumentException) {
            log.error("Failed to save {} {} entities.", entities.size, this.getEntityName(entities.first()), exception)
            throw exception
        }
    }

    override fun delete(entity: Entity): Boolean {
        return try {
            log.info("Deleting {}.{}", this.formatEntityName(entity), this.getEntityContent(entity))
            this.repository.delete(entity)
            true
        } catch (exception: IllegalArgumentException) {
            log.error("Failed to delete {}.{}", this.formatEntityName(entity), this.getEntityContent(entity), exception)
            false
        }
    }

    override fun deleteById(id: Id): Boolean {
        if (this.existsById(id)) {
            return try {
                log.info("Deleting {} with ID {}.", this.getEntityName(), id)
                this.repository.deleteById(id)
                true
            } catch (exception: IllegalArgumentException) {
                log.error("Failed to delete {} with ID {}.", this.getEntityName(), id, exception)
                false
            }
        }
        log.warn("Entity with ID {} does not exist, cannot delete.", id)
        return false
    }

    override fun deleteAll(entities: List<Entity>): Boolean {
        val allExist: Boolean = entities.all { this.exists(it) }
        if (allExist) {
            return try {
                log.info(
                    "Deleting {} {}.",
                    entities.size,
                    this.getEntityName(entities.first())
                )
                this.repository.deleteAll(entities)
                true
            } catch (exception: IllegalArgumentException) {
                log.error("Failed to delete {} {}.", entities.size, this.getEntityName(entities.first()), exception)
                false
            }
        }
        log.error("Failed to delete {} {}, not all entities exist.", entities.size, this.getEntityName(entities.first()))
        return false
    }

    override fun update(entity: Entity): Entity {
        if (this.exists(entity)) {
            log.info("Updating {}.{}", this.formatEntityName(entity), this.getEntityContent(entity))
            return this.save(entity)
        }
        log.error("Failed to update {} because it does not exist.{}", this.formatEntityName(entity), this.getEntityContent(entity))
        throw EntityNotFoundException(entity)
    }

    override fun updateAll(entities: List<Entity>): List<Entity> {
        val allExist: Boolean = entities.all { this.exists(it) }
        if (allExist) {
            return this.saveAll(entities)
        }
        log.error("Failed to update {} {}, not all entities exist.", entities.size, this.getEntityName(entities.first()))
        throw EntityNotFoundException()
    }

    protected fun getEntityName(entity: Entity): String {
        return this.colorizeEntityName(AopUtils.getTargetClass(entity).simpleName ?: "UnknownEntity")
    }

    protected fun getEntityName(): String {
        return this.colorizeEntityName(this.entityType.simpleName ?: "UnknownEntity")
    }

    protected fun formatEntityName(entity: Entity): String {
        return this.getEntityName(entity)
    }

    protected fun getEntityContent(entity: Entity, withName: Boolean = false): String {
        val entityClass: Class<*> = AopUtils.getTargetClass(entity)
        val loggableValues: List<String> = this.getLoggableFields(entityClass)
            .map { field ->
                val annotation: Loggable = field.getAnnotation(Loggable::class.java)
                val key: String = annotation.name.ifBlank { field.name }
                val valueText: String = if (annotation.redact) {
                    "${Ansi.RED}***${Ansi.RESET}"
                } else {
                    "${Ansi.WHITE}${this.formatLogValue(this.readFieldValue(field, entity))}${Ansi.RESET}"
                }
                "${Ansi.GRAY}\t${Ansi.RESET}${Ansi.BLUE}${key}${Ansi.RESET}${Ansi.GRAY}: ${Ansi.RESET}$valueText"
            }

        if (loggableValues.isEmpty()) {
            return ""
        }

        return buildString {
            if (withName) {
                append(this@BaseEntityService.formatEntityName(entity))
                append(":")
            }
            append("\n${Ansi.GRAY}{${Ansi.RESET}\n")
            append(loggableValues.joinToString(",\n"))
            append("\n${Ansi.GRAY}}${Ansi.RESET}")
        }
    }

    private fun getLoggableFields(entityClass: Class<*>): List<Field> {
        return this.loggableFieldsByType.computeIfAbsent(entityClass) {
            val fields: MutableList<Field> = mutableListOf()
            var currentClass: Class<*>? = entityClass
            while (currentClass != null && currentClass != Any::class.java) {
                currentClass.declaredFields
                    .asSequence()
                    .filter { field ->
                        field.isAnnotationPresent(Loggable::class.java) &&
                            !field.isSynthetic &&
                            !Modifier.isStatic(field.modifiers)
                    }
                    .forEach { field ->
                        field.isAccessible = true
                        fields.add(field)
                    }
                currentClass = currentClass.superclass
            }
            fields
        }
    }

    private fun readFieldValue(field: Field, entity: Entity): Any? {
        return try {
            field.get(entity)
        } catch (exception: IllegalAccessException) {
            log.warn("Failed to read loggable field {} on {}.", field.name, this.getEntityName(entity), exception)
            null
        }
    }

    private fun colorizeEntityName(entityName: String): String {
        return "${Ansi.BOLD}${Ansi.MAGENTA}${entityName}${Ansi.RESET}"
    }

    private fun formatLogValue(value: Any?): String {
        return when (value) {
            null -> "null"
            is Array<*> -> value.joinToString(prefix = "[", postfix = "]") { this.formatLogValue(it) }
            is Collection<*> -> value.joinToString(prefix = "[", postfix = "]") { this.formatLogValue(it) }
            is Map<*, *> -> value.entries.joinToString(prefix = "{", postfix = "}") { (key, mapValue) ->
                "${this.formatLogValue(key)}: ${this.formatLogValue(mapValue)}"
            }
            else -> value.toString()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveEntityType(): Class<out Entity> {
        val targetClass: Class<*> = AopUtils.getTargetClass(this)
        val resolvedType: Class<*> = ResolvableType.forClass(targetClass)
            .`as`(BaseEntityService::class.java)
            .getGeneric(0)
            .resolve()
            ?: throw IllegalStateException("Unable to resolve entity type for ${targetClass.name}")

        return resolvedType as Class<out Entity>
    }
}
