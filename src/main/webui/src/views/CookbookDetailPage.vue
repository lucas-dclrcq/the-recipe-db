<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { RouterLink } from 'vue-router'
import { ArrowLeftIcon, PhotoIcon, XMarkIcon } from '@heroicons/vue/24/outline'
import { getApiCookbooksId, getApiCookbooksIdRecipes } from '../api/client'
import RecipeCard from '../components/RecipeCard.vue'
import type { Cookbook } from '../types/cookbook'
import type { Recipe } from '../types/recipe'

const route = useRoute()
const router = useRouter()

const cookbook = ref<Cookbook | null>(null)
const recipes = ref<Recipe[]>([])
const isLoading = ref(true)
const error = ref<string | null>(null)

const coverVersion = ref(0)
const coverUrl = computed(() => {
  if (!cookbook.value) return ''
  return cookbook.value.hasCover
    ? `/api/cookbooks/${cookbook.value.id}/cover?v=${coverVersion.value}`
    : '/default-cover.svg'
})

const uploadError = ref<string | null>(null)
const isUploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const isDragging = ref(false)

const previewUrl = computed(() => {
  if (!selectedFile.value) return null
  return URL.createObjectURL(selectedFile.value)
})

const ocrStatusClass = computed(() => {
  switch (cookbook.value?.ocrStatus) {
    case 'PROCESSING':
      return 'bg-blue-50 text-blue-800'
    case 'COMPLETED':
      return 'bg-green-50 text-green-800'
    case 'COMPLETED_WITH_ERRORS':
      return 'bg-yellow-50 text-yellow-800'
    case 'FAILED':
      return 'bg-red-50 text-red-800'
    default:
      return 'bg-gray-50 text-gray-800'
  }
})

const ocrStatusText = computed(() => {
  switch (cookbook.value?.ocrStatus) {
    case 'PROCESSING':
      return 'OCR processing in progress...'
    case 'COMPLETED':
      return 'OCR results ready for review'
    case 'COMPLETED_WITH_ERRORS':
      return 'OCR completed with some errors'
    case 'FAILED':
      return 'OCR processing failed'
    default:
      return ''
  }
})

function onFileChange(e: Event) {
  const t = e.target as HTMLInputElement
  const file = t.files && t.files.length > 0 ? t.files[0] : null
  if (file && isValidFile(file)) {
    selectedFile.value = file
    uploadError.value = null
  }
}

function isValidFile(file: File): boolean {
  const validTypes = ['image/jpeg', 'image/png']
  if (!validTypes.includes(file.type)) {
    uploadError.value = 'Please select a JPEG or PNG image'
    return false
  }
  return true
}

function handleDragOver(e: DragEvent) {
  e.preventDefault()
  isDragging.value = true
}

function handleDragLeave() {
  isDragging.value = false
}

function handleDrop(e: DragEvent) {
  e.preventDefault()
  isDragging.value = false
  if (isUploading.value) return

  const file = e.dataTransfer?.files?.[0]
  if (file && isValidFile(file)) {
    selectedFile.value = file
    uploadError.value = null
  }
}

function openFilePicker() {
  if (!isUploading.value) {
    fileInput.value?.click()
  }
}

function clearSelection() {
  selectedFile.value = null
  uploadError.value = null
  if (fileInput.value) fileInput.value.value = ''
}

async function uploadCover() {
  uploadError.value = null
  if (!cookbook.value) return
  if (!selectedFile.value) {
    uploadError.value = 'Please choose an image file (JPEG or PNG)'
    return
  }
  const form = new FormData()
  form.append('file', selectedFile.value)
  isUploading.value = true
  try {
    const res = await fetch(`/api/cookbooks/${cookbook.value.id}/cover`, {
      method: 'POST',
      body: form,
    })
    if (!res.ok) {
      const txt = await res.text()
      throw new Error(txt || `Upload failed (${res.status})`)
    }
    // success: ensure UI updates
    cookbook.value.hasCover = true
    coverVersion.value++
    // clear selection
    clearSelection()
  } catch (e) {
    uploadError.value = e instanceof Error ? e.message : 'Upload failed'
  } finally {
    isUploading.value = false
  }
}

