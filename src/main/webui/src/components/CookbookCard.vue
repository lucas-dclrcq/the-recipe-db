<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { Cookbook } from '../types/cookbook'

defineProps<{
  cookbook: Cookbook
}>()
</script>

<template>
  <RouterLink
    :to="`/cookbooks/${cookbook.id}`"
    class="block bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow"
  >
    <div class="aspect-[3/4] bg-gray-100 flex items-center justify-center overflow-hidden">
      <img
        :src="cookbook.hasCover ? `/api/cookbooks/${cookbook.id}/cover` : '/default-cover.svg'"
        alt="Cookbook cover"
        class="w-full h-full object-cover"
        loading="lazy"
      />
    </div>
    <div class="p-4">
      <h3 class="text-base font-semibold text-gray-900 line-clamp-2">{{ cookbook.title }}</h3>
      <p class="text-sm text-gray-600 mt-1 line-clamp-1" v-if="cookbook.author">by {{ cookbook.author }}</p>
      <p class="text-xs text-gray-500 mt-2">{{ cookbook.recipeCount }} recipe{{ cookbook.recipeCount === 1 ? '' : 's' }}</p>
    </div>
  </RouterLink>
</template>
