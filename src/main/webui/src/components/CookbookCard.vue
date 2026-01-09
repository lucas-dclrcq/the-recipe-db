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
      return { text: 'Processing...', class: 'badge-status-processing' }
    case 'COMPLETED':
      return { text: 'Ready', class: 'badge-status-success' }
    case 'COMPLETED_WITH_ERRORS':
      return { text: 'Partial', class: 'badge-status-warning' }
    case 'FAILED':
      return { text: 'Failed', class: 'badge-status-error' }
    default:
      return null
  }
})
</script>

<template>
  <RouterLink
    :to="`/cookbooks/${cookbook.id}`"
    class="card-pop block overflow-hidden group"
  >
    <!-- Image section with artistic overlay -->
    <div class="aspect-[3/4] bg-cream relative overflow-hidden">
      <img
        :src="cookbook.hasCover ? `/api/cookbooks/${cookbook.id}/cover` : '/default-cover.svg'"
        alt="Cookbook cover"
        class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
        loading="lazy"
      />

      <!-- Gradient overlay on hover -->
      <div class="absolute inset-0 bg-gradient-to-t from-soft-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity" aria-hidden="true"></div>

      <!-- OCR Status Badge -->
      <div
        v-if="ocrBadge"
        :class="['absolute top-3 right-3 badge-status', ocrBadge.class]"
      >
        {{ ocrBadge.text }}
      </div>

      <!-- Processing overlay with spinner -->
      <div
        v-if="cookbook.ocrStatus === 'PROCESSING'"
        class="absolute inset-0 bg-soft-black/30 flex items-center justify-center backdrop-blur-[2px]"
      >
        <div class="w-14 h-14 bg-white rounded-xl border-3 border-soft-black shadow-[4px_4px_0_var(--color-soft-black)] flex items-center justify-center">
          <svg class="animate-spin h-7 w-7 text-primary" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        </div>
      </div>

      <!-- Decorative corner triangle -->
      <div class="absolute bottom-0 left-0 w-16 h-16 bg-secondary" style="clip-path: polygon(0 100%, 0 0, 100% 100%);" aria-hidden="true"></div>
    </div>

    <!-- Content section -->
    <div class="p-4 bg-white relative">
      <!-- Decorative dot -->
      <div class="absolute top-3 right-3 w-2 h-2 bg-accent rounded-full" aria-hidden="true"></div>

      <h3 class="text-base font-black text-soft-black line-clamp-2 leading-tight tracking-tight group-hover:text-primary transition-colors">
        {{ cookbook.title }}
      </h3>
      <p class="text-sm text-charcoal mt-1.5 line-clamp-1 font-medium" v-if="cookbook.author">
        by {{ cookbook.author }}
      </p>
      <div class="mt-3 flex items-center gap-2">
        <span class="inline-flex items-center px-2.5 py-1 text-xs font-bold text-soft-black bg-cream rounded-lg border-2 border-soft-black">
          {{ cookbook.recipeCount }} recipe{{ cookbook.recipeCount === 1 ? '' : 's' }}
        </span>
      </div>
    </div>

    <!-- Bottom accent line -->
    <div class="h-1.5 bg-gradient-to-r from-primary via-secondary to-accent" aria-hidden="true"></div>
  </RouterLink>
</template>
