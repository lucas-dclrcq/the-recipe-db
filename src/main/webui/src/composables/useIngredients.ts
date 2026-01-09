import { ref, computed } from 'vue'
import { getApiIngredients, postApiIngredientsMerge } from '../api/client'
import type { Ingredient, IngredientListResponse, IngredientFilters } from '../types/ingredient'

export function useIngredients() {
  const ingredients = ref<Ingredient[]>([])
  const isLoading = ref(false)
  const isLoadingMore = ref(false)
  const error = ref<string | null>(null)
  const nextCursor = ref<string | null>(null)
  const hasMore = ref(false)
  const selectedIds = ref<Set<string>>(new Set())
  const filters = ref<IngredientFilters>({})

  const selectedCount = computed(() => selectedIds.value.size)
  const canMerge = computed(() => selectedIds.value.size >= 2)

  async function fetchIngredients(resetList = true) {
    if (resetList) {
      isLoading.value = true
      ingredients.value = []
      nextCursor.value = null
    } else {
      isLoadingMore.value = true
    }
    error.value = null

    try {
      const response = await getApiIngredients({
        q: filters.value.q || undefined,
        minRecipeCount: filters.value.minRecipeCount,
        hasDisambiguations: filters.value.hasDisambiguations,
        availableNow: filters.value.availableNow,
        cursor: resetList ? undefined : (nextCursor.value || undefined),
        limit: 20,
      })

      if (response.status === 200) {
        const data = response.data as IngredientListResponse
        if (resetList) {
          ingredients.value = data.ingredients
        } else {
          ingredients.value = [...ingredients.value, ...data.ingredients]
        }
        nextCursor.value = data.nextCursor
        hasMore.value = data.hasMore
      } else {
        error.value = 'Failed to load ingredients'
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'An error occurred'
    } finally {
      isLoading.value = false
      isLoadingMore.value = false
    }
  }

  function loadMore() {
    if (hasMore.value && !isLoadingMore.value) {
      fetchIngredients(false)
    }
  }

  function setFilters(newFilters: IngredientFilters) {
    filters.value = newFilters
    selectedIds.value.clear()
    fetchIngredients(true)
  }

  function toggleSelection(id: string) {
    if (selectedIds.value.has(id)) {
      selectedIds.value.delete(id)
    } else {
      selectedIds.value.add(id)
    }
    // Force reactivity update
    selectedIds.value = new Set(selectedIds.value)
  }

  function clearSelection() {
    selectedIds.value = new Set()
  }

  function isSelected(id: string): boolean {
    return selectedIds.value.has(id)
  }

  function getSelectedIngredients(): Ingredient[] {
    return ingredients.value.filter(i => selectedIds.value.has(i.id))
  }

  async function mergeIngredients(targetId: string, sourceIds: string[]) {
    try {
      const response = await postApiIngredientsMerge({
        targetId,
        sourceIds,
      })

      if (response.status === 200) {
        // Refresh the list after merge
        clearSelection()
        await fetchIngredients(true)
        return { success: true }
      } else {
        return { success: false, error: 'Failed to merge ingredients' }
      }
    } catch (e) {
      return { success: false, error: e instanceof Error ? e.message : 'An error occurred' }
    }
  }

  return {
    ingredients,
    isLoading,
    isLoadingMore,
    error,
    hasMore,
    selectedIds,
    selectedCount,
    canMerge,
    filters,
    fetchIngredients,
    loadMore,
    setFilters,
    toggleSelection,
    clearSelection,
    isSelected,
    getSelectedIngredients,
    mergeIngredients,
  }
}
