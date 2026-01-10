<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { Ingredient } from '../types/ingredient'
import {useI18n} from "vue-i18n";

const { t } = useI18n()

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
  <div
    class="card-pop jiggling relative overflow-hidden group flex flex-col"
    :class="selected ? 'ring-4 ring-primary ring-offset-2' : ''"
  >
    <div class="absolute top-4 left-4 z-10">
      <label class="relative flex items-center justify-center w-6 h-6 cursor-pointer">
        <input
          type="checkbox"
          :checked="selected"
          :aria-label="`Select ${ingredient.name} for merge`"
          class="sr-only peer"
          @click="handleCheckboxClick"
          @keydown="handleCheckboxKeydown"
        />
        <span
          class="w-6 h-6 rounded-lg border-3 border-soft-black bg-white transition-all peer-checked:bg-primary peer-checked:border-primary peer-focus-visible:ring-2 peer-focus-visible:ring-offset-2 peer-focus-visible:ring-primary"
        >
          <svg
            v-if="selected"
            class="w-full h-full text-white p-0.5"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="4"
          >
            <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
          </svg>
        </span>
      </label>
    </div>

    <div class="absolute top-0 right-0 w-10 h-10 bg-accent" style="clip-path: polygon(100% 0, 0 0, 100% 100%);" aria-hidden="true"></div>

    <RouterLink
      :to="`/ingredients/${ingredient.id}`"
      class="block p-5 pl-14 flex-1"
    >
      <h3 class="text-lg font-black text-soft-black line-clamp-1 capitalize tracking-tight group-hover:text-primary transition-colors">
        {{ ingredient.name }}
      </h3>

      <p
        v-if="ingredient.disambiguations.length > 0"
        class="text-sm text-charcoal mt-2 line-clamp-1 flex items-center gap-2"
      >
        <span class="inline-flex items-center justify-center w-5 h-5 bg-secondary rounded-md border-2 border-soft-black text-xs font-bold">
          +
        </span>
        <span class="font-medium">{{ ingredient.disambiguations.join(', ') }}</span>
      </p>

      <div class="mt-4 flex items-center gap-2">
        <span class="inline-flex items-center px-3 py-1.5 text-xs font-bold text-soft-black bg-cream rounded-lg border-2 border-soft-black">
          {{ ingredient.recipeCount }} {{ t('recipes.badge')}}{{ ingredient.recipeCount === 1 ? '' : 's' }}
        </span>
      </div>
    </RouterLink>

    <div class="h-1 bg-gradient-to-r from-accent to-secondary" aria-hidden="true"></div>
  </div>
</template>
