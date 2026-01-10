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
  <div>
    <div class="max-w-6xl mx-auto">
      <!-- Header with merge button -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div class="page-header !mb-0 relative">
          <h1 class="page-title">Ingredients</h1>
          <!-- Decorative elements -->
          <div class="absolute -top-2 right-0 w-8 h-8 bg-accent rounded-full opacity-60 hidden sm:block" aria-hidden="true"></div>
          <div class="absolute top-4 right-12 w-4 h-4 bg-secondary hidden sm:block" style="transform: rotate(45deg);" aria-hidden="true"></div>
        </div>
        <div class="flex gap-3 flex-shrink-0">
          <button
            v-if="canMerge"
            type="button"
            class="btn-primary text-sm"
            @click="openMergeDialog"
          >
            Merge ({{ selectedCount }})
          </button>
          <button
            v-else-if="selectedCount > 0"
            type="button"
            class="btn-secondary text-sm opacity-50 cursor-not-allowed"
            disabled
          >
            Select 2+ to merge
          </button>
        </div>
      </div>

      <!-- Search and filters -->
      <div class="mb-8 space-y-4">
        <PageSearchBar
          v-model="query"
          placeholder="Search ingredients..."
          :show-filters-button="true"
          :filters-expanded="showFilters"
          @search="handleSearch"
          @update:filters-expanded="showFilters = $event"
        />

        <!-- Filter panel -->
        <div v-if="showFilters" class="filter-panel p-6 space-y-5 animate-pop-in">
          <div class="grid gap-5 sm:grid-cols-2">
            <div>
              <label class="block text-sm font-bold text-soft-black mb-2">
                Minimum recipes
              </label>
              <input
                v-model.number="minRecipeCount"
                type="number"
                min="0"
                placeholder="Any"
                class="input-field w-full"
              />
            </div>
            <div>
              <label class="block text-sm font-bold text-soft-black mb-2">
                Has aliases
              </label>
              <select
                v-model="hasDisambiguations"
                class="input-field w-full"
              >
                <option value="all">All</option>
                <option value="yes">Yes</option>
                <option value="no">No</option>
              </select>
            </div>
            <div class="sm:col-span-2">
              <label class="inline-flex items-center gap-3 cursor-pointer group">
                <span class="relative">
                  <input
                    type="checkbox"
                    v-model="availableNow"
                    class="sr-only peer"
                  />
                  <span class="w-6 h-6 rounded-lg border-3 border-soft-black bg-white flex items-center justify-center transition-all peer-checked:bg-accent peer-checked:border-accent peer-focus-visible:ring-2 peer-focus-visible:ring-offset-2 peer-focus-visible:ring-accent">
                    <svg v-if="availableNow" class="w-4 h-4 text-soft-black" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="4">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                  </span>
                </span>
                <span class="text-sm font-bold text-soft-black group-hover:text-primary transition-colors">
                  Ingredient available now
                </span>
              </label>
            </div>
          </div>
          <div class="flex gap-3 justify-end pt-2">
            <button
              type="button"
              class="btn-secondary !py-2 !px-4 text-sm"
              @click="clearFilters"
            >
              Clear
            </button>
            <button
              type="button"
              class="btn-primary !py-2 !px-4 text-sm"
              @click="applyFilters"
            >
              Apply
            </button>
          </div>
        </div>

        <!-- Error with retry -->
        <div v-if="error" class="card-pop p-5 border-primary flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-primary/5">
          <div class="flex items-center gap-3">
            <span class="w-10 h-10 flex-shrink-0 rounded-xl bg-primary flex items-center justify-center">
              <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </span>
            <p class="text-sm font-bold text-soft-black">{{ error }}</p>
          </div>
          <button
            type="button"
            class="btn-primary !py-2 !px-4 text-sm flex-shrink-0"
            @click="() => fetchIngredients()"
          >
            Retry
          </button>
        </div>
      </div>

      <!-- Loading skeleton -->
      <div v-if="isLoading" class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 pb-2 pr-2">
        <div v-for="i in 12" :key="i" class="card-pop p-5">
          <div class="h-6 skeleton-artistic w-3/4"/>
          <div class="h-4 skeleton-artistic w-1/2 mt-3"/>
          <div class="h-4 skeleton-artistic w-1/4 mt-4"/>
        </div>
      </div>

      <!-- Content -->
      <div v-else>
        <div v-if="ingredients.length === 0" class="empty-state">
          <div class="empty-state-icon">
            <svg class="w-10 h-10 text-charcoal" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
            </svg>
          </div>
          <p class="text-lg font-bold text-soft-black mb-2">No ingredients found</p>
          <p class="text-charcoal">Try adjusting your search or filters.</p>
        </div>
        <div v-else>
          <div class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 pb-2 pr-2">
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
            <div v-if="isLoadingMore" class="flex items-center gap-3 px-6 py-3 bg-white rounded-xl border-3 border-soft-black shadow-[4px_4px_0_var(--color-soft-black)]">
              <svg class="animate-spin h-5 w-5 text-primary" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <span class="font-bold text-soft-black">Loading more...</span>
            </div>
            <div v-else-if="!hasMore && ingredients.length > 0" class="inline-flex items-center gap-2 px-4 py-2 bg-cream rounded-xl border-2 border-soft-black/20">
              <span class="w-2 h-2 bg-accent rounded-full"></span>
              <span class="text-sm font-medium text-charcoal">End of list</span>
              <span class="w-2 h-2 bg-accent rounded-full"></span>
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
