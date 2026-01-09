<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { ArrowLeftIcon, PencilIcon, TrashIcon, XMarkIcon, PlusIcon } from '@heroicons/vue/24/outline'
import { useIngredient } from '../composables/useIngredient'

const route = useRoute()
const router = useRouter()
const { ingredient, isLoading, isSaving, isDeleting, error, saveError, deleteError, fetchIngredient, updateIngredient, deleteIngredient, clearErrors } = useIngredient()

const isEditMode = ref(false)
const showDeleteDialog = ref(false)
const editName = ref('')
const editDisambiguations = ref<string[]>([])
const newDisambiguation = ref('')
const editAvailableMonths = ref<number[]>([])

const monthNames = [
  'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
  'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
]
const currentMonth = new Date().getMonth() + 1

const canDelete = computed(() => ingredient.value?.recipeCount === 0)
const previewRecipes = computed(() => ingredient.value?.recipes.slice(0, 5) ?? [])
const hasMoreRecipes = computed(() => (ingredient.value?.recipeCount ?? 0) > 5)
const isAvailableNow = computed(() => ingredient.value?.availableMonths?.includes(currentMonth) ?? false)

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/ingredients')
  }
}

function enterEditMode() {
  if (!ingredient.value) return
  editName.value = ingredient.value.name
  editDisambiguations.value = [...ingredient.value.disambiguations]
  editAvailableMonths.value = [...(ingredient.value.availableMonths || [])]
  clearErrors()
  isEditMode.value = true
}

function cancelEdit() {
  isEditMode.value = false
  clearErrors()
}

function addDisambiguation() {
  const trimmed = newDisambiguation.value.trim().toLowerCase()
  if (trimmed && !editDisambiguations.value.includes(trimmed)) {
    editDisambiguations.value.push(trimmed)
    newDisambiguation.value = ''
  }
}

function removeDisambiguation(index: number) {
  editDisambiguations.value.splice(index, 1)
}

async function saveChanges() {
  if (!ingredient.value) return
  const trimmedName = editName.value.trim().toLowerCase()
  if (!trimmedName) {
    return
  }

  const result = await updateIngredient(ingredient.value.id, trimmedName, editDisambiguations.value, editAvailableMonths.value)
  if (result.success) {
    isEditMode.value = false
  }
}

function openDeleteDialog() {
  clearErrors()
  showDeleteDialog.value = true
}

function closeDeleteDialog() {
  showDeleteDialog.value = false
}

async function confirmDelete() {
  if (!ingredient.value) return
  const result = await deleteIngredient(ingredient.value.id)
  if (result.success) {
    router.push('/ingredients')
  }
}

