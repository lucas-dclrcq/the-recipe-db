<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import PageSearchBar from '../components/PageSearchBar.vue'
import RecipeList from '../components/RecipeList.vue'
import IngredientAutocomplete from '../components/IngredientAutocomplete.vue'
import CookbookSelector from '../components/CookbookSelector.vue'
import ActiveFilters from '../components/ActiveFilters.vue'
import { useRecipeSearch } from '../composables/useRecipeSearch'
import { useCookbooks } from '../composables/useCookbooks'
import type { Ingredient } from '../types/recipe'

const showFilters = ref(false)

const {
  recipes,
  isLoading,
  hasMore,
  error,
  filters,
  hasFilters,
  searchRecipes,
  loadMore,
  setSearchQuery,
  setIngredientFilter,
  setCookbookFilter,
  setAvailableNow,
  clearFilters,
  removeFilter,
} = useRecipeSearch()

const { cookbooks } = useCookbooks()

const query = ref(filters.value.q ?? '')
const ingredientFilter = ref(filters.value.ingredient ?? '')
const cookbookFilter = ref(filters.value.cookbookId ?? '')
const availableNow = ref<boolean>(filters.value.availableNow ?? false)

const selectedCookbookTitle = computed(() => {
  if (!filters.value.cookbookId) return undefined
  const cookbook = cookbooks.value.find((c) => c.id === filters.value.cookbookId)
  return cookbook?.title
})

function handleSearch() {
  setSearchQuery(query.value)
}

function handleIngredientSelected(ingredient: Ingredient) {
  ingredientFilter.value = ingredient.name
  setIngredientFilter(ingredient.name)
}

function handleCookbookChange(cookbookId: string) {
  cookbookFilter.value = cookbookId
  setCookbookFilter(cookbookId)
}

function handleAvailableNowChange(value: boolean) {
  availableNow.value = value
  setAvailableNow(value)
}

function handleClearFilters() {
  query.value = ''
  ingredientFilter.value = ''
  cookbookFilter.value = ''
  clearFilters()
}

watch(query, (v) => {
  if (!v || v.trim().length === 0) {
    setSearchQuery('')
  }
})

onMounted(() => {
  searchRecipes()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-6xl mx-auto px-4 py-8">
      <!-- Header -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-900">Recipes</h1>
      </div>

      <!-- Search and filters -->
      <div class="mb-6 space-y-3">
        <PageSearchBar
          v-model="query"
          placeholder="Search recipes..."
          :show-filters-button="true"
          :filters-expanded="showFilters"
          @search="handleSearch"
          @update:filters-expanded="showFilters = $event"
        />

        <!-- Filter panel -->
        <div v-if="showFilters" class="p-4 bg-white rounded-lg border border-gray-200 space-y-4">
          <div class="grid gap-4 sm:grid-cols-2">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Ingredient
              </label>
              <IngredientAutocomplete
                v-model="ingredientFilter"
                @select="handleIngredientSelected"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Cookbook
              </label>
              <CookbookSelector
                v-model="cookbookFilter"
                @update:model-value="handleCookbookChange"
              />
            </div>
            <div class="sm:col-span-2">
              <label class="inline-flex items-center gap-2 text-sm text-gray-700">
                <input type="checkbox" v-model="availableNow" @change="handleAvailableNowChange(($event.target as HTMLInputElement).checked)" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
                Ingredient available now
              </label>
            </div>
          </div>
          <div class="flex gap-2 justify-end">
            <button
              type="button"
              class="px-3 py-1.5 text-sm rounded-md border border-gray-300 bg-white hover:bg-gray-50 text-gray-700"
              @click="handleClearFilters"
            >
              Clear
            </button>
          </div>
        </div>

        <!-- Active filters -->
        <ActiveFilters
          v-if="hasFilters"
          :filters="filters"
          :cookbook-title="selectedCookbookTitle"
          @remove-filter="removeFilter"
          @clear-all="clearFilters"
        />

        <!-- Error with retry -->
        <div v-if="error" class="p-4 bg-red-50 border border-red-200 rounded-lg flex items-center justify-between">
          <p class="text-sm text-red-600">{{ error }}</p>
          <button
            type="button"
            class="px-3 py-1.5 text-sm rounded-md bg-red-100 text-red-700 hover:bg-red-200"
            @click="searchRecipes()"
          >
            Retry
          </button>
        </div>
      </div>

      <RecipeList :recipes="recipes" :is-loading="isLoading" :has-more="hasMore" @load-more="loadMore" />
    </div>
  </div>
</template>
