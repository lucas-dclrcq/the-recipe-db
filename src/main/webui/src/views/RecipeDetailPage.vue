<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { ArrowLeftIcon, BookOpenIcon, PlusIcon, XMarkIcon } from '@heroicons/vue/24/outline'
import { getApiRecipesId, postApiRecipesIdIngredients, deleteApiRecipesIdIngredientsIngredientName } from '../api/client'
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
const removingIngredient = ref<string | null>(null)

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

async function removeIngredient(ingredientName: string) {
  if (!recipe.value) return

  removingIngredient.value = ingredientName

  try {
    const response = await deleteApiRecipesIdIngredientsIngredientName(recipe.value.id, ingredientName)

    if (response.status === 200) {
      recipe.value = response.data as Recipe
    }
  } catch (e) {
    // Silent fail - could add error handling if needed
  } finally {
    removingIngredient.value = null
  }
}

onMounted(() => {
  fetchRecipe()
})
</script>

<template>
  <div>
    <div class="max-w-3xl mx-auto">
      <!-- Back button -->
      <button
        type="button"
        class="inline-flex items-center gap-2 text-sm font-bold text-charcoal hover:text-soft-black mb-6 transition-colors"
        @click="goBack"
      >
        <ArrowLeftIcon class="h-4 w-4" />
        Back to search
      </button>

      <!-- Loading state -->
      <div v-if="isLoading" class="flex justify-center py-12">
        <div class="flex items-center gap-3 px-6 py-3 bg-white rounded-xl border-3 border-soft-black shadow-[4px_4px_0_var(--color-soft-black)]">
          <svg class="animate-spin h-6 w-6 text-primary" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span class="font-bold text-soft-black">Loading...</span>
        </div>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="text-center py-12">
        <div class="empty-state">
          <div class="empty-state-icon bg-primary/10">
            <svg class="w-10 h-10 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <p class="text-lg font-bold text-soft-black mb-2">{{ error }}</p>
          <button
            type="button"
            class="btn-secondary !py-2 !px-4 text-sm"
            @click="goBack"
          >
            Go back
          </button>
        </div>
      </div>

      <!-- Recipe content -->
      <div v-else-if="recipe" class="card-pop overflow-hidden">
        <!-- Header section -->
        <div class="p-6 pb-0">
          <div class="flex flex-col sm:flex-row items-start justify-between gap-3">
            <h1 class="text-3xl font-bold text-soft-black">{{ recipe.name }}</h1>
            <span class="badge-page flex-shrink-0">
              Page {{ recipe.pageNumber }}
            </span>
          </div>

          <!-- Cookbook link -->
          <div class="mt-4 flex items-center gap-2">
            <span class="w-8 h-8 rounded-lg bg-electric-blue/20 flex items-center justify-center">
              <BookOpenIcon class="h-4 w-4 text-electric-blue" />
            </span>
            <RouterLink
              v-if="recipe.cookbookId"
              :to="`/cookbooks/${recipe.cookbookId}`"
              class="text-charcoal hover:text-primary font-medium transition-colors"
            >
              <span v-if="recipe.cookbookTitle">{{ recipe.cookbookTitle }}</span>
              <span v-if="recipe.cookbookTitle && recipe.cookbookAuthor"> by </span>
              <span v-if="recipe.cookbookAuthor">{{ recipe.cookbookAuthor }}</span>
            </RouterLink>
          </div>
        </div>

        <!-- Ingredients section -->
        <div class="p-6 mt-4 border-t-3 border-soft-black/10">
          <div class="page-header !mb-4 relative">
            <h2 class="text-lg font-bold text-soft-black">Ingredients</h2>
          </div>

          <div v-if="recipe.ingredients.length > 0" class="flex flex-wrap gap-2 mb-4">
            <div
              v-for="ingredient in recipe.ingredients"
              :key="ingredient"
              class="group inline-flex items-center badge-ingredient pr-1.5 hover:shadow-[2px_2px_0_var(--color-soft-black)] transition-all"
              :class="{ 'opacity-50': removingIngredient === ingredient }"
            >
              <RouterLink
                :to="{ path: '/', query: { ingredient } }"
                class="text-soft-black"
              >
                {{ ingredient }}
              </RouterLink>
              <button
                type="button"
                class="ml-1.5 w-5 h-5 flex items-center justify-center rounded-full bg-soft-black/10 hover:bg-primary hover:text-white text-charcoal transition-all"
                :disabled="removingIngredient === ingredient"
                @click.stop="removeIngredient(ingredient)"
              >
                <XMarkIcon class="h-3 w-3" />
              </button>
            </div>
          </div>
          <p v-else class="text-charcoal text-sm mb-4 italic">No ingredients yet.</p>

          <!-- Add ingredient button -->
          <div v-if="!showAddIngredient">
            <button
              type="button"
              class="inline-flex items-center gap-1 px-4 py-2 text-sm font-bold text-primary hover:text-primary-dark hover:bg-primary/5 rounded-xl transition-all"
              @click="openAddIngredient"
            >
              <PlusIcon class="h-4 w-4" />
              Add ingredient
            </button>
          </div>

          <!-- Add ingredient form -->
          <div v-else class="mt-3 p-4 bg-cream rounded-xl border-2 border-soft-black/10">
            <div class="flex items-center gap-2">
              <div class="flex-1">
                <IngredientAutocomplete
                  v-model="newIngredientName"
                  @select="handleIngredientSelect"
                />
              </div>
              <button
                type="button"
                :disabled="!newIngredientName.trim() || isAddingIngredient"
                class="btn-primary !py-2 !px-4 text-sm disabled:opacity-50 flex-shrink-0"
                @click="addIngredient"
              >
                <span v-if="isAddingIngredient">Adding...</span>
                <span v-else>Add</span>
              </button>
              <button
                type="button"
                class="p-2 text-charcoal hover:text-primary hover:bg-soft-black/5 rounded-lg flex-shrink-0 transition-colors"
                @click="closeAddIngredient"
              >
                <XMarkIcon class="h-5 w-5" />
              </button>
            </div>
            <p class="mt-2 text-xs text-charcoal">
              Type to search existing ingredients or enter a new one.
            </p>
            <p v-if="addIngredientError" class="mt-2 text-sm font-bold text-primary">
              {{ addIngredientError }}
            </p>
          </div>
        </div>

        <!-- Footer section -->
        <div class="px-6 py-4 bg-cream border-t-3 border-soft-black/10">
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 bg-accent rounded-full"></span>
            <p class="text-sm text-charcoal">
              Find this recipe on <span class="font-bold text-soft-black">page {{ recipe.pageNumber }}</span> of your cookbook.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
