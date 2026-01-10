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
  <div class="flex flex-col sm:flex-row gap-3">
    <div class="flex-1 relative">
      <input
        :value="localValue"
        type="search"
        :placeholder="placeholder ?? 'Search...'"
        class="input-field w-full pr-12"
        @input="handleInput"
        @keyup.enter="handleSubmit"
      />
      <!-- Search icon inside input -->
      <svg
        class="absolute right-4 top-1/2 -translate-y-1/2 w-5 h-5 text-charcoal/50 pointer-events-none"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        stroke-width="2"
      >
        <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
      </svg>
    </div>
    <div class="flex gap-3">
      <button
        type="button"
        class="btn-primary text-sm flex-1"
        @click="handleSubmit"
      >
        Search
      </button>
      <button
        v-if="showFiltersButton"
        type="button"
        class="btn-secondary text-sm flex-1 sm:flex-none flex items-center justify-center gap-2"
        :class="filtersExpanded ? 'bg-secondary border-secondary' : ''"
        @click="toggleFilters"
      >
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
        </svg>
        Filters
      </button>
    </div>
  </div>
</template>
