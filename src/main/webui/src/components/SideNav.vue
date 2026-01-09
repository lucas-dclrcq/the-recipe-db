<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { mdiFood, mdiCarrot, mdiBookOpenVariant } from '@mdi/js'

const route = useRoute()

const isActive = (path: string) => {
  if (path === '/') {
    return route.path === '/' || route.path.startsWith('/recipes')
  }
  return route.path === path || route.path.startsWith(path + '/')
}

const navItems = computed(() => [
  { label: 'Recipes', to: '/recipes', exact: true, icon: mdiFood },
  { label: 'Ingredients', to: '/ingredients', icon: mdiCarrot },
  { label: 'Cookbooks', to: '/cookbooks', icon: mdiBookOpenVariant },
])
</script>

<template>
  <nav class="h-full flex flex-col" aria-label="Primary" role="navigation">
    <div class="px-4 py-4 border-b border-gray-200">
      <RouterLink to="/" class="font-semibold text-gray-900">The Recipe DB</RouterLink>
    </div>

    <ul class="py-2">
      <li v-for="item in navItems" :key="item.to">
        <RouterLink
          :to="item.to"
          class="flex items-center gap-3 px-4 py-2 text-sm rounded-md mx-2"
          :class="isActive(item.to) ? 'bg-indigo-50 text-indigo-700' : 'text-gray-700 hover:bg-gray-50'"
          :aria-current="isActive(item.to) ? 'page' : undefined"
        >
          <svg v-if="item.icon" :width="20" :height="20" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
            <path :d="item.icon" />
          </svg>
          <span>{{ item.label }}</span>
        </RouterLink>
      </li>
    </ul>

    <div class="mt-auto p-4 border-t border-gray-200">
      <RouterLink
        to="/import"
        class="w-full inline-flex items-center justify-center px-3 py-2 text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
      >
        Import New Cookbook
      </RouterLink>
    </div>
  </nav>
</template>
