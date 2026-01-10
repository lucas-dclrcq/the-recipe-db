<script setup lang="ts">
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'
import RecipeCard from './RecipeCard.vue'
import RecipeCardSkeleton from './RecipeCardSkeleton.vue'
import { useInfiniteScroll } from '../composables/useInfiniteScroll'
import type { Recipe } from '../types/recipe'

const { t } = useI18n()

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
    <!-- Empty state with artistic styling -->
    <div v-if="recipes.length === 0 && !isLoading" class="empty-state">
      <div class="empty-state-icon">
        <svg
          class="w-10 h-10 text-charcoal"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
      </div>
      <p class="text-lg font-bold text-soft-black mb-2">{{ t('recipes.noRecipesFound') }}</p>
      <p class="text-charcoal">{{ t('recipes.noRecipesHint') }}</p>
    </div>

    <!-- Recipe grid -->
    <div v-else class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 pb-2 pr-2">
      <RecipeCard v-for="recipe in recipes" :key="recipe.id" :recipe="recipe" />
    </div>

    <!-- Loading more indicator -->
    <div v-if="isLoading && recipes.length > 0" class="flex justify-center py-10">
      <div class="flex items-center gap-3 px-6 py-3 bg-white rounded-xl border-3 border-soft-black shadow-[4px_4px_0_var(--color-soft-black)]">
        <svg
          class="animate-spin h-6 w-6 text-primary"
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
        <span class="font-bold text-soft-black">{{ t('common.loadingMore') }}</span>
      </div>
    </div>

    <!-- Initial loading skeleton -->
    <div v-else-if="isLoading && recipes.length === 0" class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 pb-2 pr-2">
      <RecipeCardSkeleton v-for="n in 6" :key="n" />
    </div>

    <!-- End of results -->
    <div
      v-else-if="!hasMore && recipes.length > 0"
      class="text-center py-8"
    >
      <div class="inline-flex items-center gap-2 px-4 py-2 bg-cream rounded-xl border-2 border-soft-black/20">
        <span class="w-2 h-2 bg-accent rounded-full"></span>
        <span class="text-sm font-medium text-charcoal">{{ t('recipes.endOfList') }}</span>
        <span class="w-2 h-2 bg-accent rounded-full"></span>
      </div>
    </div>

    <!-- Infinite scroll trigger -->
    <div
      ref="scrollTargetRef"
      v-if="hasMore && !isLoading"
      class="h-4"
      aria-hidden="true"
    ></div>
  </div>
</template>
