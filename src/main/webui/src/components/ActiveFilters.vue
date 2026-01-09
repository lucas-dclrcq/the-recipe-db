<script setup lang="ts">
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
  <div v-if="filters.q || filters.ingredient || filters.cookbookId" class="flex flex-wrap items-center gap-3">
    <span class="text-sm font-bold text-soft-black">Active filters:</span>

    <span
      v-if="filters.q"
      class="inline-flex items-center gap-2 px-3 py-1.5 rounded-xl text-sm font-bold bg-electric-blue text-white border-2 border-soft-black shadow-[2px_2px_0_var(--color-soft-black)]"
    >
      <span class="text-white/70">Name:</span> {{ filters.q }}
      <button
        type="button"
        class="flex-shrink-0 w-5 h-5 rounded-lg bg-white/20 hover:bg-white/30 inline-flex items-center justify-center transition-colors"
        @click="emit('removeFilter', 'q')"
        aria-label="Remove name filter"
      >
        <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </span>

    <span
      v-if="filters.ingredient"
      class="inline-flex items-center gap-2 px-3 py-1.5 rounded-xl text-sm font-bold bg-accent text-soft-black border-2 border-soft-black shadow-[2px_2px_0_var(--color-soft-black)]"
    >
      <span class="opacity-70">Ingredient:</span> {{ filters.ingredient }}
      <button
        type="button"
        class="flex-shrink-0 w-5 h-5 rounded-lg bg-soft-black/10 hover:bg-soft-black/20 inline-flex items-center justify-center transition-colors"
        @click="emit('removeFilter', 'ingredient')"
        aria-label="Remove ingredient filter"
      >
        <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </span>

    <span
      v-if="filters.cookbookId"
      class="inline-flex items-center gap-2 px-3 py-1.5 rounded-xl text-sm font-bold bg-pop-purple text-white border-2 border-soft-black shadow-[2px_2px_0_var(--color-soft-black)]"
    >
      <span class="text-white/70">Cookbook:</span> {{ cookbookTitle || 'Selected' }}
      <button
        type="button"
        class="flex-shrink-0 w-5 h-5 rounded-lg bg-white/20 hover:bg-white/30 inline-flex items-center justify-center transition-colors"
        @click="emit('removeFilter', 'cookbookId')"
        aria-label="Remove cookbook filter"
      >
        <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </span>

    <button
      type="button"
      class="text-sm font-bold text-primary hover:text-primary-dark underline underline-offset-2 transition-colors"
      @click="emit('clearAll')"
    >
      Clear all
    </button>
  </div>
</template>
