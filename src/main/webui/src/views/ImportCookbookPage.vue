<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useImportWizard } from '../composables/useImportWizard'
import CookbookForm from '../components/CookbookForm.vue'
import ImageUploader from '../components/ImageUploader.vue'
import OcrProgress from '../components/OcrProgress.vue'
import RecipeReviewList from '../components/RecipeReviewList.vue'
import type { OcrResultDto } from '../types/ocr'
import type { ReviewableRecipe } from '../composables/useImportWizard'
import {useI18n} from "vue-i18n";

const { t } = useI18n()
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
  <div>
    <div class="max-w-4xl mx-auto">
      <!-- Header with artistic styling -->
      <div class="page-header mb-8 relative">
        <h1 class="page-title">{{t('import.title')}}</h1>
        <p class="mt-2 text-charcoal">
          {{t('import.subtitle')}}
        </p>
        <!-- Decorative elements -->
        <div class="absolute -top-2 right-0 w-8 h-8 bg-secondary rounded-full opacity-60" aria-hidden="true"></div>
        <div class="absolute top-4 right-12 w-4 h-4 bg-accent" style="transform: rotate(45deg);" aria-hidden="true"></div>
      </div>

      <!-- Step progress indicator -->
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
                  'h-1 w-full rounded-full',
                  getStepStatus(step.id) === 'completed' ? 'bg-accent' : 'bg-soft-black/10',
                ]"
              ></div>
            </div>

            <div class="relative flex items-center justify-center group">
              <span class="flex flex-col items-center">
                <span
                  :class="[
                    'h-10 w-10 rounded-xl flex items-center justify-center text-sm font-bold border-3 transition-all',
                    getStepStatus(step.id) === 'completed'
                      ? 'bg-accent border-soft-black text-soft-black shadow-[3px_3px_0_var(--color-soft-black)]'
                      : getStepStatus(step.id) === 'current'
                        ? 'bg-primary border-soft-black text-white shadow-[3px_3px_0_var(--color-soft-black)]'
                        : 'bg-white border-soft-black/20 text-charcoal',
                  ]"
                >
                  <svg
                    v-if="getStepStatus(step.id) === 'completed'"
                    class="h-5 w-5"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    stroke-width="3"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                  <span v-else>{{ stepIdx + 1 }}</span>
                </span>
                <span :class="[
                  'mt-2 text-xs font-bold',
                  getStepStatus(step.id) === 'current' ? 'text-soft-black' : 'text-charcoal'
                ]">{{ step.name }}</span>
              </span>
            </div>
          </li>
        </ol>
      </nav>

      <!-- Error message -->
      <div v-if="error" class="mb-6 card-pop p-5 border-primary bg-primary/5">
        <div class="flex items-center gap-3">
          <span class="w-10 h-10 rounded-xl bg-primary flex items-center justify-center flex-shrink-0">
            <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </span>
          <p class="text-sm font-bold text-soft-black">{{ error }}</p>
        </div>
      </div>

      <!-- Main content card -->
      <div class="card-pop p-6">
        <template v-if="currentStep === 'form'">
          <h2 class="text-xl font-bold text-soft-black mb-6">{{t('import.cookbookDetails')}}</h2>
          <CookbookForm v-model="formData" :disabled="isLoading" @submit="handleFormSubmit" />
        </template>

        <template v-else-if="currentStep === 'upload'">
          <h2 class="text-xl font-bold text-soft-black mb-6">Upload Index Pages</h2>
          <p class="text-charcoal mb-6">
            Upload photos of your cookbook's index pages. The pages will be processed in order.
          </p>
          <ImageUploader
            v-model="uploadedFiles"
            :disabled="isLoading"
            @submit="handleUploadSubmit"
          />
        </template>

        <template v-else-if="currentStep === 'processing'">
          <h2 class="text-xl font-bold text-soft-black mb-6">Processing Index Pages</h2>
          <OcrProgress
            v-if="cookbookId"
            :cookbook-id="cookbookId"
            @complete="handleOcrComplete"
            @error="handleOcrError"
          />
        </template>

        <template v-else-if="currentStep === 'review'">
          <h2 class="text-xl font-bold text-soft-black mb-6">Review Extracted Recipes</h2>
          <p class="text-charcoal mb-6">
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
