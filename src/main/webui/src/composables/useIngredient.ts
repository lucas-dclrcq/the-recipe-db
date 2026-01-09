import { ref } from 'vue'
import { getApiIngredientsId, putApiIngredientsId, deleteApiIngredientsId } from '../api/client'
import type { IngredientDetail } from '../types/ingredient'

export function useIngredient() {
  const ingredient = ref<IngredientDetail | null>(null)
  const isLoading = ref(false)
  const isSaving = ref(false)
  const isDeleting = ref(false)
  const error = ref<string | null>(null)
  const saveError = ref<string | null>(null)
  const deleteError = ref<string | null>(null)

  async function fetchIngredient(id: string) {
    isLoading.value = true
    error.value = null

    try {
      const response = await getApiIngredientsId(id)

      if (response.status === 200) {
        ingredient.value = response.data as IngredientDetail
      } else {
        error.value = 'Ingredient not found'
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load ingredient'
    } finally {
      isLoading.value = false
    }
  }

  async function updateIngredient(id: string, name: string, disambiguations: string[], availableMonths?: number[]) {
    isSaving.value = true
    saveError.value = null

    try {
      const response = await putApiIngredientsId(id, { name, disambiguations, availableMonths })

      if (response.status === 200) {
        ingredient.value = response.data as IngredientDetail
        return { success: true }
      } else {
        saveError.value = 'Failed to update ingredient'
        return { success: false, error: saveError.value }
      }
    } catch (e) {
      saveError.value = e instanceof Error ? e.message : 'An error occurred'
      return { success: false, error: saveError.value }
    } finally {
      isSaving.value = false
    }
  }

  async function deleteIngredient(id: string) {
    isDeleting.value = true
    deleteError.value = null

    try {
      const response = await deleteApiIngredientsId(id)

      if (response.status === 200 || response.status === 204) {
        return { success: true }
      } else {
        deleteError.value = 'Failed to delete ingredient'
        return { success: false, error: deleteError.value }
      }
    } catch (e) {
      const errorMessage = e instanceof Error ? e.message : 'An error occurred'
      if (errorMessage.includes('409') || errorMessage.includes('recipes')) {
        deleteError.value = 'Cannot delete ingredient that is used in recipes'
      } else {
        deleteError.value = errorMessage
      }
      return { success: false, error: deleteError.value }
    } finally {
      isDeleting.value = false
    }
  }

  function clearErrors() {
    error.value = null
    saveError.value = null
    deleteError.value = null
  }

  return {
    ingredient,
    isLoading,
    isSaving,
    isDeleting,
    error,
    saveError,
    deleteError,
    fetchIngredient,
    updateIngredient,
    deleteIngredient,
    clearErrors,
  }
}
