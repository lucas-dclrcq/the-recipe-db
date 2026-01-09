<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useImportWizard } from '../composables/useImportWizard'
import CookbookForm from '../components/CookbookForm.vue'
import ImageUploader from '../components/ImageUploader.vue'
import OcrProgress from '../components/OcrProgress.vue'
import RecipeReviewList from '../components/RecipeReviewList.vue'
import type { OcrResultDto } from '../types/ocr'
import type { ReviewableRecipe } from '../composables/useImportWizard'

const router = useRouter()

const {
  currentStep,
  isLoading,
  error,
  cookbookId,
  formData,
  uploadedFiles,
  ocrResults,
  createCookbook,
  uploadIndexPages,
  setOcrResults,
  updateRecipe,
  toggleRecipeKeep,
  confirmImport,
} = useImportWizard()

const steps = [
  { id: 'form', name: 'Details', description: 'Enter cookbook info' },
  { id: 'upload', name: 'Upload', description: 'Add index pages' },
  { id: 'processing', name: 'Process', description: 'OCR extraction' },
  { id: 'review', name: 'Review', description: 'Confirm recipes' },
]

function getStepStatus(stepId: string) {
  const stepOrder = ['form', 'upload', 'processing', 'review', 'success']
  const currentIndex = stepOrder.indexOf(currentStep.value)
  const stepIndex = stepOrder.indexOf(stepId)

  if (stepIndex < currentIndex) return 'completed'
  if (stepIndex === currentIndex) return 'current'
  return 'upcoming'
}

async function handleFormSubmit() {
  await createCookbook()
}

async function handleUploadSubmit() {
  await uploadIndexPages()
}

function handleOcrComplete(results: OcrResultDto[]) {
  setOcrResults(results)
}

function handleOcrError(errorMsg: string) {
  console.error('OCR Error:', errorMsg)
}

function handleRecipeUpdate(index: number, updates: Partial<ReviewableRecipe>) {
  updateRecipe(index, updates)
}

function handleToggleKeep(index: number) {
  toggleRecipeKeep(index)
}

async function handleConfirmImport() {
  const success = await confirmImport()
  if (success) {
    router.push('/import/success')
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-4xl mx-auto px-4 py-8">
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">Import Cookbook</h1>
        <p class="mt-1 text-sm text-gray-600">
          Add your physical cookbook to the database for easy recipe searching
        </p>
      </div>

      <nav aria-label="Progress" class="mb-8">
        <ol class="flex items-center">
          <li
            v-for="(step, stepIdx) in steps"
            :key="step.id"
            :class="[stepIdx !== steps.length - 1 ? 'flex-1' : '', 'relative']"
          >
            <div
              v-if="stepIdx !== steps.length - 1"
              class="absolute top-4 left-0 -right-4 flex items-center"
              aria-hidden="true"
            >
              <div
                :class="[
                  'h-0.5 w-full',
                  getStepStatus(step.id) === 'completed' ? 'bg-indigo-600' : 'bg-gray-200',
                ]"
              ></div>
            </div>

            <div class="relative flex items-center justify-center group">
              <span class="flex flex-col items-center">
                <span
                  :class="[
                    'h-8 w-8 rounded-full flex items-center justify-center text-sm font-medium',
                    getStepStatus(step.id) === 'completed'
                      ? 'bg-indigo-600 text-white'
                      : getStepStatus(step.id) === 'current'
                        ? 'bg-indigo-600 text-white'
                        : 'bg-gray-200 text-gray-500',
                  ]"
                >
                  <svg
                    v-if="getStepStatus(step.id) === 'completed'"
                    class="h-5 w-5"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                  <span v-else>{{ stepIdx + 1 }}</span>
                </span>
                <span class="mt-2 text-xs font-medium text-gray-500">{{ step.name }}</span>
              </span>
            </div>
          </li>
        </ol>
      </nav>

      <div v-if="error" class="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
        <div class="flex">
          <svg
            class="h-5 w-5 text-red-400 mr-2"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <p class="text-sm text-red-700">{{ error }}</p>
        </div>
      </div>

      <div class="bg-white shadow rounded-lg p-6">
        <template v-if="currentStep === 'form'">
          <h2 class="text-lg font-medium text-gray-900 mb-6">Cookbook Details</h2>
          <CookbookForm v-model="formData" :disabled="isLoading" @submit="handleFormSubmit" />
        </template>

        <template v-else-if="currentStep === 'upload'">
          <h2 class="text-lg font-medium text-gray-900 mb-6">Upload Index Pages</h2>
          <p class="text-sm text-gray-600 mb-6">
            Upload photos of your cookbook's index pages. The pages will be processed in order.
          </p>
          <ImageUploader
            v-model="uploadedFiles"
            :disabled="isLoading"
            @submit="handleUploadSubmit"
          />
        </template>

        <template v-else-if="currentStep === 'processing'">
          <h2 class="text-lg font-medium text-gray-900 mb-6">Processing Index Pages</h2>
          <OcrProgress
            v-if="cookbookId"
            :cookbook-id="cookbookId"
            @complete="handleOcrComplete"
            @error="handleOcrError"
          />
        </template>

        <template v-else-if="currentStep === 'review'">
          <h2 class="text-lg font-medium text-gray-900 mb-6">Review Extracted Recipes</h2>
          <p class="text-sm text-gray-600 mb-6">
            Review the recipes extracted from your cookbook index. You can edit or skip any entries
            before importing.
          </p>
          <RecipeReviewList
            :recipes="ocrResults"
            :disabled="isLoading"
            @update="handleRecipeUpdate"
            @toggle-keep="handleToggleKeep"
            @submit="handleConfirmImport"
          />
        </template>
      </div>
    </div>
  </div>
</template>
