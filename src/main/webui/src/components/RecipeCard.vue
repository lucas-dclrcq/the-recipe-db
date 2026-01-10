<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { Recipe } from '../types/recipe'

defineProps<{
  recipe: Recipe
}>()
</script>

<template>
  <RouterLink
    :to="`/recipes/${recipe.id}`"
    class="card-pop jiggling block p-5 relative overflow-hidden group"
  >
    <!-- Decorative corner -->
    <div class="absolute top-0 right-0 w-10 h-10 bg-secondary" style="clip-path: polygon(100% 0, 0 0, 100% 100%);" aria-hidden="true"></div>

    <div class="flex justify-between items-start mb-3 pr-6">
      <h3 class="text-lg font-black text-soft-black line-clamp-2 leading-tight tracking-tight group-hover:text-primary transition-colors">
        {{ recipe.name }}
      </h3>
      <span class="badge-page flex-shrink-0 ml-3">
        p. {{ recipe.pageNumber }}
      </span>
    </div>

    <div class="text-sm text-charcoal mb-4 flex items-center gap-2">
      <span v-if="recipe.cookbookTitle" class="font-medium">{{ recipe.cookbookTitle }}</span>
      <span v-if="recipe.cookbookTitle && recipe.cookbookAuthor" class="w-1 h-1 bg-primary rounded-full"></span>
      <span v-if="recipe.cookbookAuthor" class="text-charcoal/70">{{ recipe.cookbookAuthor }}</span>
    </div>

    <div v-if="recipe.ingredients.length > 0" class="flex flex-wrap gap-2">
      <span
        v-for="ingredient in recipe.ingredients.slice(0, 5)"
        :key="ingredient"
        class="badge-ingredient"
      >
        {{ ingredient }}
      </span>
      <span
        v-if="recipe.ingredients.length > 5"
        class="inline-flex items-center px-3 py-1 text-xs font-bold text-soft-black bg-cream rounded-full border-2 border-soft-black"
      >
        +{{ recipe.ingredients.length - 5 }}
      </span>
    </div>

    <!-- Hover effect accent line -->
    <div class="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-primary via-secondary to-accent transform scale-x-0 group-hover:scale-x-100 transition-transform origin-left" aria-hidden="true"></div>
  </RouterLink>
</template>
