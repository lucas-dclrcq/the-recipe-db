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
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-6xl mx-auto px-4 py-8">
      <!-- Header -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-900">Cookbooks</h1>
      </div>

      <!-- Search -->
      <div class="mb-6 space-y-3">
        <PageSearchBar
          v-model="query"
          placeholder="Search cookbooks by title or author..."
          @search="handleSearch"
        />

        <!-- Error with retry -->
        <div v-if="error" class="p-4 bg-red-50 border border-red-200 rounded-lg flex items-center justify-between">
          <p class="text-sm text-red-600">{{ error }}</p>
          <button
            type="button"
            class="px-3 py-1.5 text-sm rounded-md bg-red-100 text-red-700 hover:bg-red-200"
            @click="fetchCookbooks()"
          >
            Retry
          </button>
        </div>
      </div>

      <!-- Loading skeleton -->
      <div v-if="isLoading" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div v-for="i in 8" :key="i" class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          <div class="aspect-[3/4] bg-gray-100 animate-pulse" />
          <div class="p-4 space-y-2">
            <div class="h-4 bg-gray-200 rounded w-4/5 animate-pulse" />
            <div class="h-3 bg-gray-200 rounded w-2/3 animate-pulse" />
            <div class="h-3 bg-gray-200 rounded w-1/3 animate-pulse" />
          </div>
        </div>
      </div>

      <!-- Content -->
      <div v-else>
        <div v-if="cookbooks.length === 0" class="text-center py-12 text-gray-500">
          No cookbooks found.
        </div>
        <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <CookbookCard v-for="c in cookbooks" :key="c.id" :cookbook="c" />
        </div>
      </div>
    </div>
  </div>
</template>
