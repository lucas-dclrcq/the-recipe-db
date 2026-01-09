<script setup lang="ts">
import { watch } from 'vue'
import RecipeCard from './RecipeCard.vue'
import RecipeCardSkeleton from './RecipeCardSkeleton.vue'
import { useInfiniteScroll } from '../composables/useInfiniteScroll'
import type { Recipe } from '../types/recipe'

const props = defineProps<{
  recipes: Recipe[]
  isLoading: boolean
  hasMore: boolean
}>()

const emit = defineEmits<{
  loadMore: []
}>()

function handleLoadMore() {
  if (!props.isLoading && props.hasMore) {
    emit('loadMore')
  }
}

const { targetRef: scrollTargetRef, disable, enable } = useInfiniteScroll(handleLoadMore, {
  rootMargin: '200px',
})

// Used in template as ref="scrollTargetRef"
void scrollTargetRef

watch(
  () => props.hasMore,
  (hasMore) => {
    if (hasMore) {
      enable()
    } else {
      disable()
    }
  },
  { immediate: true }
)

watch(
  () => props.isLoading,
  (isLoading) => {
    if (isLoading) {
      disable()
    } else if (props.hasMore) {
      enable()
    }
  }
)
</script>

<template>
  <div>
    <div v-if="recipes.length === 0 && !isLoading" class="text-center py-12">
      <svg
        class="mx-auto h-12 w-12 text-gray-400"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
      <h3 class="mt-2 text-sm font-medium text-gray-900">No recipes found</h3>
      <p class="mt-1 text-sm text-gray-500">Try adjusting your search or filters.</p>
    </div>

    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <RecipeCard v-for="recipe in recipes" :key="recipe.id" :recipe="recipe" />
    </div>

    <div v-if="isLoading && recipes.length > 0" class="flex justify-center py-8">
      <svg
        class="animate-spin h-8 w-8 text-indigo-600"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
      >
        <circle
          class="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          stroke-width="4"
        ></circle>
        <path
          class="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        ></path>
      </svg>
    </div>

    <div v-else-if="isLoading && recipes.length === 0" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <RecipeCardSkeleton v-for="n in 6" :key="n" />
    </div>

    <div
      v-else-if="!hasMore && recipes.length > 0"
      class="text-center py-6 text-sm text-gray-500"
    >
      You've reached the end of the results
    </div>

    <div
      ref="scrollTargetRef"
      v-if="hasMore && !isLoading"
      class="h-4"
      aria-hidden="true"
    ></div>
  </div>
</template>
