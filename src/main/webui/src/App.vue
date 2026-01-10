<script setup lang="ts">
import { ref, watch } from 'vue'
import { RouterView, RouterLink, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import SideNav from './components/SideNav.vue'

const { t } = useI18n()
const isMobileNavOpen = ref(false)
const route = useRoute()

function closeNavOnRouteChange() {
  isMobileNavOpen.value = false
}

// Close the mobile nav whenever route changes
watch(
  () => route.fullPath,
  () => closeNavOnRouteChange()
)
</script>

<template>
  <div class="min-h-screen bg-cream pattern-grid overflow-x-hidden">
    <!-- Decorative floating shapes - visible on larger screens -->
    <div class="hidden lg:block fixed top-20 right-10 w-16 h-16 bg-secondary rounded-full opacity-20 animate-float" aria-hidden="true"></div>
    <div class="hidden lg:block fixed top-40 right-32 w-8 h-8 bg-primary opacity-15 rotate-45" aria-hidden="true"></div>
    <div class="hidden lg:block fixed bottom-20 right-20 w-12 h-12 border-4 border-accent rounded-full opacity-20" aria-hidden="true"></div>

    <!-- Desktop/Tablet side nav -->
    <aside class="hidden md:flex md:flex-col w-64 sidebar-artistic fixed inset-y-0 left-0 z-20 h-screen">
      <SideNav />
    </aside>

    <!-- Mobile top bar -->
    <div class="md:hidden fixed top-0 inset-x-0 h-16 bg-warm-white border-b-4 border-soft-black flex items-center justify-between px-4 z-30">
      <button
        type="button"
        class="p-2 rounded-xl border-3 border-soft-black bg-white hover:bg-cream transition-colors shadow-[3px_3px_0_var(--color-soft-black)]"
        :aria-label="t('nav.openNavigation')"
        @click="isMobileNavOpen = true"
      >
        <svg class="h-5 w-5 text-soft-black" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>
      <RouterLink to="/" class="logo-artistic">{{ t('app.title') }}</RouterLink>
      <span class="w-10" aria-hidden="true"></span>
    </div>

    <!-- Mobile drawer overlay -->
    <div
      class="md:hidden fixed inset-0 z-40"
      v-show="isMobileNavOpen"
      aria-hidden="true"
      @click="isMobileNavOpen = false"
    >
      <div class="absolute inset-0 bg-soft-black/40 backdrop-blur-sm"></div>
    </div>

    <!-- Mobile drawer -->
    <aside
      class="md:hidden fixed inset-y-0 left-0 w-72 sidebar-artistic z-50 transform transition-transform duration-300 ease-out flex flex-col h-screen"
      :class="isMobileNavOpen ? 'translate-x-0' : '-translate-x-full'"
    >
      <div class="h-16 flex-shrink-0 flex items-center justify-between px-5 border-b-4 border-soft-black bg-secondary">
        <span class="font-black text-soft-black text-lg tracking-tight">{{ t('app.menu') }}</span>
        <button
          type="button"
          class="p-2 rounded-xl border-3 border-soft-black bg-white hover:bg-cream transition-colors shadow-[3px_3px_0_var(--color-soft-black)]"
          :aria-label="t('nav.closeNavigation')"
          @click="isMobileNavOpen = false"
        >
          <svg class="h-5 w-5 text-soft-black" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
      <div class="flex-1 flex flex-col min-h-0">
        <SideNav hide-logo />
      </div>
    </aside>

    <!-- Main content -->
    <main class="md:ml-64">
      <div class="px-4 pt-20 pb-6 md:pt-8 md:pb-8 md:px-10">
        <RouterView />
      </div>
    </main>
  </div>
</template>
