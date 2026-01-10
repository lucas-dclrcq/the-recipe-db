<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { mdiFood, mdiCarrot, mdiBookOpenVariant } from '@mdi/js'
import LanguageSwitcher from './LanguageSwitcher.vue'

defineProps<{
  hideLogo?: boolean
}>()

const { t } = useI18n()
const route = useRoute()

const isActive = (path: string) => {
  if (path === '/') {
    return route.path === '/' || route.path.startsWith('/recipes')
  }
  return route.path === path || route.path.startsWith(path + '/')
}

const navItems = computed(() => [
  { labelKey: 'nav.recipes', to: '/recipes', exact: true, icon: mdiFood, color: 'bg-primary' },
  { labelKey: 'nav.ingredients', to: '/ingredients', icon: mdiCarrot, color: 'bg-accent' },
  { labelKey: 'nav.cookbooks', to: '/cookbooks', icon: mdiBookOpenVariant, color: 'bg-electric-blue' },
])
</script>

<template>
  <nav class="h-full flex flex-col relative z-10" aria-label="Primary" role="navigation">
    <!-- Logo section with decorative element (hidden on mobile drawer) -->
    <div v-if="!hideLogo" class="px-5 py-6 border-b-4 border-soft-black relative overflow-hidden">
      <!-- Decorative corner triangle -->
      <div class="absolute top-0 right-0 w-12 h-12 bg-secondary" style="clip-path: polygon(100% 0, 0 0, 100% 100%);" aria-hidden="true"></div>

      <RouterLink to="/" class="block relative logo-artistic text-xl">
        {{ t('app.title') }}
      </RouterLink>

      <!-- Small decorative circle -->
      <div class="absolute bottom-3 right-5 w-3 h-3 bg-accent rounded-full" aria-hidden="true"></div>
    </div>

    <ul class="py-4 px-3 space-y-2 flex-1">
      <li v-for="item in navItems" :key="item.to">
        <RouterLink
          :to="item.to"
          class="flex items-center gap-4 px-4 py-3 text-base font-bold rounded-xl transition-all duration-200"
          :class="isActive(item.to)
            ? 'bg-primary text-white shadow-[4px_4px_0_var(--color-soft-black)] border-3 border-soft-black'
            : 'text-soft-black hover:bg-cream border-3 border-transparent hover:border-soft-black/20'"
          :aria-current="isActive(item.to) ? 'page' : undefined"
        >
          <!-- Icon with colored background when not active -->
          <span
            class="flex items-center justify-center w-10 h-10 rounded-lg border-2 transition-colors"
            :class="isActive(item.to)
              ? 'bg-white/20 border-white/30'
              : [item.color, 'border-soft-black text-white']"
          >
            <svg :width="22" :height="22" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path :d="item.icon" />
            </svg>
          </span>
          <span class="tracking-tight">{{ t(item.labelKey) }}</span>

          <!-- Active indicator dot -->
          <span
            v-if="isActive(item.to)"
            class="ml-auto w-2 h-2 bg-secondary rounded-full"
            aria-hidden="true"
          ></span>
        </RouterLink>
      </li>
    </ul>

    <div class="flex justify-end px-4 pb-3">
      <LanguageSwitcher />
    </div>
    
    <!-- Import button section - pinned to bottom -->
    <div class="p-4 border-t-4 border-soft-black relative">
      <!-- Decorative dots pattern -->
      <div class="absolute inset-0 opacity-5 pointer-events-none" style="background-image: radial-gradient(circle, #1A1A2E 1px, transparent 1px); background-size: 8px 8px;" aria-hidden="true"></div>

      <RouterLink
        to="/import"
        class="btn-accent w-full inline-flex items-center justify-center gap-2 text-sm relative overflow-hidden group"
      >
        <!-- Animated shine effect on hover -->
        <span class="absolute inset-0 bg-white/20 -translate-x-full group-hover:translate-x-full transition-transform duration-500" aria-hidden="true"></span>

        <!-- Plus icon -->
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
        <span class="relative font-black tracking-wide">{{ t('nav.importCookbook') }}</span>
      </RouterLink>
    </div>

    <!-- Decorative bottom wave -->
    <div class="h-2 bg-gradient-to-r from-primary via-secondary to-accent" aria-hidden="true"></div>
  </nav>
</template>
