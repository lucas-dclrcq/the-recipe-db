<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  modelValue: string
  placeholder?: string
  showFiltersButton?: boolean
  filtersExpanded?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'update:filtersExpanded': [value: boolean]
  search: [query: string]
}>()

const localValue = ref(props.modelValue)

watch(
  () => props.modelValue,
  (newValue) => {
    localValue.value = newValue
  }
)

function handleSubmit() {
  emit('update:modelValue', localValue.value)
  emit('search', localValue.value)
}

function handleInput(event: Event) {
  const target = event.target as HTMLInputElement
  localValue.value = target.value
  emit('update:modelValue', localValue.value)
}

function toggleFilters() {
  emit('update:filtersExpanded', !props.filtersExpanded)
}
</script>

<template>
  <div class="flex gap-2">
    <input
      :value="localValue"
      type="search"
      :placeholder="placeholder ?? 'Search...'"
      class="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
      @input="handleInput"
      @keyup.enter="handleSubmit"
    />
    <button
      type="button"
      class="px-4 py-2 rounded-md bg-indigo-600 text-white hover:bg-indigo-700"
      @click="handleSubmit"
    >
      Search
    </button>
    <button
      v-if="showFiltersButton"
      type="button"
      class="px-4 py-2 rounded-md border border-gray-300 bg-white hover:bg-gray-50 text-gray-700"
      @click="toggleFilters"
    >
      Filters
    </button>
  </div>
</template>
