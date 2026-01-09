<script setup lang="ts">
import type { CookbookFormData } from '../composables/useImportWizard'

interface Props {
  modelValue: CookbookFormData
  disabled?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: CookbookFormData): void
  (e: 'submit'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

function updateField(field: keyof CookbookFormData, value: string) {
  // Directly mutate the reactive object since v-model passes a reactive reference
  // This ensures the parent's reactive state updates properly
  props.modelValue[field] = value
}

function handleSubmit(event: Event) {
  event.preventDefault()
  emit('submit')
}
</script>

<template>
  <form @submit="handleSubmit" class="space-y-6">
    <div>
      <label for="title" class="block text-sm font-medium text-gray-700">
        Cookbook Title
      </label>
      <input
        id="title"
        type="text"
        :value="modelValue.title"
        @input="updateField('title', ($event.target as HTMLInputElement).value)"
        :disabled="disabled"
        required
        placeholder="e.g., The Joy of Cooking"
        class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm disabled:bg-gray-100 disabled:cursor-not-allowed px-3 py-2 border"
      />
    </div>

    <div>
      <label for="author" class="block text-sm font-medium text-gray-700">
        Author
      </label>
      <input
        id="author"
        type="text"
        :value="modelValue.author"
        @input="updateField('author', ($event.target as HTMLInputElement).value)"
        :disabled="disabled"
        required
        placeholder="e.g., Irma S. Rombauer"
        class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm disabled:bg-gray-100 disabled:cursor-not-allowed px-3 py-2 border"
      />
    </div>

    <div class="flex justify-end">
      <button
        type="submit"
        :disabled="disabled || !modelValue.title.trim() || !modelValue.author.trim()"
        class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-gray-400 disabled:cursor-not-allowed"
      >
        <span v-if="disabled" class="flex items-center">
          <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          Creating...
        </span>
        <span v-else>Continue</span>
      </button>
    </div>
  </form>
</template>
