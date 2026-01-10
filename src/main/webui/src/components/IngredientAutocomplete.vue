<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useIngredientAutocomplete } from '../composables/useIngredientAutocomplete'
import type { Ingredient } from '../types/recipe'
import {useI18n} from "vue-i18n";

const { t } = useI18n()
const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  select: [ingredient: Ingredient]
}>()

const { query, suggestions, isLoading, isOpen, selectSuggestion, close } =
  useIngredientAutocomplete()

const containerRef = ref<HTMLElement | null>(null)
const highlightedIndex = ref(-1)

watch(
  () => props.modelValue,
  (newValue) => {
    query.value = newValue
  },
  { immediate: true }
)

watch(query, (newQuery) => {
  emit('update:modelValue', newQuery)
})

watch(suggestions, () => {
  highlightedIndex.value = -1
})

function handleSelect(ingredient: Ingredient) {
  selectSuggestion(ingredient)
  emit('select', ingredient)
}

function handleKeydown(event: KeyboardEvent) {
  if (!isOpen.value || suggestions.value.length === 0) return

  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      highlightedIndex.value = Math.min(highlightedIndex.value + 1, suggestions.value.length - 1)
      break
    case 'ArrowUp':
      event.preventDefault()
      highlightedIndex.value = Math.max(highlightedIndex.value - 1, 0)
      break
    case 'Enter': {
      event.preventDefault()
      const selected = suggestions.value[highlightedIndex.value]
      if (highlightedIndex.value >= 0 && selected) {
        handleSelect(selected)
      }
      break
    }
    case 'Escape':
      close()
      break
  }
}

function handleClickOutside(event: MouseEvent) {
  if (containerRef.value && !containerRef.value.contains(event.target as Node)) {
    close()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div ref="containerRef" class="relative">
    <input
      v-model="query"
      type="text"
      class="block w-full px-3 py-2 text-base border border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md disabled:bg-gray-100"
      placeholder="Search by ingredient..."
      role="combobox"
      aria-autocomplete="list"
      :aria-expanded="isOpen && suggestions.length > 0"
      :aria-activedescendant="highlightedIndex >= 0 ? `ingredient-option-${highlightedIndex}` : undefined"
      aria-controls="ingredient-listbox"
      @keydown="handleKeydown"
      @focus="isOpen = query.length >= 2"
    />

    <div v-if="isLoading" class="absolute right-3 top-1/2 -translate-y-1/2">
      <svg
        class="animate-spin h-4 w-4 text-gray-400"
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

    <ul
      v-if="isOpen && suggestions.length > 0"
      id="ingredient-listbox"
      role="listbox"
      class="absolute z-10 mt-1 w-full bg-white shadow-lg max-h-60 rounded-md py-1 text-base ring-1 ring-black ring-opacity-5 overflow-auto focus:outline-none sm:text-sm"
    >
      <li
        v-for="(ingredient, index) in suggestions"
        :id="`ingredient-option-${index}`"
        :key="ingredient.id"
        role="option"
        :aria-selected="highlightedIndex === index"
        :class="[
          'cursor-pointer select-none relative py-2 pl-3 pr-9',
          highlightedIndex === index ? 'bg-indigo-600 text-white' : 'text-gray-900',
        ]"
        @click="handleSelect(ingredient)"
        @mouseenter="highlightedIndex = index"
      >
        {{ ingredient.name }}
      </li>
    </ul>

    <div
      v-if="isOpen && query.length >= 2 && suggestions.length === 0 && !isLoading"
      class="absolute z-10 mt-1 w-full bg-white shadow-lg rounded-md py-2 px-3 text-sm text-gray-500 ring-1 ring-black ring-opacity-5"
    >
      {{t('ingredients.noIngredientsFound')}}
    </div>
  </div>
</template>