onMounted(() => {
  const id = route.params.id as string
  if (id) {
    fetchIngredient(id)
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-4xl mx-auto px-4 py-8">
      <button
        type="button"
        class="inline-flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-6"
        @click="goBack"
      >
        <ArrowLeftIcon class="h-4 w-4" />
        Back
      </button>

      <!-- Loading state -->
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

      <!-- Error state -->
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

      <!-- View Mode -->
      <template v-else-if="ingredient && !isEditMode">
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div class="flex items-start justify-between">
            <div>
              <h1 class="text-2xl font-bold text-gray-900 capitalize">{{ ingredient.name }}</h1>
              <p v-if="ingredient.disambiguations.length > 0" class="mt-2 text-gray-600">
                Also known as:
                <span class="capitalize">{{ ingredient.disambiguations.join(', ') }}</span>
              </p>
              <p class="mt-3 text-sm text-gray-500">
                Used in {{ ingredient.recipeCount }} recipe{{ ingredient.recipeCount === 1 ? '' : 's' }}
              </p>
              <div class="mt-4">
                <div class="flex items-center gap-2 mb-2">
                  <span class="text-sm font-medium text-gray-700">Availability</span>
                  <span
                    class="text-xs px-2 py-0.5 rounded-full"
                    :class="isAvailableNow ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-700'"
                  >
                    {{ isAvailableNow ? 'Available now' : 'Not in season now' }}
                  </span>
                </div>
                <div class="grid grid-cols-6 gap-2">
                  <span
                    v-for="m in 12"
                    :key="m"
                    class="text-xs px-2 py-1 rounded border text-center"
                    :class="ingredient.availableMonths.includes(m) ? 'bg-emerald-50 border-emerald-200 text-emerald-700' : 'bg-white border-gray-200 text-gray-500'"
                  >{{ monthNames[m-1] }}</span>
                </div>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <button
                type="button"
                class="inline-flex items-center gap-1 px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                @click="enterEditMode"
              >
                <PencilIcon class="h-4 w-4" />
                Edit
              </button>
              <button
                v-if="canDelete"
                type="button"
                class="inline-flex items-center gap-1 px-3 py-2 text-sm font-medium text-red-700 bg-white border border-red-300 rounded-md hover:bg-red-50"
                @click="openDeleteDialog"
              >
                <TrashIcon class="h-4 w-4" />
                Delete
              </button>
            </div>
          </div>

          <!-- Recipe preview -->
          <div v-if="ingredient.recipeCount > 0" class="mt-8">
            <h2 class="text-lg font-semibold text-gray-900 mb-4">
              Recipes using this ingredient
            </h2>
            <div class="border border-gray-200 rounded-lg divide-y divide-gray-200">
              <RouterLink
                v-for="recipe in previewRecipes"
                :key="recipe.id"
                :to="{ name: 'recipe-detail', params: { id: recipe.id } }"
                class="block px-4 py-3 hover:bg-gray-50"
              >
                <div class="font-medium text-gray-900">{{ recipe.name }}</div>
                <div class="text-sm text-gray-500">
                  <span v-if="recipe.cookbookTitle">{{ recipe.cookbookTitle }}</span>
                  <span v-if="recipe.cookbookTitle && recipe.pageNumber">, </span>
                  <span v-if="recipe.pageNumber">p. {{ recipe.pageNumber }}</span>
                </div>
              </RouterLink>
            </div>
            <RouterLink
              v-if="hasMoreRecipes"
              :to="{ path: '/', query: { ingredient: ingredient.name } }"
              class="mt-3 inline-block text-sm text-indigo-600 hover:text-indigo-800"
            >
              View all {{ ingredient.recipeCount }} recipes
            </RouterLink>
          </div>
        </div>
      </template>

      <!-- Edit Mode -->
      <template v-else-if="ingredient && isEditMode">
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h1 class="text-xl font-semibold text-gray-900 mb-6">Edit Ingredient</h1>

          <div v-if="saveError" class="mb-4 p-3 bg-red-50 border border-red-200 rounded-md">
            <p class="text-sm text-red-600">{{ saveError }}</p>
          </div>

          <div class="space-y-6">
            <!-- Name field -->
            <div>
              <label for="ingredient-name" class="block text-sm font-medium text-gray-700 mb-1">
                Name
              </label>
              <input
                id="ingredient-name"
                v-model="editName"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="Ingredient name"
              />
            </div>

            <!-- Disambiguations field -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Disambiguations (aliases)
              </label>
              <div class="space-y-3">
                <!-- Existing disambiguations -->
                <div v-if="editDisambiguations.length > 0" class="flex flex-wrap gap-2">
                  <span
                    v-for="(dis, index) in editDisambiguations"
                    :key="index"
                    class="inline-flex items-center gap-1 px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm"
                  >
                    <span class="capitalize">{{ dis }}</span>
                    <button
                      type="button"
                      class="text-gray-500 hover:text-gray-700"
                      @click="removeDisambiguation(index)"
                    >
                      <XMarkIcon class="h-4 w-4" />
                    </button>
                  </span>
                </div>
                <p v-else class="text-sm text-gray-500 italic">No aliases defined</p>

                <!-- Add new disambiguation -->
                <div class="flex gap-2">
                  <input
                    v-model="newDisambiguation"
                    type="text"
                    class="flex-1 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                    placeholder="Add new alias..."
                    @keyup.enter="addDisambiguation"
                  />
                  <button
                    type="button"
                    class="inline-flex items-center gap-1 px-3 py-2 text-sm font-medium text-indigo-700 bg-indigo-50 border border-indigo-300 rounded-md hover:bg-indigo-100"
                    @click="addDisambiguation"
                  >
                    <PlusIcon class="h-4 w-4" />
                    Add
                  </button>
                </div>
              </div>
            </div>

            <!-- Availability months selector -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Availability by month
              </label>
              <div class="grid grid-cols-6 gap-2">
                <label v-for="m in 12" :key="m" class="flex items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                    :value="m"
                    v-model="editAvailableMonths"
                  />
                  <span :class="{'font-medium text-gray-900': m === currentMonth}">{{ monthNames[m-1] }}</span>
                </label>
              </div>
              <p class="mt-2 text-xs text-gray-500">Tip: months are 1-12 (Jan-Dec). Current month highlighted.</p>
            </div>
          </div>

          <!-- Action buttons -->
          <div class="mt-8 flex justify-end gap-3">
            <button
              type="button"
              class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
              :disabled="isSaving"
              @click="cancelEdit"
            >
              Cancel
            </button>
            <button
              type="button"
              class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 border border-transparent rounded-md hover:bg-indigo-700 disabled:opacity-50"
              :disabled="isSaving || !editName.trim()"
              @click="saveChanges"
            >
              {{ isSaving ? 'Saving...' : 'Save Changes' }}
            </button>
          </div>
        </div>
      </template>
    </div>

    <!-- Delete Confirmation Dialog -->
    <div v-if="showDeleteDialog" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="flex min-h-screen items-center justify-center p-4">
        <div class="fixed inset-0 bg-black bg-opacity-30" @click="closeDeleteDialog"></div>
        <div class="relative bg-white rounded-lg shadow-xl max-w-md w-full p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-2">Delete Ingredient</h3>
          <p class="text-gray-600 mb-4">
            Are you sure you want to delete <strong class="capitalize">{{ ingredient?.name }}</strong>?
            This action cannot be undone.
          </p>

          <div v-if="deleteError" class="mb-4 p-3 bg-red-50 border border-red-200 rounded-md">
            <p class="text-sm text-red-600">{{ deleteError }}</p>
          </div>

          <div class="flex justify-end gap-3">
            <button
              type="button"
              class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
              :disabled="isDeleting"
              @click="closeDeleteDialog"
            >
              Cancel
            </button>
            <button
              type="button"
              class="px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-md hover:bg-red-700 disabled:opacity-50"
              :disabled="isDeleting"
              @click="confirmDelete"
            >
              {{ isDeleting ? 'Deleting...' : 'Delete' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
