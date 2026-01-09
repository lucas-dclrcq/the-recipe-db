<script setup lang="ts">
import { XMarkIcon } from '@heroicons/vue/20/solid'
import type { SearchFilters } from '../types/recipe'

defineProps<{
  filters: SearchFilters
  cookbookTitle?: string
}>()

const emit = defineEmits<{
  removeFilter: [key: keyof SearchFilters]
  clearAll: []
}>()
</script>

<template>
  <div v-if="filters.q || filters.ingredient || filters.cookbookId" class="flex flex-wrap items-center gap-2">
    <span class="text-sm text-gray-500">Filters:</span>

    <span
      v-if="filters.q"
      class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-sm font-medium bg-blue-100 text-blue-800"
    >
      Name: {{ filters.q }}
      <button
        type="button"
        class="flex-shrink-0 ml-0.5 h-4 w-4 rounded-full inline-flex items-center justify-center text-blue-400 hover:bg-blue-200 hover:text-blue-500 focus:outline-none focus:bg-blue-500 focus:text-white"
        @click="emit('removeFilter', 'q')"
      >
        <XMarkIcon class="h-3 w-3" />
      </button>
    </span>

    <span
      v-if="filters.ingredient"
      class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-sm font-medium bg-green-100 text-green-800"
    >
      Ingredient: {{ filters.ingredient }}
      <button
        type="button"
        class="flex-shrink-0 ml-0.5 h-4 w-4 rounded-full inline-flex items-center justify-center text-green-400 hover:bg-green-200 hover:text-green-500 focus:outline-none focus:bg-green-500 focus:text-white"
        @click="emit('removeFilter', 'ingredient')"
      >
        <XMarkIcon class="h-3 w-3" />
      </button>
    </span>

    <span
      v-if="filters.cookbookId"
      class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-sm font-medium bg-purple-100 text-purple-800"
    >
      Cookbook: {{ cookbookTitle || 'Selected' }}
      <button
        type="button"
        class="flex-shrink-0 ml-0.5 h-4 w-4 rounded-full inline-flex items-center justify-center text-purple-400 hover:bg-purple-200 hover:text-purple-500 focus:outline-none focus:bg-purple-500 focus:text-white"
        @click="emit('removeFilter', 'cookbookId')"
      >
        <XMarkIcon class="h-3 w-3" />
      </button>
    </span>

    <button
      type="button"
      class="text-sm text-gray-500 hover:text-gray-700 underline"
      @click="emit('clearAll')"
    >
      Clear all
    </button>
  </div>
</template>
