<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { ArrowLeftIcon, BookOpenIcon, PlusIcon, XMarkIcon } from '@heroicons/vue/24/outline'
import { getApiRecipesId, postApiRecipesIdIngredients } from '../api/client'
import type { Recipe, Ingredient } from '../types/recipe'
import IngredientAutocomplete from '../components/IngredientAutocomplete.vue'

const route = useRoute()
const router = useRouter()

const recipe = ref<Recipe | null>(null)
const isLoading = ref(true)
const error = ref<string | null>(null)

const showAddIngredient = ref(false)
const newIngredientName = ref('')
const isAddingIngredient = ref(false)
const addIngredientError = ref<string | null>(null)

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

function openAddIngredient() {
  showAddIngredient.value = true
  newIngredientName.value = ''
  addIngredientError.value = null
}

function closeAddIngredient() {
  showAddIngredient.value = false
  newIngredientName.value = ''
  addIngredientError.value = null
}

function handleIngredientSelect(ingredient: Ingredient) {
  newIngredientName.value = ingredient.name
}

async function addIngredient() {
  if (!recipe.value || !newIngredientName.value.trim()) return

  isAddingIngredient.value = true
  addIngredientError.value = null

  try {
    const response = await postApiRecipesIdIngredients(recipe.value.id, {
      ingredientName: newIngredientName.value.trim(),
    })

    if (response.status === 200) {
      recipe.value = response.data as Recipe
      closeAddIngredient()
    } else {
      addIngredientError.value = 'Failed to add ingredient'
    }
  } catch (e) {
    addIngredientError.value = e instanceof Error ? e.message : 'Failed to add ingredient'
  } finally {
    isAddingIngredient.value = false
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

        <div class="border-t border-gray-200 px-6 py-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Ingredients</h2>
          <div v-if="recipe.ingredients.length > 0" class="flex flex-wrap gap-2 mb-4">
            <RouterLink
              v-for="ingredient in recipe.ingredients"
              :key="ingredient"
              :to="{ path: '/', query: { ingredient } }"
              class="inline-flex items-center px-3 py-1 text-sm font-medium text-indigo-700 bg-indigo-50 rounded-full hover:bg-indigo-100"
            >
              {{ ingredient }}
            </RouterLink>
          </div>
          <p v-else class="text-gray-500 text-sm mb-4">No ingredients yet.</p>

          <div v-if="!showAddIngredient">
            <button
              type="button"
              class="inline-flex items-center gap-1 px-3 py-1.5 text-sm font-medium text-indigo-600 hover:text-indigo-800 hover:bg-indigo-50 rounded-md transition-colors"
              @click="openAddIngredient"
            >
              <PlusIcon class="h-4 w-4" />
              Add ingredient
            </button>
          </div>

          <div v-else class="mt-2 p-4 bg-gray-50 rounded-lg">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm font-medium text-gray-700">Add new ingredient</span>
              <button
                type="button"
                class="text-gray-400 hover:text-gray-600"
                @click="closeAddIngredient"
              >
                <XMarkIcon class="h-5 w-5" />
              </button>
            </div>
            <div class="flex gap-2">
              <div class="flex-1">
                <IngredientAutocomplete
                  v-model="newIngredientName"
                  @select="handleIngredientSelect"
                />
              </div>
              <button
                type="button"
                :disabled="!newIngredientName.trim() || isAddingIngredient"
                class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
                @click="addIngredient"
              >
                <span v-if="isAddingIngredient">Adding...</span>
                <span v-else>Add</span>
              </button>
            </div>
            <p class="mt-2 text-xs text-gray-500">
              Type to search existing ingredients or enter a new one.
            </p>
            <p v-if="addIngredientError" class="mt-2 text-sm text-red-600">
              {{ addIngredientError }}
            </p>
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
