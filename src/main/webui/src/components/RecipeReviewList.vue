<script setup lang="ts">
import { computed } from 'vue'
import RecipeReviewItem from './RecipeReviewItem.vue'
import type { ReviewableRecipe } from '../composables/useImportWizard'

interface Props {
  recipes: ReviewableRecipe[]
  disabled?: boolean
}

interface Emits {
  (e: 'update', index: number, updates: Partial<ReviewableRecipe>): void
  (e: 'toggle-keep', index: number): void
  (e: 'submit'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const stats = computed(() => {
  const kept = props.recipes.filter((r) => r.keep).length
  const skipped = props.recipes.filter((r) => !r.keep).length
  const needsReview = props.recipes.filter((r) => r.needsReview).length
  return { kept, skipped, needsReview, total: props.recipes.length }
})

const groupedByIngredient = computed(() => {
  const groups = new Map<string, { recipe: ReviewableRecipe; index: number }[]>()

  props.recipes.forEach((recipe, index) => {
    const ingredient = recipe.ingredient || 'Unknown'
    if (!groups.has(ingredient)) {
      groups.set(ingredient, [])
    }
    groups.get(ingredient)!.push({ recipe, index })
  })

  return Array.from(groups.entries()).sort((a, b) => a[0].localeCompare(b[0]))
})

function handleUpdate(index: number, updates: Partial<ReviewableRecipe>) {
  emit('update', index, updates)
}

function handleToggleKeep(index: number) {
  emit('toggle-keep', index)
}

function handleSubmit() {
  emit('submit')
}
</script>

<template>
  <div class="space-y-6">
    <div class="bg-gray-50 rounded-lg p-4">
      <h3 class="text-sm font-medium text-gray-700 mb-3">Summary</h3>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
        <div>
          <div class="text-2xl font-bold text-gray-900">{{ stats.total }}</div>
          <div class="text-xs text-gray-500">Total Found</div>
        </div>
        <div>
          <div class="text-2xl font-bold text-green-600">{{ stats.kept }}</div>
          <div class="text-xs text-gray-500">To Import</div>
        </div>
        <div>
          <div class="text-2xl font-bold text-gray-400">{{ stats.skipped }}</div>
          <div class="text-xs text-gray-500">Skipped</div>
        </div>
        <div v-if="stats.needsReview > 0">
          <div class="text-2xl font-bold text-yellow-600">{{ stats.needsReview }}</div>
          <div class="text-xs text-gray-500">Needs Review</div>
        </div>
      </div>
    </div>

    <div v-if="stats.needsReview > 0" class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
      <div class="flex">
        <svg
          class="h-5 w-5 text-yellow-400 mr-2"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          />
        </svg>
        <p class="text-sm text-yellow-700">
          <strong>{{ stats.needsReview }}</strong> recipe{{ stats.needsReview === 1 ? '' : 's' }}
          {{ stats.needsReview === 1 ? 'has' : 'have' }} low confidence and
          {{ stats.needsReview === 1 ? 'needs' : 'need' }} review. Please verify the extracted data.
        </p>
      </div>
    </div>

    <div class="max-h-[500px] overflow-y-auto space-y-6 pr-2">
      <div v-for="[ingredient, items] in groupedByIngredient" :key="ingredient">
        <h4 class="text-sm font-medium text-gray-700 mb-2 sticky top-0 bg-white py-1">
          {{ ingredient }}
          <span class="text-gray-400 font-normal">({{ items.length }})</span>
        </h4>

        <div class="space-y-2">
          <RecipeReviewItem
            v-for="{ recipe, index } in items"
            :key="index"
            :recipe="recipe"
            :index="index"
            @update="(updates) => handleUpdate(index, updates)"
            @toggle-keep="handleToggleKeep(index)"
          />
        </div>
      </div>
    </div>

    <div class="flex justify-end pt-4 border-t">
      <button
        type="button"
        @click="handleSubmit"
        :disabled="disabled || stats.kept === 0"
        class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-gray-400 disabled:cursor-not-allowed"
      >
        <span v-if="disabled" class="flex items-center">
          <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
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
          Importing...
        </span>
        <span v-else>Confirm Import ({{ stats.kept }} recipes)</span>
      </button>
    </div>
  </div>
</template>
