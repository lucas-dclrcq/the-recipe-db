<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { Recipe } from '../types/recipe'

defineProps<{
  recipe: Recipe
  clickable?: boolean
}>()
</script>

<template>
  <RouterLink
    v-if="clickable !== false"
    :to="`/recipes/${recipe.id}`"
    class="block bg-white rounded-lg shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow"
  >
    <div class="flex justify-between items-start mb-2">
      <h3 class="text-lg font-semibold text-gray-900 line-clamp-2">
        {{ recipe.name }}
      </h3>
      <span class="flex-shrink-0 ml-2 px-2 py-1 text-xs font-medium text-gray-600 bg-gray-100 rounded">
        p. {{ recipe.pageNumber }}
      </span>
    </div>

    <div class="text-sm text-gray-500 mb-3">
      <span v-if="recipe.cookbookTitle">{{ recipe.cookbookTitle }}</span>
      <span v-if="recipe.cookbookTitle && recipe.cookbookAuthor"> &middot; </span>
      <span v-if="recipe.cookbookAuthor">{{ recipe.cookbookAuthor }}</span>
    </div>

    <div v-if="recipe.ingredients.length > 0" class="flex flex-wrap gap-1">
      <span
        v-for="ingredient in recipe.ingredients.slice(0, 5)"
        :key="ingredient"
        class="inline-flex items-center px-2 py-0.5 text-xs font-medium text-indigo-700 bg-indigo-50 rounded"
      >
        {{ ingredient }}
      </span>
      <span
        v-if="recipe.ingredients.length > 5"
        class="inline-flex items-center px-2 py-0.5 text-xs font-medium text-gray-500 bg-gray-100 rounded"
      >
        +{{ recipe.ingredients.length - 5 }} more
      </span>
    </div>
  </RouterLink>

  <div
    v-else
    class="block bg-white rounded-lg shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow"
  >
    <div class="flex justify-between items-start mb-2">
      <h3 class="text-lg font-semibold text-gray-900 line-clamp-2">
        {{ recipe.name }}
      </h3>
      <span class="flex-shrink-0 ml-2 px-2 py-1 text-xs font-medium text-gray-600 bg-gray-100 rounded">
        p. {{ recipe.pageNumber }}
      </span>
    </div>

    <div class="text-sm text-gray-500 mb-3">
      <span v-if="recipe.cookbookTitle">{{ recipe.cookbookTitle }}</span>
      <span v-if="recipe.cookbookTitle && recipe.cookbookAuthor"> &middot; </span>
      <span v-if="recipe.cookbookAuthor">{{ recipe.cookbookAuthor }}</span>
    </div>

    <div v-if="recipe.ingredients.length > 0" class="flex flex-wrap gap-1">
      <span
        v-for="ingredient in recipe.ingredients.slice(0, 5)"
        :key="ingredient"
        class="inline-flex items-center px-2 py-0.5 text-xs font-medium text-indigo-700 bg-indigo-50 rounded"
      >
        {{ ingredient }}
      </span>
      <span
        v-if="recipe.ingredients.length > 5"
        class="inline-flex items-center px-2 py-0.5 text-xs font-medium text-gray-500 bg-gray-100 rounded"
      >
        +{{ recipe.ingredients.length - 5 }} more
      </span>
    </div>
  </div>
</template>
