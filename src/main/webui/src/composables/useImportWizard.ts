import { ref, computed, reactive } from 'vue'
import type { OcrResultDto } from '../types/ocr'
import {
  postApiCookbooks,
  postApiCookbooksIdIndexPages,
  postApiCookbooksIdConfirm,
  type ConfirmedRecipe,
} from '../api/client'

export type WizardStep = 'form' | 'upload' | 'processing' | 'review' | 'success'

export interface CookbookFormData {
  title: string
  author: string
}

export interface ReviewableRecipe extends OcrResultDto {
  keep: boolean
  edited: boolean
}

export function useImportWizard() {
  const currentStep = ref<WizardStep>('form')
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  const cookbookId = ref<string | null>(null)
  const formData = reactive<CookbookFormData>({
    title: '',
    author: '',
  })

  const uploadedFiles = ref<File[]>([])
  const ocrResults = ref<ReviewableRecipe[]>([])

  const canProceed = computed(() => {
    switch (currentStep.value) {
      case 'form':
        return formData.title.trim() !== '' && formData.author.trim() !== ''
      case 'upload':
        return uploadedFiles.value.length > 0
      case 'review':
        return ocrResults.value.some((r) => r.keep)
      default:
        return false
    }
  })

  const recipesToKeep = computed(() => ocrResults.value.filter((r) => r.keep))
  const recipesToSkip = computed(() => ocrResults.value.filter((r) => !r.keep))

  async function createCookbook(): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await postApiCookbooks({
        title: formData.title,
        author: formData.author,
      })

      if (response.status >= 200 && response.status < 300) {
        cookbookId.value = (response.data as { id: string }).id
        currentStep.value = 'upload'
        return true
      } else {
        error.value = 'Failed to create cookbook'
        return false
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error occurred'
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function uploadIndexPages(): Promise<boolean> {
    if (!cookbookId.value) {
      error.value = 'No cookbook ID'
      return false
    }

    isLoading.value = true
    error.value = null

    try {
      const response = await postApiCookbooksIdIndexPages(cookbookId.value, {
        files: uploadedFiles.value,
      })

      if (response.status === 200) {
        currentStep.value = 'processing'
        return true
      } else {
        error.value = 'Failed to upload index pages'
        return false
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error occurred'
      return false
    } finally {
      isLoading.value = false
    }
  }

  function setOcrResults(results: OcrResultDto[]) {
    ocrResults.value = results.map((r) => ({
      ...r,
      keep: true,
      edited: false,
    }))
    currentStep.value = 'review'
  }

  function updateRecipe(index: number, updates: Partial<ReviewableRecipe>) {
    const recipe = ocrResults.value[index]
    if (recipe) {
      Object.assign(recipe, updates)
      if (
        updates.recipeName !== undefined ||
        updates.pageNumber !== undefined ||
        updates.ingredient !== undefined
      ) {
        recipe.edited = true
      }
    }
  }

  function toggleRecipeKeep(index: number) {
    const recipe = ocrResults.value[index]
    if (recipe) {
      recipe.keep = !recipe.keep
    }
  }

  async function confirmImport(): Promise<boolean> {
    if (!cookbookId.value) {
      error.value = 'No cookbook ID'
      return false
    }

    isLoading.value = true
    error.value = null

    try {
      const recipes: ConfirmedRecipe[] = ocrResults.value.map((r) => ({
        recipeName: r.recipeName,
        pageNumber: r.pageNumber,
        ingredient: r.ingredient,
        keep: r.keep,
      }))

      const response = await postApiCookbooksIdConfirm(cookbookId.value, {
        recipes,
      })

      if (response.status === 200) {
        currentStep.value = 'success'
        return true
      } else {
        error.value = 'Failed to confirm import'
        return false
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error occurred'
      return false
    } finally {
      isLoading.value = false
    }
  }

  function reset() {
    currentStep.value = 'form'
    isLoading.value = false
    error.value = null
    cookbookId.value = null
    formData.title = ''
    formData.author = ''
    uploadedFiles.value = []
    ocrResults.value = []
  }

  function goToStep(step: WizardStep) {
    currentStep.value = step
  }

  return {
    // State
    currentStep,
    isLoading,
    error,
    cookbookId,
    formData,
    uploadedFiles,
    ocrResults,

    // Computed
    canProceed,
    recipesToKeep,
    recipesToSkip,

    // Actions
    createCookbook,
    uploadIndexPages,
    setOcrResults,
    updateRecipe,
    toggleRecipeKeep,
    confirmImport,
    reset,
    goToStep,
  }
}
