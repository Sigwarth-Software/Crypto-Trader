package org.cryptotrader.universal.library.services.search

import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class SearchSpecificationBuilder<Entity> private constructor(
    private val entityType: Class<Entity>
) {

    private val specifications: MutableList<Specification<Entity>> = mutableListOf()

    companion object {
        fun <Entity> forEntity(entityType: Class<Entity>): SearchSpecificationBuilder<Entity> {
            return SearchSpecificationBuilder(entityType)
        }
    }

    fun equalTo(fieldPath: String, value: Any?): SearchSpecificationBuilder<Entity> {
        if (value == null) {
            return this
        }

        this.specifications.add { root, _, criteriaBuilder ->
            criteriaBuilder.equal(resolvePath(root, fieldPath), value)
        }

        return this
    }

    fun notEqualTo(fieldPath: String, value: Any?): SearchSpecificationBuilder<Entity> {
        if (value == null) {
            return this
        }

        this.specifications.add { root, _, criteriaBuilder ->
            criteriaBuilder.notEqual(resolvePath(root, fieldPath), value)
        }

        return this
    }

    fun containsIgnoreCase(fieldPath: String, value: String?): SearchSpecificationBuilder<Entity> {
        if (value.isNullOrBlank()) {
            return this
        }

        this.specifications.add { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                criteriaBuilder.lower(resolvePath(root, fieldPath).`as`(String::class.java)),
                "%${value.lowercase()}%"
            )
        }

        return this
    }

    fun startsWithIgnoreCase(fieldPath: String, value: String?): SearchSpecificationBuilder<Entity> {
        if (value.isNullOrBlank()) {
            return this
        }

        this.specifications.add { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                criteriaBuilder.lower(resolvePath(root, fieldPath).`as`(String::class.java)),
                "${value.lowercase()}%"
            )
        }

        return this
    }

    fun endsWithIgnoreCase(fieldPath: String, value: String?): SearchSpecificationBuilder<Entity> {
        if (value.isNullOrBlank()) {
            return this
        }

        this.specifications.add { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                criteriaBuilder.lower(resolvePath(root, fieldPath).`as`(String::class.java)),
                "%${value.lowercase()}"
            )
        }

        return this
    }

    fun <Value : Comparable<Value>> greaterThan(
        fieldPath: String,
        value: Value?
    ): SearchSpecificationBuilder<Entity> {
        if (value == null) {
            return this
        }

        this.specifications.add { root, _, criteriaBuilder ->
            @Suppress("UNCHECKED_CAST")
            criteriaBuilder.greaterThan(
                resolvePath(root, fieldPath).`as`(value.javaClass) as Path<Value>,
                value
            )
        }

        return this
    }

    fun <Value : Comparable<Value>> greaterThanOrEqualTo(
        fieldPath: String,
        value: Value?
    ): SearchSpecificationBuilder<Entity> {
        if (value == null) {
            return this
        }

        this.specifications.add { root, _, criteriaBuilder ->
            @Suppress("UNCHECKED_CAST")
            criteriaBuilder.greaterThanOrEqualTo(
                resolvePath(root, fieldPath).`as`(value.javaClass) as Path<Value>,
                value
            )
        }

        return this
    }

    fun <Value : Comparable<Value>> lessThan(
        fieldPath: String,
        value: Value?
    ): SearchSpecificationBuilder<Entity> {
        if (value == null) {
            return this
        }

        this.specifications.add { root, _, criteriaBuilder ->
            @Suppress("UNCHECKED_CAST")
            criteriaBuilder.lessThan(
                resolvePath(root, fieldPath).`as`(value.javaClass) as Path<Value>,
                value
            )
        }

        return this
    }

    fun <Value : Comparable<Value>> lessThanOrEqualTo(
        fieldPath: String,
        value: Value?
    ): SearchSpecificationBuilder<Entity> {
        if (value == null) {
            return this
        }

        this.specifications.add { root, _, criteriaBuilder ->
            @Suppress("UNCHECKED_CAST")
            criteriaBuilder.lessThanOrEqualTo(
                resolvePath(root, fieldPath).`as`(value.javaClass) as Path<Value>,
                value
            )
        }

        return this
    }

    fun <Value : Comparable<Value>> between(
        fieldPath: String,
        start: Value?,
        end: Value?
    ): SearchSpecificationBuilder<Entity> {
        if (start == null || end == null) {
            return this
        }

        this.specifications.add { root, _, criteriaBuilder ->
            @Suppress("UNCHECKED_CAST")
            criteriaBuilder.between(
                resolvePath(root, fieldPath).`as`(start.javaClass) as Path<Value>,
                start,
                end
            )
        }

        return this
    }

    fun `in`(fieldPath: String, values: List<*>?): SearchSpecificationBuilder<Entity> {
        if (values.isNullOrEmpty()) {
            return this
        }

        this.specifications.add { root, _, _ ->
            resolvePath(root, fieldPath).`in`(values)
        }

        return this
    }

    fun or(block: SearchSpecificationBuilder<Entity>.() -> Unit): SearchSpecificationBuilder<Entity> {
        val branch = SearchSpecificationBuilder(this.entityType).apply(block)

        if (branch.specifications.isEmpty()) {
            return this
        }

        this.specifications.add { root, query, criteriaBuilder ->
            val predicates = branch.specifications
                .mapNotNull { it.toPredicate(root, query, criteriaBuilder) }
                .toTypedArray()

            criteriaBuilder.or(*predicates)
        }

        return this
    }

    fun build(): Specification<Entity> {
        var result: Specification<Entity> = Specification { _, _, criteriaBuilder -> criteriaBuilder.conjunction() }

        for (specification in this.specifications) {
            result = result.and(specification)
        }

        return result
    }

    private fun resolvePath(root: Root<Entity>, fieldPath: String): Path<*> {
        val fieldParts: List<String> = fieldPath.split(".")
        var path: Path<*> = root.get<Any>(fieldParts[0])

        for (index in 1 until fieldParts.size) {
            path = path.get<Any>(fieldParts[index])
        }

        return path
    }
}
