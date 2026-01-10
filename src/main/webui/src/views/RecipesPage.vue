<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import PageSearchBar from '../components/PageSearchBar.vue'
import RecipeList from '../components/RecipeList.vue'
import IngredientAutocomplete from '../components/IngredientAutocomplete.vue'
import CookbookSelector from '../components/CookbookSelector.vue'
import ActiveFilters from '../components/ActiveFilters.vue'
import { useRecipeSearch } from '../composables/useRecipeSearch'
import { useCookbooks } from '../composables/useCookbooks'
import type { Ingredient } from '../types/recipe'

const { t } = useI18n()

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
  <div>
    <div class="max-w-6xl mx-auto">
      <div class="page-header mb-8 relative">
        <h1 class="page-title">{{ t('recipes.title') }}</h1>
        <div class="absolute -top-2 right-0 w-8 h-8 bg-secondary rounded-full opacity-60" aria-hidden="true"></div>
        <div class="absolute top-4 right-12 w-4 h-4 bg-accent" style="transform: rotate(45deg);" aria-hidden="true"></div>
      </div>

      <div class="mb-8 space-y-4">
        <PageSearchBar
          v-model="query"
          :placeholder="t('recipes.searchPlaceholder')"
          :show-filters-button="true"
          :filters-expanded="showFilters"
          @search="handleSearch"
          @update:filters-expanded="showFilters = $event"
        />

        <div v-if="showFilters" class="filter-panel p-6 space-y-5 animate-pop-in">
          <div class="grid gap-5 sm:grid-cols-2">
            <div>
              <label class="block text-sm font-bold text-soft-black mb-2">
                {{ t('filters.ingredient') }}
              </label>
              <IngredientAutocomplete
                v-model="ingredientFilter"
                @select="handleIngredientSelected"
              />
            </div>
            <div>
              <label class="block text-sm font-bold text-soft-black mb-2">
                {{ t('filters.cookbook') }}
              </label>
              <CookbookSelector
                v-model="cookbookFilter"
                @update:model-value="handleCookbookChange"
              />
            </div>
            <div class="sm:col-span-2">
              <label class="inline-flex items-center gap-3 cursor-pointer group">
                <span class="relative">
                  <input
                    type="checkbox"
                    v-model="availableNow"
                    @change="handleAvailableNowChange(($event.target as HTMLInputElement).checked)"
                    class="sr-only peer"
                  />
                  <span class="w-6 h-6 rounded-lg border-3 border-soft-black bg-white flex items-center justify-center transition-all peer-checked:bg-accent peer-checked:border-accent peer-focus-visible:ring-2 peer-focus-visible:ring-offset-2 peer-focus-visible:ring-accent">
                    <svg v-if="availableNow" class="w-4 h-4 text-soft-black" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="4">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                  </span>
                </span>
                <span class="text-sm font-bold text-soft-black group-hover:text-primary transition-colors">
                  {{ t('filters.ingredientsAvailableNow') }}
                </span>
              </label>
            </div>
          </div>
          <div class="flex gap-3 justify-end pt-2">
            <button
              type="button"
              class="btn-secondary !py-2 !px-4 text-sm"
              @click="handleClearFilters"
            >
              {{ t('common.clearAll') }}
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
            @click="searchRecipes()"
          >
            {{ t('common.retry') }}
          </button>
        </div>
      </div>

      <RecipeList :recipes="recipes" :is-loading="isLoading" :has-more="hasMore" @load-more="loadMore" />
    </div>
  </div>
</template>
