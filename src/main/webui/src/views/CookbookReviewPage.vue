<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import RecipeReviewList from '../components/RecipeReviewList.vue'
import { getApiCookbooksId, getGetApiCookbooksIdOcrResultsUrl, postApiCookbooksIdConfirm } from '../api/client'
import type { OcrResultDto } from '../types/ocr'
import type { Cookbook } from '../types/cookbook'
import type { ConfirmedRecipe } from '../api/client'

interface ReviewableRecipe extends OcrResultDto {
  keep: boolean
  edited: boolean
}

const route = useRoute()
const router = useRouter()

const cookbook = ref<Cookbook | null>(null)
const ocrResults = ref<ReviewableRecipe[]>([])
const isLoading = ref(true)
const isSubmitting = ref(false)
const error = ref<string | null>(null)

let pollInterval: ReturnType<typeof setInterval> | null = null

const isProcessing = computed(() => cookbook.value?.ocrStatus === 'PROCESSING')
const hasResults = computed(() =>
  cookbook.value?.ocrStatus === 'COMPLETED' ||
  cookbook.value?.ocrStatus === 'COMPLETED_WITH_ERRORS'
)
const hasFailed = computed(() => cookbook.value?.ocrStatus === 'FAILED')

async function fetchData() {
  const id = route.params.id as string
  if (!id) {
    error.value = 'No cookbook ID provided'
    isLoading.value = false
    return
  }

  try {
    const cookbookRes = await getApiCookbooksId(id)

    if (cookbookRes.status === 200) {
      cookbook.value = cookbookRes.data as Cookbook
    } else {
      error.value = 'Cookbook not found'
      return
    }

    // Fetch OCR results using direct fetch
    const resultsRes = await fetch(getGetApiCookbooksIdOcrResultsUrl(id))
    if (resultsRes.ok) {
      const data = await resultsRes.json()
      ocrResults.value = (data.results || []).map((r: OcrResultDto) => ({
        ...r,
        keep: true,
        edited: false,
      }))
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to load data'
  } finally {
    isLoading.value = false
  }
}

function startPolling() {
  if (pollInterval) return
  pollInterval = setInterval(async () => {
    if (!cookbook.value || cookbook.value.ocrStatus !== 'PROCESSING') {
      stopPolling()
      return
    }
    await fetchData()
  }, 3000)
}

function stopPolling() {
  if (pollInterval) {
    clearInterval(pollInterval)
    pollInterval = null
  }
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

async function confirmImport() {
  if (!cookbook.value) return

  isSubmitting.value = true
  error.value = null

  try {
    const recipes: ConfirmedRecipe[] = ocrResults.value.map((r) => ({
      recipeName: r.recipeName,
      pageNumber: r.pageNumber,
      ingredient: r.ingredient,
      keep: r.keep,
    }))

    const response = await postApiCookbooksIdConfirm(cookbook.value.id, { recipes })

    if (response.status === 200) {
      router.push(`/cookbooks/${cookbook.value.id}`)
    } else {
      error.value = 'Failed to confirm import'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Unknown error occurred'
  } finally {
    isSubmitting.value = false
  }
}

function goBack() {
  router.push(`/cookbooks/${route.params.id}`)
}

onMounted(() => {
  fetchData().then(() => {
    if (cookbook.value?.ocrStatus === 'PROCESSING') {
      startPolling()
    }
  })
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-4xl mx-auto px-4 py-8">
      <button
        type="button"
        class="inline-flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-6"
        @click="goBack"
      >
        <ArrowLeftIcon class="h-4 w-4" />
        Back to cookbook
      </button>

      <div v-if="isLoading" class="flex justify-center py-12">
        <svg class="animate-spin h-8 w-8 text-indigo-600" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      </div>

      <div v-else-if="error" class="text-center py-12">
        <p class="text-red-600">{{ error }}</p>
        <button
          type="button"
          class="mt-4 text-indigo-600 hover:text-indigo-800"
          @click="goBack"
        >
          Go back
        </button>
      </div>

      <template v-else-if="cookbook">
        <div class="mb-8">
          <h1 class="text-2xl font-bold text-gray-900">Review OCR Results</h1>
          <p class="mt-1 text-sm text-gray-600">{{ cookbook.title }} by {{ cookbook.author }}</p>
        </div>

        <!-- Processing state -->
        <div v-if="isProcessing" class="bg-white shadow rounded-lg p-8 text-center">
          <svg class="animate-spin mx-auto h-12 w-12 text-indigo-600 mb-4" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <h3 class="text-lg font-medium text-gray-900">Processing index pages...</h3>
          <p class="mt-2 text-sm text-gray-600">
            This may take a few minutes. You can leave this page and come back later.
          </p>
        </div>

        <!-- Failed state -->
        <div v-else-if="hasFailed" class="bg-white shadow rounded-lg p-8 text-center">
          <svg class="mx-auto h-12 w-12 text-red-500 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <h3 class="text-lg font-medium text-gray-900">OCR Processing Failed</h3>
          <p v-if="cookbook.ocrErrorMessage" class="mt-2 text-sm text-red-600">
            {{ cookbook.ocrErrorMessage }}
          </p>
          <p class="mt-4 text-sm text-gray-600">
            Please try uploading the index pages again.
          </p>
        </div>

        <!-- Results ready -->
        <div v-else-if="hasResults" class="bg-white shadow rounded-lg p-6">
          <div v-if="cookbook.ocrStatus === 'COMPLETED_WITH_ERRORS'" class="mb-6 bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div class="flex">
              <svg class="h-5 w-5 text-yellow-400 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <p class="text-sm text-yellow-700">
                {{ cookbook.ocrErrorMessage || 'Some pages failed to process. Partial results are shown below.' }}
              </p>
            </div>
          </div>

          <div v-if="ocrResults.length === 0" class="text-center py-8 text-gray-500">
            No recipes were extracted. Please check the index page images and try again.
          </div>

          <RecipeReviewList
            v-else
            :recipes="ocrResults"
            :disabled="isSubmitting"
            @update="updateRecipe"
            @toggle-keep="toggleRecipeKeep"
            @submit="confirmImport"
          />
        </div>

        <!-- No OCR status -->
        <div v-else class="bg-white shadow rounded-lg p-8 text-center">
          <p class="text-gray-600">No OCR processing has been started for this cookbook.</p>
          <button
            type="button"
            class="mt-4 text-indigo-600 hover:text-indigo-800"
            @click="goBack"
          >
            Go back to cookbook
          </button>
        </div>
      </template>
    </div>
  </div>
</template>
