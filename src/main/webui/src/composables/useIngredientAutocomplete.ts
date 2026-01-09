import { ref, watch } from 'vue'
import { getApiIngredients } from '../api/client'
import type { Ingredient } from '../types/recipe'

interface IngredientListResponse {
  ingredients: Array<{ id: string; name: string; disambiguations: string[]; recipeCount: number }>
  nextCursor: string | null
  hasMore: boolean
}

export function useIngredientAutocomplete() {
  const query = ref('')
  const suggestions = ref<Ingredient[]>([])
  const isLoading = ref(false)
  const isOpen = ref(false)

  let debounceTimeout: ReturnType<typeof setTimeout> | null = null

  async function fetchSuggestions(prefix: string) {
    if (prefix.length < 2) {
      suggestions.value = []
      return
    }

    isLoading.value = true

    try {
      const response = await getApiIngredients({ q: prefix, limit: 10 })

      if (response.status === 200) {
        const data = response.data as IngredientListResponse
        // Map to simple Ingredient type (id, name only)
        suggestions.value = data.ingredients.map(i => ({ id: i.id, name: i.name }))
      }
    } catch {
      suggestions.value = []
    } finally {
      isLoading.value = false
    }
  }

  function debouncedFetch(prefix: string) {
    if (debounceTimeout) {
      clearTimeout(debounceTimeout)
    }

    debounceTimeout = setTimeout(() => {
      fetchSuggestions(prefix)
    }, 300)
  }

  watch(query, (newQuery) => {
    if (newQuery.length >= 2) {
      isOpen.value = true
      debouncedFetch(newQuery)
    } else {
      suggestions.value = []
      isOpen.value = false
    }
  })

  function selectSuggestion(ingredient: Ingredient) {
    query.value = ingredient.name
    isOpen.value = false
    suggestions.value = []
  }

  function clear() {
    query.value = ''
    suggestions.value = []
    isOpen.value = false
  }

  function close() {
    isOpen.value = false
  }

  return {
    query,
    suggestions,
    isLoading,
    isOpen,
    selectSuggestion,
    clear,
    close,
  }
}
