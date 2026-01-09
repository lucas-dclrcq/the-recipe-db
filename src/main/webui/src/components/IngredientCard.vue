<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { Ingredient } from '../types/ingredient'

const props = defineProps<{
  ingredient: Ingredient
  selected: boolean
}>()

const emit = defineEmits<{
  toggleSelect: [id: string]
}>()

function handleCheckboxClick(e: Event) {
  e.stopPropagation()
  emit('toggleSelect', props.ingredient.id)
}

function handleCheckboxKeydown(e: KeyboardEvent) {
  if (e.key === ' ' || e.key === 'Enter') {
    e.preventDefault()
    emit('toggleSelect', props.ingredient.id)
  }
}
</script>

<template>
  <div class="relative bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow">
    <div class="absolute top-3 left-3 z-10">
      <input
        type="checkbox"
        :checked="selected"
        :aria-label="`Select ${ingredient.name} for merge`"
        class="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 focus:ring-2 cursor-pointer"
        @click="handleCheckboxClick"
        @keydown="handleCheckboxKeydown"
      />
    </div>
    <RouterLink
      :to="`/ingredients/${ingredient.id}`"
      class="block p-4 pl-10"
    >
      <h3 class="text-base font-semibold text-gray-900 line-clamp-1 capitalize">{{ ingredient.name }}</h3>
      <p
        v-if="ingredient.disambiguations.length > 0"
        class="text-sm text-gray-500 mt-1 line-clamp-1"
      >
        Also: {{ ingredient.disambiguations.join(', ') }}
      </p>
      <p class="text-xs text-gray-500 mt-2">
        {{ ingredient.recipeCount }} recipe{{ ingredient.recipeCount === 1 ? '' : 's' }}
      </p>
    </RouterLink>
  </div>
</template>
