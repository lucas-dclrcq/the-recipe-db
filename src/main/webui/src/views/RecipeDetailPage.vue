<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { ArrowLeftIcon, BookOpenIcon } from '@heroicons/vue/24/outline'
import { getApiRecipesId } from '../api/client'
import type { Recipe } from '../types/recipe'

const route = useRoute()
const router = useRouter()

const recipe = ref<Recipe | null>(null)
const isLoading = ref(true)
const error = ref<string | null>(null)

async function fetchRecipe() {
  const id = route.params.id as string
  if (!id) {
    error.value = 'No recipe ID provided'
    isLoading.value = false
    return
  }

  isLoading.value = true
  error.value = null

  try {
    const response = await getApiRecipesId(id)

    if (response.status === 200) {
      recipe.value = response.data as Recipe
    } else {
      error.value = 'Recipe not found'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to load recipe'
  } finally {
    isLoading.value = false
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

onMounted(() => {
  fetchRecipe()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-3xl mx-auto px-4 py-8">
      <button
        type="button"
        class="inline-flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-6"
        @click="goBack"
      >
        <ArrowLeftIcon class="h-4 w-4" />
        Back to search
      </button>

      <div v-if="isLoading" class="flex justify-center py-12">
        <svg
          class="animate-spin h-8 w-8 text-indigo-600"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle
            class="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            stroke-width="4"
          ></circle>
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          ></path>
        </svg>
      </div>

      <div v-else-if="error" class="text-center py-12">
        <p class="text-red-600">{{ error }}</p>
        <button
          type="button"
          class="mt-4 text-indigo-600 hover:text-indigo-800"
          @click="goBack"
        >
          Go back
        </button>
      </div>

      <div v-else-if="recipe" class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <div class="px-6 py-8">
          <div class="flex items-start justify-between">
            <h1 class="text-2xl font-bold text-gray-900">{{ recipe.name }}</h1>
            <span class="flex-shrink-0 ml-4 px-3 py-1 text-sm font-medium text-gray-700 bg-gray-100 rounded-full">
              Page {{ recipe.pageNumber }}
            </span>
          </div>

          <div class="mt-4 flex items-center gap-2 text-gray-600">
            <BookOpenIcon class="h-5 w-5" />
            <RouterLink
              v-if="recipe.cookbookId"
              :to="`/cookbooks/${recipe.cookbookId}`"
              class="hover:text-indigo-600"
            >
              <span v-if="recipe.cookbookTitle">{{ recipe.cookbookTitle }}</span>
              <span v-if="recipe.cookbookTitle && recipe.cookbookAuthor"> by </span>
              <span v-if="recipe.cookbookAuthor">{{ recipe.cookbookAuthor }}</span>
            </RouterLink>
          </div>
        </div>

        <div v-if="recipe.ingredients.length > 0" class="border-t border-gray-200 px-6 py-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Ingredients</h2>
          <div class="flex flex-wrap gap-2">
            <RouterLink
              v-for="ingredient in recipe.ingredients"
              :key="ingredient"
              :to="{ path: '/', query: { ingredient } }"
              class="inline-flex items-center px-3 py-1 text-sm font-medium text-indigo-700 bg-indigo-50 rounded-full hover:bg-indigo-100"
            >
              {{ ingredient }}
            </RouterLink>
          </div>
        </div>

        <div class="border-t border-gray-200 px-6 py-4 bg-gray-50">
          <p class="text-sm text-gray-500">
            Find this recipe on page {{ recipe.pageNumber }} of your cookbook.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
