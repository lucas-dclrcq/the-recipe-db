<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { ReviewableRecipe } from '../composables/useImportWizard'

interface Props {
  recipe: ReviewableRecipe
  index: number
}

interface Emits {
  (e: 'update', updates: Partial<ReviewableRecipe>): void
  (e: 'toggle-keep'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const isEditing = ref(false)
const editedRecipeName = ref(props.recipe.recipeName || '')
const editedPageNumber = ref(props.recipe.pageNumber || 0)
const editedIngredient = ref(props.recipe.ingredient || '')

watch(
  () => props.recipe,
  (newRecipe) => {
    editedRecipeName.value = newRecipe.recipeName || ''
    editedPageNumber.value = newRecipe.pageNumber || 0
    editedIngredient.value = newRecipe.ingredient || ''
  }
)

const confidenceColor = computed(() => {
  const confidence = props.recipe.confidence || 0
  if (confidence >= 0.8) return 'text-green-600 bg-green-50'
  if (confidence >= 0.5) return 'text-yellow-600 bg-yellow-50'
  return 'text-red-600 bg-red-50'
})

const confidencePercentage = computed(() => {
  return Math.round((props.recipe.confidence || 0) * 100)
})

function startEditing() {
  isEditing.value = true
}

function saveEdits() {
  emit('update', {
    recipeName: editedRecipeName.value,
    pageNumber: editedPageNumber.value,
    ingredient: editedIngredient.value,
  })
  isEditing.value = false
}

function cancelEdits() {
  editedRecipeName.value = props.recipe.recipeName || ''
  editedPageNumber.value = props.recipe.pageNumber || 0
  editedIngredient.value = props.recipe.ingredient || ''
  isEditing.value = false
}

function toggleKeep() {
  emit('toggle-keep')
}
</script>

<template>
  <div
    :class="[
      'border rounded-lg p-4 transition-all',
      recipe.keep ? 'bg-white border-gray-200' : 'bg-gray-50 border-gray-200 opacity-60',
      recipe.needsReview ? 'ring-2 ring-yellow-400' : '',
    ]"
  >
    <div class="flex items-start justify-between gap-4">
      <div class="flex-1 min-w-0">
        <template v-if="!isEditing">
          <div class="flex items-center gap-2 mb-2">
            <h4 class="text-sm font-medium text-gray-900 truncate">
              {{ recipe.recipeName }}
            </h4>
            <span
              v-if="recipe.needsReview"
              class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800"
            >
              Needs Review
            </span>
          </div>

          <div class="flex flex-wrap items-center gap-4 text-sm text-gray-600">
            <span class="inline-flex items-center gap-1">
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
              Page {{ recipe.pageNumber }}
            </span>

            <span class="inline-flex items-center gap-1">
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"
                />
              </svg>
              {{ recipe.ingredient }}
            </span>

            <span
              :class="['inline-flex items-center px-2 py-0.5 rounded text-xs font-medium', confidenceColor]"
            >
              {{ confidencePercentage }}% confidence
            </span>
          </div>
        </template>

        <template v-else>
          <div class="space-y-3">
            <div>
              <label class="block text-xs font-medium text-gray-700">Recipe Name</label>
              <input
                v-model="editedRecipeName"
                type="text"
                class="mt-1 block w-full rounded border-gray-300 shadow-sm text-sm focus:border-indigo-500 focus:ring-indigo-500 px-2 py-1 border"
              />
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-medium text-gray-700">Page Number</label>
                <input
                  v-model.number="editedPageNumber"
                  type="number"
                  min="1"
                  class="mt-1 block w-full rounded border-gray-300 shadow-sm text-sm focus:border-indigo-500 focus:ring-indigo-500 px-2 py-1 border"
                />
              </div>

              <div>
                <label class="block text-xs font-medium text-gray-700">Ingredient</label>
                <input
                  v-model="editedIngredient"
                  type="text"
                  class="mt-1 block w-full rounded border-gray-300 shadow-sm text-sm focus:border-indigo-500 focus:ring-indigo-500 px-2 py-1 border"
                />
              </div>
            </div>
          </div>
        </template>
      </div>

      <div class="flex items-center gap-2">
        <template v-if="!isEditing">
          <button
            @click="startEditing"
            class="p-1.5 text-gray-400 hover:text-gray-600 rounded"
            title="Edit"
          >
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
              />
            </svg>
          </button>

          <button
            @click="toggleKeep"
            :class="[
              'p-1.5 rounded transition-colors',
              recipe.keep
                ? 'text-green-600 hover:text-red-600'
                : 'text-gray-400 hover:text-green-600',
            ]"
            :title="recipe.keep ? 'Skip this recipe' : 'Include this recipe'"
          >
            <svg v-if="recipe.keep" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M5 13l4 4L19 7"
              />
            </svg>
            <svg v-else class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </template>

        <template v-else>
          <button
            @click="saveEdits"
            class="p-1.5 text-green-600 hover:text-green-700 rounded"
            title="Save"
          >
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M5 13l4 4L19 7"
              />
            </svg>
          </button>

          <button
            @click="cancelEdits"
            class="p-1.5 text-gray-400 hover:text-gray-600 rounded"
            title="Cancel"
          >
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </template>
      </div>
    </div>
  </div>
</template>
