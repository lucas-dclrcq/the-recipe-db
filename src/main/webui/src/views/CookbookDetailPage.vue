<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { RouterLink } from 'vue-router'
import { ArrowLeftIcon, PhotoIcon, XMarkIcon, TrashIcon } from '@heroicons/vue/24/outline'
import { useCookbook } from '../composables/useCookbook'
import RecipeCard from '../components/RecipeCard.vue'

const route = useRoute()
const router = useRouter()
const { cookbook, recipes, isLoading, isDeleting, error, deleteError, fetchCookbook: fetchCookbookFromComposable, deleteCookbook, clearErrors } = useCookbook()

const showDeleteDialog = ref(false)

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

const ocrStatusCardClass = computed(() => {
  switch (cookbook.value?.ocrStatus) {
    case 'PROCESSING':
      return 'bg-electric-blue/10 border-electric-blue'
    case 'COMPLETED':
      return 'bg-accent/10 border-accent'
    case 'COMPLETED_WITH_ERRORS':
      return 'bg-secondary/20 border-secondary'
    case 'FAILED':
      return 'bg-primary/10 border-primary'
    default:
      return 'bg-cream border-soft-black/20'
  }
})

const ocrStatusIconClass = computed(() => {
  switch (cookbook.value?.ocrStatus) {
    case 'PROCESSING':
      return 'w-10 h-10 rounded-xl bg-electric-blue flex items-center justify-center text-white'
    case 'COMPLETED':
      return 'w-10 h-10 rounded-xl bg-accent flex items-center justify-center text-soft-black'
    case 'COMPLETED_WITH_ERRORS':
      return 'w-10 h-10 rounded-xl bg-secondary flex items-center justify-center text-soft-black'
    case 'FAILED':
      return 'w-10 h-10 rounded-xl bg-primary flex items-center justify-center text-white'
    default:
      return 'w-10 h-10 rounded-xl bg-charcoal flex items-center justify-center text-white'
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

function openDeleteDialog() {
  clearErrors()
  showDeleteDialog.value = true
}

function closeDeleteDialog() {
  showDeleteDialog.value = false
}

async function confirmDelete() {
  if (!cookbook.value) return
  const result = await deleteCookbook(cookbook.value.id)
  if (result.success) {
    router.push('/cookbooks')
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
  const id = route.params.id as string
  if (id) {
    fetchCookbookFromComposable(id)
  }
})
</script>

<template>
  <div>
    <div class="max-w-6xl mx-auto">
      <!-- Back button -->
      <button
        type="button"
        class="inline-flex items-center gap-2 text-sm font-bold text-charcoal hover:text-soft-black mb-6 transition-colors"
        @click="goBack"
      >
        <ArrowLeftIcon class="h-4 w-4" />
        Back
      </button>

      <!-- Loading state -->
      <div v-if="isLoading" class="flex justify-center py-12">
        <div class="flex items-center gap-3 px-6 py-3 bg-white rounded-xl border-3 border-soft-black shadow-[4px_4px_0_var(--color-soft-black)]">
          <svg class="animate-spin h-6 w-6 text-primary" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span class="font-bold text-soft-black">Loading...</span>
        </div>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="text-center py-12">
        <div class="empty-state">
          <div class="empty-state-icon bg-primary/10">
            <svg class="w-10 h-10 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <p class="text-lg font-bold text-soft-black mb-2">{{ error }}</p>
          <button
            type="button"
            class="btn-secondary !py-2 !px-4 text-sm"
            @click="goBack"
          >
            Go back
          </button>
        </div>
      </div>

      <template v-else-if="cookbook">
        <!-- Main cookbook card -->
        <div class="card-pop p-6 mb-8">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
            <!-- Cover image -->
            <div class="md:col-span-1">
              <div class="aspect-[3/4] bg-cream rounded-xl overflow-hidden border-3 border-soft-black shadow-[4px_4px_0_var(--color-soft-black)]">
                <img :src="coverUrl" alt="Cookbook cover" class="w-full h-full object-cover" />
              </div>
            </div>

            <!-- Cookbook details -->
            <div class="md:col-span-2">
              <h1 class="text-3xl font-bold text-soft-black">{{ cookbook.title }}</h1>
              <p v-if="cookbook.author" class="mt-2 text-lg text-charcoal">by {{ cookbook.author }}</p>

              <div class="mt-4 flex flex-wrap items-center gap-3">
                <span class="badge-page">{{ cookbook.recipeCount }} recipe{{ cookbook.recipeCount === 1 ? '' : 's' }}</span>
                <RouterLink
                  :to="{ path: '/', query: { cookbookId: cookbook.id } }"
                  class="text-sm font-bold text-primary hover:text-primary-dark transition-colors"
                >
                  Search within this cookbook
                </RouterLink>
              </div>

              <!-- Delete button -->
              <div class="mt-5">
                <button
                  type="button"
                  class="inline-flex items-center gap-2 px-4 py-2 text-sm font-bold text-primary bg-white border-3 border-primary rounded-xl hover:bg-primary/5 transition-colors"
                  @click="openDeleteDialog"
                >
                  <TrashIcon class="h-4 w-4" />
                  Delete Cookbook
                </button>
              </div>

              <!-- OCR Status Section -->
              <div v-if="cookbook.ocrStatus !== 'NONE'" class="mt-6 p-4 rounded-xl border-3" :class="ocrStatusCardClass">
                <div class="flex items-center justify-between flex-wrap gap-4">
                  <div class="flex items-center gap-3">
                    <span :class="ocrStatusIconClass">
                      <svg v-if="cookbook.ocrStatus === 'PROCESSING'" class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      <svg v-else-if="cookbook.ocrStatus === 'COMPLETED'" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                      </svg>
                      <svg v-else-if="cookbook.ocrStatus === 'FAILED'" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                      </svg>
                      <svg v-else class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                      </svg>
                    </span>
                    <div>
                      <p class="font-bold text-soft-black">{{ ocrStatusText }}</p>
                      <p v-if="cookbook.ocrErrorMessage" class="text-sm text-charcoal">{{ cookbook.ocrErrorMessage }}</p>
                    </div>
                  </div>
                  <RouterLink
                    v-if="cookbook.ocrStatus === 'COMPLETED' || cookbook.ocrStatus === 'COMPLETED_WITH_ERRORS'"
                    :to="`/cookbooks/${cookbook.id}/review`"
                    class="btn-primary !py-2 !px-4 text-sm"
                  >
                    Review Results
                  </RouterLink>
                  <RouterLink
                    v-else-if="cookbook.ocrStatus === 'PROCESSING'"
                    :to="`/cookbooks/${cookbook.id}/review`"
                    class="text-sm font-bold text-primary hover:text-primary-dark transition-colors"
                  >
                    View progress
                  </RouterLink>
                </div>
              </div>

              <!-- Cover upload section -->
              <div class="mt-6">
                <label class="block text-sm font-bold text-soft-black mb-3">Upload new cover</label>
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
                    'border-3 border-dashed rounded-xl p-6 text-center cursor-pointer transition-all',
                    isDragging ? 'border-primary bg-primary/5' : 'border-soft-black/30 hover:border-soft-black hover:bg-cream',
                  ]"
                >
                  <PhotoIcon class="mx-auto h-10 w-10 text-charcoal" />
                  <p class="mt-2 text-sm text-charcoal">
                    <span class="font-bold text-primary">Click to upload</span>
                    or drag and drop
                  </p>
                  <p class="mt-1 text-xs text-charcoal">JPEG or PNG</p>
                </div>

                <!-- Preview selected file -->
                <div v-else class="space-y-4">
                  <div class="relative inline-block">
                    <div class="w-32 aspect-[3/4] rounded-xl overflow-hidden bg-cream border-3 border-soft-black shadow-[3px_3px_0_var(--color-soft-black)]">
                      <img :src="previewUrl!" alt="Preview" class="w-full h-full object-cover" />
                    </div>
                    <button
                      type="button"
                      @click="clearSelection"
                      class="absolute -top-2 -right-2 bg-soft-black hover:bg-charcoal text-white p-1.5 rounded-lg shadow-sm transition-colors"
                      :disabled="isUploading"
                    >
                      <XMarkIcon class="h-4 w-4" />
                    </button>
                  </div>
                  <div class="flex items-center gap-3">
                    <button
                      type="button"
                      class="btn-primary !py-2 !px-4 text-sm"
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
                      class="btn-secondary !py-2 !px-4 text-sm"
                      :disabled="isUploading"
                      @click="clearSelection"
                    >
                      Cancel
                    </button>
                  </div>
                </div>

                <p v-if="uploadError" class="mt-3 text-sm font-bold text-primary">{{ uploadError }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Recipes section header -->
        <div class="page-header !mb-6 relative">
          <h2 class="text-xl font-bold text-soft-black">Recipes in this cookbook</h2>
        </div>

        <!-- Empty state -->
        <div v-if="recipes.length === 0" class="empty-state">
          <div class="empty-state-icon">
            <svg class="w-10 h-10 text-charcoal" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
            </svg>
          </div>
          <p class="text-lg font-bold text-soft-black mb-2">No recipes found</p>
          <p class="text-charcoal">This cookbook doesn't have any recipes yet.</p>
        </div>

        <!-- Recipe grid -->
        <div v-else class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <RecipeCard v-for="recipe in recipes" :key="recipe.id" :recipe="recipe" />
        </div>
      </template>
    </div>

    <!-- Delete Confirmation Dialog -->
    <div v-if="showDeleteDialog" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="flex min-h-screen items-center justify-center p-4">
        <div class="fixed inset-0 bg-soft-black/40" @click="closeDeleteDialog"></div>
        <div class="relative card-pop max-w-md w-full p-6 animate-pop-in">
          <h3 class="text-xl font-bold text-soft-black mb-3">Delete Cookbook</h3>
          <p class="text-charcoal mb-4">
            Are you sure you want to delete <strong class="text-soft-black">{{ cookbook?.title }}</strong>?
          </p>
          <p v-if="cookbook?.recipeCount && cookbook.recipeCount > 0" class="text-sm font-bold text-secondary-dark mb-4 p-3 bg-secondary/20 rounded-lg border-2 border-secondary">
            This will also delete {{ cookbook.recipeCount }} recipe{{ cookbook.recipeCount === 1 ? '' : 's' }} in this cookbook.
          </p>
          <p class="text-charcoal text-sm mb-4">
            This action cannot be undone.
          </p>

          <div v-if="deleteError" class="mb-4 p-3 bg-primary/10 border-2 border-primary rounded-lg">
            <p class="text-sm font-bold text-primary">{{ deleteError }}</p>
          </div>

          <div class="flex justify-end gap-3">
            <button
              type="button"
              class="btn-secondary !py-2 !px-4 text-sm"
              :disabled="isDeleting"
              @click="closeDeleteDialog"
            >
              Cancel
            </button>
            <button
              type="button"
              class="btn-primary !py-2 !px-4 text-sm"
              :disabled="isDeleting"
              @click="confirmDelete"
            >
              {{ isDeleting ? 'Deleting...' : 'Delete' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
