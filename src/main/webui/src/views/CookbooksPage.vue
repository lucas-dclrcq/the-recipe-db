<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import CookbookCard from '../components/CookbookCard.vue'
import PageSearchBar from '../components/PageSearchBar.vue'
import { useCookbooks } from '../composables/useCookbooks'

const query = ref('')
const { cookbooks, isLoading, error, searchCookbooks, fetchCookbooks } = useCookbooks()

function handleSearch() {
  searchCookbooks(query.value)
}

watch(query, (v) => {
  if (!v || v.trim().length === 0) {
    // when cleared, reload all
    fetchCookbooks()
  }
})

onMounted(() => {
  fetchCookbooks()
})
</script>

<template>
  <div>
    <div class="max-w-6xl mx-auto">
      <!-- Header with artistic styling -->
      <div class="page-header mb-8 relative">
        <h1 class="page-title">Cookbooks</h1>
        <!-- Decorative elements -->
        <div class="absolute -top-2 right-0 w-8 h-8 bg-electric-blue rounded-full opacity-60" aria-hidden="true"></div>
        <div class="absolute top-4 right-12 w-4 h-4 bg-secondary" style="transform: rotate(45deg);" aria-hidden="true"></div>
      </div>

      <!-- Search -->
      <div class="mb-8 space-y-4">
        <PageSearchBar
          v-model="query"
          placeholder="Search cookbooks by title or author..."
          @search="handleSearch"
        />

        <!-- Error with retry -->
        <div v-if="error" class="card-pop p-5 border-primary flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-primary/5">
          <div class="flex items-center gap-3">
            <span class="w-10 h-10 flex-shrink-0 rounded-xl bg-primary flex items-center justify-center">
              <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </span>
            <p class="text-sm font-bold text-soft-black">{{ error }}</p>
          </div>
          <button
            type="button"
            class="btn-primary !py-2 !px-4 text-sm flex-shrink-0"
            @click="fetchCookbooks()"
          >
            Retry
          </button>
        </div>
      </div>

      <!-- Loading skeleton with artistic style -->
      <div v-if="isLoading" class="grid gap-6 sm:grid-cols-2 lg:grid-cols-4 pb-2 pr-2">
        <div v-for="i in 8" :key="i" class="card-pop overflow-hidden">
          <div class="aspect-[3/4] skeleton-artistic" />
          <div class="p-4 space-y-3">
            <div class="h-5 skeleton-artistic w-4/5" />
            <div class="h-4 skeleton-artistic w-2/3" />
            <div class="h-4 skeleton-artistic w-1/3" />
          </div>
          <div class="h-1.5 bg-gradient-to-r from-primary via-secondary to-accent opacity-30" />
        </div>
      </div>

      <!-- Content -->
      <div v-else>
        <div v-if="cookbooks.length === 0" class="empty-state">
          <div class="empty-state-icon">
            <svg class="w-10 h-10 text-charcoal" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
          </div>
          <p class="text-lg font-bold text-soft-black mb-2">No cookbooks found</p>
          <p class="text-charcoal">Try adjusting your search or import a new cookbook.</p>
        </div>
        <div v-else class="grid gap-6 sm:grid-cols-2 lg:grid-cols-4 pb-2 pr-2">
          <CookbookCard v-for="c in cookbooks" :key="c.id" :cookbook="c" />
        </div>
      </div>
    </div>
  </div>
</template>