async function fetchCookbook() {
  const id = route.params.id as string
  if (!id) {
    error.value = 'No cookbook ID provided'
    isLoading.value = false
    return
  }

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

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

onMounted(() => {
  fetchCookbook()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-6xl mx-auto px-4 py-8">
      <button
        type="button"
        class="inline-flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-6"
        @click="goBack"
      >
        <ArrowLeftIcon class="h-4 w-4" />
        Back
      </button>

      <div v-if="isLoading" class="flex justify-center py-12">
        <svg
          class="animate-spin h-8 w-8 text-indigo-600"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
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
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 px-6 py-8 mb-8">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
            <div class="md:col-span-1">
              <div class="aspect-[3/4] bg-gray-100 rounded overflow-hidden border border-gray-200">
                <img :src="coverUrl" alt="Cookbook cover" class="w-full h-full object-cover" />
              </div>
            </div>
            <div class="md:col-span-2">
              <h1 class="text-2xl font-bold text-gray-900">{{ cookbook.title }}</h1>
              <p v-if="cookbook.author" class="mt-1 text-lg text-gray-600">by {{ cookbook.author }}</p>
              <div class="mt-4 flex items-center gap-4 text-sm text-gray-500">
                <span>{{ cookbook.recipeCount }} recipe{{ cookbook.recipeCount === 1 ? '' : 's' }}</span>
                <RouterLink
                  :to="{ path: '/', query: { cookbookId: cookbook.id } }"
                  class="text-indigo-600 hover:text-indigo-800"
                >
                  Search within this cookbook
                </RouterLink>
              </div>

              <!-- OCR Status Section -->
              <div v-if="cookbook.ocrStatus !== 'NONE'" class="mt-6 p-4 rounded-lg" :class="ocrStatusClass">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <svg v-if="cookbook.ocrStatus === 'PROCESSING'" class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <svg v-else-if="cookbook.ocrStatus === 'COMPLETED'" class="h-5 w-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                    <svg v-else-if="cookbook.ocrStatus === 'FAILED'" class="h-5 w-5 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                    <svg v-else class="h-5 w-5 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                    <div>
                      <p class="font-medium">{{ ocrStatusText }}</p>
                      <p v-if="cookbook.ocrErrorMessage" class="text-sm opacity-75">{{ cookbook.ocrErrorMessage }}</p>
                    </div>
                  </div>
                  <RouterLink
                    v-if="cookbook.ocrStatus === 'COMPLETED' || cookbook.ocrStatus === 'COMPLETED_WITH_ERRORS'"
                    :to="`/cookbooks/${cookbook.id}/review`"
                    class="inline-flex items-center px-4 py-2 text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700"
                  >
                    Review Results
                  </RouterLink>
                  <RouterLink
                    v-else-if="cookbook.ocrStatus === 'PROCESSING'"
                    :to="`/cookbooks/${cookbook.id}/review`"
                    class="text-sm text-indigo-600 hover:text-indigo-800"
                  >
                    View progress
                  </RouterLink>
                </div>
              </div>

              <div class="mt-6">
                <label class="block text-sm font-medium text-gray-700 mb-2">Upload new cover</label>
                <input
                  ref="fileInput"
                  type="file"
                  accept="image/jpeg,image/png"
                  class="hidden"
                  @change="onFileChange"
                />

                <!-- Drop zone / file picker -->
                <div
                  v-if="!selectedFile"
                  @dragover="handleDragOver"
                  @dragleave="handleDragLeave"
                  @drop="handleDrop"
                  @click="openFilePicker"
                  :class="[
                    'border-2 border-dashed rounded-lg p-6 text-center cursor-pointer transition-colors',
                    isDragging ? 'border-indigo-500 bg-indigo-50' : 'border-gray-300 hover:border-gray-400',
                  ]"
                >
                  <PhotoIcon class="mx-auto h-10 w-10 text-gray-400" />
                  <p class="mt-2 text-sm text-gray-600">
                    <span class="font-medium text-indigo-600">Click to upload</span>
                    or drag and drop
                  </p>
                  <p class="mt-1 text-xs text-gray-500">JPEG or PNG</p>
                </div>

                <!-- Preview selected file -->
                <div v-else class="space-y-3">
                  <div class="relative inline-block">
                    <div class="w-32 aspect-[3/4] rounded-lg overflow-hidden bg-gray-100 border border-gray-200">
                      <img :src="previewUrl!" alt="Preview" class="w-full h-full object-cover" />
                    </div>
                    <button
                      type="button"
                      @click="clearSelection"
                      class="absolute -top-2 -right-2 bg-gray-600 hover:bg-gray-700 text-white p-1 rounded-full shadow-sm"
                      :disabled="isUploading"
                    >
                      <XMarkIcon class="h-4 w-4" />
                    </button>
                  </div>
                  <div class="flex items-center gap-3">
                    <button
                      type="button"
                      class="inline-flex items-center px-4 py-2 text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
                      :disabled="isUploading"
                      @click="uploadCover"
                    >
                      <svg
                        v-if="isUploading"
                        class="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                        fill="none"
                        viewBox="0 0 24 24"
                      >
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      {{ isUploading ? 'Uploading...' : 'Upload Cover' }}
                    </button>
                    <button
                      type="button"
                      class="px-4 py-2 text-sm font-medium rounded-md text-gray-700 bg-white border border-gray-300 hover:bg-gray-50"
                      :disabled="isUploading"
                      @click="clearSelection"
                    >
                      Cancel
                    </button>
                  </div>
                </div>

                <p v-if="uploadError" class="mt-2 text-sm text-red-600">{{ uploadError }}</p>
              </div>
            </div>
          </div>
        </div>

        <h2 class="text-lg font-semibold text-gray-900 mb-4">
          Recipes in this cookbook
        </h2>

        <div v-if="recipes.length === 0" class="text-center py-12 text-gray-500">
          No recipes found in this cookbook.
        </div>

        <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <RecipeCard v-for="recipe in recipes" :key="recipe.id" :recipe="recipe" />
        </div>
      </template>
    </div>
  </div>
</template>
