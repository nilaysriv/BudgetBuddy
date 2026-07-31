package com.nilay.budgetbuddy.util

import com.nilay.budgetbuddy.data.local.entity.Category

/** Orders categories so each top-level category is immediately followed by its own subcategories (one level deep). */
fun List<Category>.groupedByParent(): List<Category> {
    val topLevel = filter { it.parentCategoryId == null }
    val childrenByParent = filter { it.parentCategoryId != null }.groupBy { it.parentCategoryId }
    return topLevel.flatMap { parent -> listOf(parent) + (childrenByParent[parent.id] ?: emptyList()) }
}
