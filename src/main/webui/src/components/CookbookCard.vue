<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import type { Cookbook } from '../types/cookbook'

const props = defineProps<{
  cookbook: Cookbook
}>()

const ocrBadge = computed(() => {
  switch (props.cookbook.ocrStatus) {
    case 'PROCESSING':
      return { text: 'Processing...', class: 'bg-blue-100 text-blue-800' }
    case 'COMPLETED':
      return { text: 'Ready to review', class: 'bg-green-100 text-green-800' }
    case 'COMPLETED_WITH_ERRORS':
      return { text: 'Partial results', class: 'bg-yellow-100 text-yellow-800' }
    case 'FAILED':
      return { text: 'OCR failed', class: 'bg-red-100 text-red-800' }
    default:
      return null
  }
})
</script>

<template>
  <RouterLink
    :to="`/cookbooks/${cookbook.id}`"
    class="block bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow"
  >
    <div class="aspect-[3/4] bg-gray-100 flex items-center justify-center overflow-hidden relative">
      <img
        :src="cookbook.hasCover ? `/api/cookbooks/${cookbook.id}/cover` : '/default-cover.svg'"
        alt="Cookbook cover"
        class="w-full h-full object-cover"
        loading="lazy"
      />
      <div
        v-if="ocrBadge"
        :class="['absolute top-2 right-2 px-2 py-1 rounded-full text-xs font-medium', ocrBadge.class]"
      >
        {{ ocrBadge.text }}
      </div>
      <div
        v-if="cookbook.ocrStatus === 'PROCESSING'"
        class="absolute inset-0 bg-black/20 flex items-center justify-center"
      >
        <svg class="animate-spin h-8 w-8 text-white" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      </div>
    </div>
    <div class="p-4">
      <h3 class="text-base font-semibold text-gray-900 line-clamp-2">{{ cookbook.title }}</h3>
      <p class="text-sm text-gray-600 mt-1 line-clamp-1" v-if="cookbook.author">by {{ cookbook.author }}</p>
      <p class="text-xs text-gray-500 mt-2">{{ cookbook.recipeCount }} recipe{{ cookbook.recipeCount === 1 ? '' : 's' }}</p>
    </div>
  </RouterLink>
</template>
