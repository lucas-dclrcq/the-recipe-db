import { ref } from 'vue'
import { getApiCookbooksId, getApiCookbooksIdRecipes, deleteApiCookbooksId } from '../api/client'
import type { Cookbook } from '../types/cookbook'
import type { Recipe } from '../types/recipe'

export function useCookbook() {
  const cookbook = ref<Cookbook | null>(null)
  const recipes = ref<Recipe[]>([])
  const isLoading = ref(false)
  const isDeleting = ref(false)
  const error = ref<string | null>(null)
  const deleteError = ref<string | null>(null)

  async function fetchCookbook(id: string) {
    isLoading.value = true
    error.value = null

    try {
      const [cookbookResponse, recipesResponse] = await Promise.all([
        getApiCookbooksId(id),
        getApiCookbooksIdRecipes(id),
      ])

      if (cookbookResponse.status === 200) {
        cookbook.value = cookbookResponse.data as Cookbook
      } else {
        error.value = 'Cookbook not found'
        return
      }

      if (recipesResponse.status === 200) {
        recipes.value = recipesResponse.data as Recipe[]
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load cookbook'
    } finally {
      isLoading.value = false
    }
  }

  async function deleteCookbook(id: string) {
    isDeleting.value = true
    deleteError.value = null

    try {
      const response = await deleteApiCookbooksId(id)

      if (response.status === 200 || response.status === 204) {
        return { success: true }
      } else {
        deleteError.value = 'Failed to delete cookbook'
        return { success: false, error: deleteError.value }
      }
    } catch (e) {
      deleteError.value = e instanceof Error ? e.message : 'An error occurred'
      return { success: false, error: deleteError.value }
    } finally {
      isDeleting.value = false
    }
  }

  function clearErrors() {
    error.value = null
    deleteError.value = null
  }

  return {
    cookbook,
    recipes,
    isLoading,
    isDeleting,
    error,
    deleteError,
    fetchCookbook,
    deleteCookbook,
    clearErrors,
  }
}
