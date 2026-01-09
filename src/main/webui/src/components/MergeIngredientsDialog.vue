<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { Ingredient } from '../types/ingredient'

const props = defineProps<{
  show: boolean
  ingredients: Ingredient[]
  isMerging: boolean
  error: string | null
}>()

const emit = defineEmits<{
  close: []
  confirm: [targetId: string, sourceIds: string[]]
}>()

const selectedTargetId = ref<string | null>(null)

// Reset selection when dialog opens
watch(() => props.show, (isOpen) => {
  if (isOpen && props.ingredients.length > 0) {
    selectedTargetId.value = props.ingredients[0]?.id ?? null
  }
})

const targetIngredient = computed(() => {
  return props.ingredients.find(i => i.id === selectedTargetId.value)
})

const sourceIngredients = computed(() => {
  return props.ingredients.filter(i => i.id !== selectedTargetId.value)
})

const totalRecipeCount = computed(() => {
  return props.ingredients.reduce((sum, i) => sum + i.recipeCount, 0)
})

const allExistingAliases = computed(() => {
  return props.ingredients.flatMap(i => i.disambiguations)
})

function handleConfirm() {
  if (!selectedTargetId.value) return

  const sourceIds = sourceIngredients.value.map(i => i.id)
  emit('confirm', selectedTargetId.value, sourceIds)
}
</script>

<template>
  <div
    v-if="show"
    class="fixed inset-0 z-50 overflow-y-auto"
    aria-labelledby="merge-dialog-title"
    role="dialog"
    aria-modal="true"
  >
    <div class="flex min-h-screen items-center justify-center p-4">
      <!-- Backdrop -->
      <div
        class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"
        @click="emit('close')"
      />

      <!-- Dialog -->
      <div class="relative bg-white rounded-lg shadow-xl max-w-lg w-full p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 id="merge-dialog-title" class="text-lg font-semibold text-gray-900">
            Merge Ingredients
          </h2>
          <button
            type="button"
            class="text-gray-400 hover:text-gray-500"
            :disabled="isMerging"
            @click="emit('close')"
          >
            <span class="sr-only">Close</span>
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <p class="text-sm text-gray-600 mb-4">
          Select the primary ingredient (others will merge into it):
        </p>

        <!-- Ingredient selection list -->
        <div class="space-y-2 max-h-64 overflow-y-auto mb-4">
          <label
            v-for="ingredient in ingredients"
            :key="ingredient.id"
            class="flex items-start p-3 rounded-md border cursor-pointer transition-colors"
            :class="selectedTargetId === ingredient.id ? 'border-indigo-500 bg-indigo-50' : 'border-gray-200 hover:bg-gray-50'"
          >
            <input
              type="radio"
              :value="ingredient.id"
              v-model="selectedTargetId"
              class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500"
            />
            <div class="ml-3">
              <span class="font-medium text-gray-900 capitalize">{{ ingredient.name }}</span>
              <span class="text-gray-500 text-sm ml-2">({{ ingredient.recipeCount }} recipes)</span>
              <p v-if="ingredient.disambiguations.length > 0" class="text-sm text-gray-500 mt-1">
                Existing aliases: {{ ingredient.disambiguations.join(', ') }}
              </p>
              <p v-else class="text-sm text-gray-400 mt-1">No existing aliases</p>
            </div>
          </label>
        </div>

        <!-- Merge preview -->
        <div v-if="targetIngredient" class="bg-gray-50 rounded-md p-3 mb-4 text-sm">
          <p class="font-medium text-gray-700 mb-2">Preview:</p>
          <ul class="list-disc list-inside text-gray-600 space-y-1">
            <li>
              "<span class="capitalize">{{ targetIngredient.name }}</span>" will become the primary name
            </li>
            <li v-if="sourceIngredients.length > 0">
              {{ sourceIngredients.map(i => `"${i.name}"`).join(' and ') }}
              will be added as {{ sourceIngredients.length === 1 ? 'an alias' : 'aliases' }}
            </li>
            <li v-if="allExistingAliases.length > 0">
              Existing aliases ({{ allExistingAliases.join(', ') }}) will be kept
            </li>
            <li>
              {{ totalRecipeCount }} recipes will be updated
            </li>
          </ul>
        </div>

        <!-- Warning -->
        <div class="flex items-start gap-2 mb-4 text-amber-600 text-sm">
          <svg class="h-5 w-5 flex-shrink-0" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd" />
          </svg>
          <span>This action cannot be undone.</span>
        </div>

        <!-- Error message -->
        <p v-if="error" class="text-red-600 text-sm mb-4">{{ error }}</p>

        <!-- Actions -->
        <div class="flex justify-end gap-3">
          <button
            type="button"
            class="px-4 py-2 rounded-md border border-gray-300 bg-white hover:bg-gray-50 text-gray-700"
            :disabled="isMerging"
            @click="emit('close')"
          >
            Cancel
          </button>
          <button
            type="button"
            class="px-4 py-2 rounded-md bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-50"
            :disabled="isMerging || !selectedTargetId"
            @click="handleConfirm"
          >
            {{ isMerging ? 'Merging...' : 'Merge Ingredients' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
