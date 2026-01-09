import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getApiRecipes, type GetApiRecipesParams } from '../api/client'
import type { Recipe, RecipeListResponse, SearchFilters } from '../types/recipe'

export function useRecipeSearch() {
  const route = useRoute()
  const router = useRouter()

  const recipes = ref<Recipe[]>([])
  const nextCursor = ref<string | null>(null)
  const hasMore = ref(false)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  const filters = ref<SearchFilters>({
    q: '',
    ingredient: '',
    cookbookId: '',
    availableNow: false,
  })

  const hasFilters = computed(() => {
    return !!(filters.value.q || filters.value.ingredient || filters.value.cookbookId || filters.value.availableNow)
  })

  function initFromRoute() {
    filters.value = {
      q: (route.query.q as string) || '',
      ingredient: (route.query.ingredient as string) || '',
      cookbookId: (route.query.cookbookId as string) || '',
      availableNow: route.query.availableNow === 'true',
    }
  }

  function updateRouteFromFilters() {
    const query: Record<string, string> = {}
    if (filters.value.q) query.q = filters.value.q
    if (filters.value.ingredient) query.ingredient = filters.value.ingredient
    if (filters.value.cookbookId) query.cookbookId = filters.value.cookbookId
    if (filters.value.availableNow) query.availableNow = 'true'

    router.replace({ query })
  }

  async function searchRecipes(resetResults = true) {
    if (resetResults) {
      recipes.value = []
      nextCursor.value = null
    }

    isLoading.value = true
    error.value = null

    try {
      const params: GetApiRecipesParams = {
        limit: 20,
      }

      if (filters.value.q) params.q = filters.value.q
      if (filters.value.ingredient) params.ingredient = filters.value.ingredient
      if (filters.value.cookbookId) params.cookbookId = filters.value.cookbookId
      if (filters.value.availableNow) params.availableNow = true
      if (nextCursor.value) params.cursor = nextCursor.value

      const response = await getApiRecipes(params)

      if (response.status === 200) {
        const data = response.data as RecipeListResponse
        if (resetResults) {
          recipes.value = data.recipes
        } else {
          recipes.value = [...recipes.value, ...data.recipes]
        }
        nextCursor.value = data.nextCursor
        hasMore.value = data.hasMore
      } else {
        error.value = 'Failed to load recipes'
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'An error occurred'
    } finally {
      isLoading.value = false
    }
  }

  async function loadMore() {
    if (!hasMore.value || isLoading.value) return
    await searchRecipes(false)
  }

  function setSearchQuery(query: string) {
    filters.value.q = query
    updateRouteFromFilters()
    searchRecipes()
  }

  function setIngredientFilter(ingredient: string) {
    filters.value.ingredient = ingredient
    updateRouteFromFilters()
    searchRecipes()
  }

  function setCookbookFilter(cookbookId: string) {
    filters.value.cookbookId = cookbookId
    updateRouteFromFilters()
    searchRecipes()
  }

  function setAvailableNow(value: boolean) {
    filters.value.availableNow = value
    updateRouteFromFilters()
    searchRecipes()
  }

  function clearFilters() {
    filters.value = { q: '', ingredient: '', cookbookId: '', availableNow: false }
    updateRouteFromFilters()
    searchRecipes()
  }

  function removeFilter() {
    updateRouteFromFilters()
    searchRecipes()
  }

  watch(
    () => route.query,
    () => {
      initFromRoute()
    },
    { immediate: true }
  )

  return {
    recipes,
    nextCursor,
    hasMore,
    isLoading,
    error,
    filters,
    hasFilters,
    searchRecipes,
    loadMore,
    setSearchQuery,
    setIngredientFilter,
    setCookbookFilter,
    setAvailableNow,
    clearFilters,
    removeFilter,
  }
}
