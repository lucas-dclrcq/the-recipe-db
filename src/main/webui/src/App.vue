<script setup lang="ts">
import { ref, watch } from 'vue'
import { RouterView, RouterLink, useRoute } from 'vue-router'
import SideNav from './components/SideNav.vue'

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
  <div class="min-h-screen bg-gray-50 flex">
    <!-- Desktop/Tablet side nav -->
    <aside class="hidden md:block w-60 border-r border-gray-200 bg-white fixed inset-y-0 left-0 z-20">
      <SideNav />
    </aside>

    <!-- Mobile top bar -->
    <div class="md:hidden fixed top-0 inset-x-0 h-14 bg-white border-b border-gray-200 flex items-center justify-between px-4 z-30">
      <button
        type="button"
        class="p-2 rounded-md border border-gray-200 text-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        aria-label="Open navigation"
        @click="isMobileNavOpen = true"
      >
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>
      <RouterLink to="/" class="font-semibold text-gray-900">The Recipe DB</RouterLink>
      <span class="w-9" aria-hidden="true"></span>
    </div>

    <!-- Mobile drawer overlay -->
    <div
      class="md:hidden fixed inset-0 z-40"
      v-show="isMobileNavOpen"
      aria-hidden="true"
      @click="isMobileNavOpen = false"
    >
      <div class="absolute inset-0 bg-black/30"></div>
    </div>

    <!-- Mobile drawer -->
    <aside
      class="md:hidden fixed inset-y-0 left-0 w-64 bg-white border-r border-gray-200 z-50 transform transition-transform"
      :class="isMobileNavOpen ? 'translate-x-0' : '-translate-x-full'"
    >
      <div class="h-14 flex items-center justify-between px-4 border-b border-gray-200">
        <span class="font-semibold">Menu</span>
        <button
          type="button"
          class="p-2 rounded-md border border-gray-200 text-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          aria-label="Close navigation"
          @click="isMobileNavOpen = false"
        >
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
      <SideNav />
    </aside>

    <!-- Main content -->
    <main class="flex-1 min-w-0 md:ml-60 w-full">
      <div class="md:py-6 md:px-8 px-4" :class="{'pt-20': true}">
        <RouterView />
      </div>
    </main>
  </div>
</template>
