<script setup lang="ts">
import {onMounted, ref, watch} from 'vue'
import IngredientCard from '../components/IngredientCard.vue'
import MergeIngredientsDialog from '../components/MergeIngredientsDialog.vue'
import PageSearchBar from '../components/PageSearchBar.vue'
import {useIngredients} from '../composables/useIngredients'
import {useInfiniteScroll} from '../composables/useInfiniteScroll'
import type {IngredientFilters} from '../types/ingredient'

const query = ref('')
const minRecipeCount = ref<number | undefined>(undefined)
const hasDisambiguations = ref<'all' | 'yes' | 'no'>('all')
const availableNow = ref<boolean>(false)
const showFilters = ref(false)
const showMergeDialog = ref(false)
const isMerging = ref(false)
const mergeError = ref<string | null>(null)

const {
  ingredients,
  isLoading,
  isLoadingMore,
  error,
  hasMore,
  selectedCount,
  canMerge,
  fetchIngredients,
  loadMore,
  setFilters,
  toggleSelection,
  isSelected,
  getSelectedIngredients,
  mergeIngredients,
} = useIngredients()

const {targetRef: scrollTargetRef} = useInfiniteScroll(() => {
  if (hasMore.value && !isLoadingMore.value) {
    loadMore()
  }
})
// Used in template as ref="scrollTargetRef"
void scrollTargetRef

function handleSearch() {
  applyFilters()
}

function applyFilters() {
  const filters: IngredientFilters = {}
  if (query.value.trim()) {
    filters.q = query.value.trim()
  }
  if (minRecipeCount.value !== undefined && minRecipeCount.value > 0) {
    filters.minRecipeCount = minRecipeCount.value
  }
  if (hasDisambiguations.value !== 'all') {
    filters.hasDisambiguations = hasDisambiguations.value === 'yes'
  }
  if (availableNow.value) {
    filters.availableNow = true
  }
  setFilters(filters)
}

function clearFilters() {
  query.value = ''
  minRecipeCount.value = undefined
  hasDisambiguations.value = 'all'
  availableNow.value = false
  setFilters({})
}

watch(query, (v) => {
  if (!v || v.trim().length === 0) {
    applyFilters()
  }
})

function openMergeDialog() {
  if (!canMerge.value) return
  mergeError.value = null
  showMergeDialog.value = true
}

function closeMergeDialog() {
  showMergeDialog.value = false
  mergeError.value = null
}

async function handleMergeConfirm(targetId: string, sourceIds: string[]) {
  isMerging.value = true
  mergeError.value = null

  const result = await mergeIngredients(targetId, sourceIds)

  isMerging.value = false

  if (result.success) {
    closeMergeDialog()
  } else {
    mergeError.value = result.error || 'Failed to merge ingredients'
  }
}

onMounted(() => {
  fetchIngredients()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-6xl mx-auto px-4 py-8">
      <!-- Header with merge button -->
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold text-gray-900">Ingredients</h1>
        <div class="flex gap-2">
          <button
              v-if="canMerge"
              type="button"
              class="px-4 py-2 rounded-md bg-indigo-600 text-white hover:bg-indigo-700 text-sm font-medium"
              @click="openMergeDialog"
          >
            Merge ({{ selectedCount }})
          </button>
          <button
              v-else-if="selectedCount > 0"
              type="button"
              class="px-4 py-2 rounded-md bg-gray-300 text-gray-500 cursor-not-allowed text-sm font-medium"
              disabled
          >
            Select 2+ to merge
          </button>
        </div>
      </div>

      <!-- Search and filters -->
      <div class="mb-6 space-y-3">
        <PageSearchBar
            v-model="query"
            placeholder="Search ingredients..."
            :show-filters-button="true"
            :filters-expanded="showFilters"
            @search="handleSearch"
            @update:filters-expanded="showFilters = $event"
        />

        <!-- Filter panel -->
        <div v-if="showFilters" class="p-4 bg-white rounded-lg border border-gray-200 space-y-4">
          <div class="grid gap-4 sm:grid-cols-2">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Minimum recipes
              </label>
              <input
                  v-model.number="minRecipeCount"
                  type="number"
                  min="0"
                  placeholder="Any"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Has aliases
              </label>
              <select
                  v-model="hasDisambiguations"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                <option value="all">All</option>
                <option value="yes">Yes</option>
                <option value="no">No</option>
              </select>
            </div>
            <div class="sm:col-span-2">
              <label class="inline-flex items-center gap-2 text-sm text-gray-700">
                <input type="checkbox" v-model="availableNow" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
                Ingredient available now
              </label>
            </div>
          </div>
          <div class="flex gap-2 justify-end">
            <button
                type="button"
                class="px-3 py-1.5 text-sm rounded-md border border-gray-300 bg-white hover:bg-gray-50 text-gray-700"
                @click="clearFilters"
            >
              Clear
            </button>
            <button
                type="button"
                class="px-3 py-1.5 text-sm rounded-md bg-indigo-600 text-white hover:bg-indigo-700"
                @click="applyFilters"
            >
              Apply
            </button>
          </div>
        </div>

        <!-- Error with retry -->
        <div v-if="error" class="p-4 bg-red-50 border border-red-200 rounded-lg flex items-center justify-between">
          <p class="text-sm text-red-600">{{ error }}</p>
          <button
              type="button"
              class="px-3 py-1.5 text-sm rounded-md bg-red-100 text-red-700 hover:bg-red-200"
              @click="() => fetchIngredients()"
          >
            Retry
          </button>
        </div>
      </div>

      <!-- Loading skeleton -->
      <div v-if="isLoading" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <div v-for="i in 12" :key="i" class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
          <div class="h-5 bg-gray-200 rounded w-3/4 animate-pulse"/>
          <div class="h-4 bg-gray-200 rounded w-1/2 mt-2 animate-pulse"/>
          <div class="h-3 bg-gray-200 rounded w-1/4 mt-3 animate-pulse"/>
        </div>
      </div>

      <!-- Content -->
      <div v-else>
        <div v-if="ingredients.length === 0" class="text-center py-12 text-gray-500">
          No ingredients found.
        </div>
        <div v-else>
          <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <IngredientCard
                v-for="ingredient in ingredients"
                :key="ingredient.id"
                :ingredient="ingredient"
                :selected="isSelected(ingredient.id)"
                @toggle-select="toggleSelection"
            />
          </div>

          <!-- Infinite scroll trigger -->
          <div
              ref="scrollTargetRef"
              class="h-20 flex items-center justify-center"
          >
            <div v-if="isLoadingMore" class="text-gray-500">Loading more...</div>
            <div v-else-if="!hasMore && ingredients.length > 0" class="text-gray-400 text-sm">
              End of list
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Merge dialog -->
    <MergeIngredientsDialog
        :show="showMergeDialog"
        :ingredients="getSelectedIngredients()"
        :is-merging="isMerging"
        :error="mergeError"
        @close="closeMergeDialog"
        @confirm="handleMergeConfirm"
    />
  </div>
</template>
