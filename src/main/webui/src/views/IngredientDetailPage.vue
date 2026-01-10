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
  <div>
    <div class="max-w-4xl mx-auto">
      <!-- Back button -->
      <button
        type="button"
        class="inline-flex items-center gap-2 text-sm font-bold text-charcoal hover:text-soft-black mb-6 transition-colors"
        @click="goBack"
      >
        <ArrowLeftIcon class="h-4 w-4" />
        Back
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

      <!-- View Mode -->
      <template v-else-if="ingredient && !isEditMode">
        <div class="card-pop p-6">
          <div class="flex flex-col sm:flex-row items-start justify-between gap-4">
            <div class="flex-1">
              <h1 class="text-3xl font-bold text-soft-black capitalize">{{ ingredient.name }}</h1>
              <p v-if="ingredient.disambiguations.length > 0" class="mt-2 text-charcoal">
                Also known as:
                <span class="capitalize font-medium">{{ ingredient.disambiguations.join(', ') }}</span>
              </p>
              <div class="mt-3">
                <span class="badge-page">{{ ingredient.recipeCount }} recipe{{ ingredient.recipeCount === 1 ? '' : 's' }}</span>
              </div>

              <!-- Availability section -->
              <div class="mt-6">
                <div class="flex items-center gap-3 mb-3">
                  <span class="text-sm font-bold text-soft-black">Availability</span>
                  <span
                    class="text-xs font-bold px-3 py-1 rounded-lg border-2"
                    :class="isAvailableNow ? 'bg-accent/20 border-accent text-accent-dark' : 'bg-cream border-soft-black/20 text-charcoal'"
                  >
                    {{ isAvailableNow ? 'Available now' : 'Not in season' }}
                  </span>
                </div>
                <div class="grid grid-cols-6 gap-2">
                  <span
                    v-for="m in 12"
                    :key="m"
                    class="text-xs font-bold px-2 py-1.5 rounded-lg border-2 text-center transition-all"
                    :class="ingredient.availableMonths.includes(m) ? 'bg-accent/20 border-accent text-accent-dark' : 'bg-white border-soft-black/10 text-charcoal'"
                  >{{ monthNames[m-1] }}</span>
                </div>
              </div>
            </div>

            <!-- Action buttons -->
            <div class="flex items-center gap-2 flex-shrink-0">
              <button
                type="button"
                class="btn-secondary !py-2 !px-4 text-sm"
                @click="enterEditMode"
              >
                <PencilIcon class="h-4 w-4 mr-1 inline" />
                Edit
              </button>
              <button
                v-if="canDelete"
                type="button"
                class="inline-flex items-center gap-1 px-4 py-2 text-sm font-bold text-primary bg-white border-3 border-primary rounded-xl hover:bg-primary/5 transition-colors"
                @click="openDeleteDialog"
              >
                <TrashIcon class="h-4 w-4" />
                Delete
              </button>
            </div>
          </div>

          <!-- Recipe preview -->
          <div v-if="ingredient.recipeCount > 0" class="mt-8">
            <div class="page-header !mb-4 relative">
              <h2 class="text-lg font-bold text-soft-black">Recipes using this ingredient</h2>
            </div>
            <div class="border-3 border-soft-black rounded-xl overflow-hidden divide-y-2 divide-soft-black/10">
              <RouterLink
                v-for="recipe in previewRecipes"
                :key="recipe.id"
                :to="{ name: 'recipe-detail', params: { id: recipe.id } }"
                class="block px-4 py-3 hover:bg-cream transition-colors"
              >
                <div class="font-bold text-soft-black">{{ recipe.name }}</div>
                <div class="text-sm text-charcoal">
                  <span v-if="recipe.cookbookTitle">{{ recipe.cookbookTitle }}</span>
                  <span v-if="recipe.cookbookTitle && recipe.pageNumber">, </span>
                  <span v-if="recipe.pageNumber">p. {{ recipe.pageNumber }}</span>
                </div>
              </RouterLink>
            </div>
            <RouterLink
              v-if="hasMoreRecipes"
              :to="{ path: '/', query: { ingredient: ingredient.name } }"
              class="mt-4 inline-block text-sm font-bold text-primary hover:text-primary-dark transition-colors"
            >
              View all {{ ingredient.recipeCount }} recipes
            </RouterLink>
          </div>
        </div>
      </template>

      <!-- Edit Mode -->
      <template v-else-if="ingredient && isEditMode">
        <div class="card-pop p-6">
          <h1 class="text-2xl font-bold text-soft-black mb-6">Edit Ingredient</h1>

          <div v-if="saveError" class="mb-6 p-4 bg-primary/10 border-2 border-primary rounded-xl">
            <p class="text-sm font-bold text-primary">{{ saveError }}</p>
          </div>

          <div class="space-y-6">
            <!-- Name field -->
            <div>
              <label for="ingredient-name" class="block text-sm font-bold text-soft-black mb-2">
                Name
              </label>
              <input
                id="ingredient-name"
                v-model="editName"
                type="text"
                class="input-field w-full"
                placeholder="Ingredient name"
              />
            </div>

            <!-- Disambiguations field -->
            <div>
              <label class="block text-sm font-bold text-soft-black mb-2">
                Disambiguations (aliases)
              </label>
              <div class="space-y-3">
                <!-- Existing disambiguations -->
                <div v-if="editDisambiguations.length > 0" class="flex flex-wrap gap-2">
                  <span
                    v-for="(dis, index) in editDisambiguations"
                    :key="index"
                    class="inline-flex items-center gap-2 px-3 py-1.5 bg-cream text-soft-black rounded-lg text-sm font-bold border-2 border-soft-black/20"
                  >
                    <span class="capitalize">{{ dis }}</span>
                    <button
                      type="button"
                      class="text-charcoal hover:text-primary transition-colors"
                      @click="removeDisambiguation(index)"
                    >
                      <XMarkIcon class="h-4 w-4" />
                    </button>
                  </span>
                </div>
                <p v-else class="text-sm text-charcoal italic">No aliases defined</p>

                <!-- Add new disambiguation -->
                <div class="flex gap-2">
                  <input
                    v-model="newDisambiguation"
                    type="text"
                    class="input-field flex-1"
                    placeholder="Add new alias..."
                    @keyup.enter="addDisambiguation"
                  />
                  <button
                    type="button"
                    class="btn-accent !py-2 !px-4 text-sm"
                    @click="addDisambiguation"
                  >
                    <PlusIcon class="h-4 w-4 mr-1 inline" />
                    Add
                  </button>
                </div>
              </div>
            </div>

            <!-- Availability months selector -->
            <div>
              <label class="block text-sm font-bold text-soft-black mb-3">
                Availability by month
              </label>
              <div class="grid grid-cols-4 sm:grid-cols-6 gap-3">
                <label
                  v-for="m in 12"
                  :key="m"
                  class="flex items-center gap-2 cursor-pointer group"
                >
                  <span class="relative">
                    <input
                      type="checkbox"
                      class="sr-only peer"
                      :value="m"
                      v-model="editAvailableMonths"
                    />
                    <span class="w-5 h-5 rounded-lg border-3 border-soft-black bg-white flex items-center justify-center transition-all peer-checked:bg-accent peer-checked:border-accent peer-focus-visible:ring-2 peer-focus-visible:ring-offset-2 peer-focus-visible:ring-accent">
                      <svg v-if="editAvailableMonths.includes(m)" class="w-3 h-3 text-soft-black" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="4">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                      </svg>
                    </span>
                  </span>
                  <span
                    class="text-sm font-bold transition-colors"
                    :class="m === currentMonth ? 'text-primary' : 'text-soft-black group-hover:text-primary'"
                  >{{ monthNames[m-1] }}</span>
                </label>
              </div>
              <p class="mt-3 text-xs text-charcoal">Current month highlighted in color.</p>
            </div>
          </div>

          <!-- Action buttons -->
          <div class="mt-8 flex justify-end gap-3">
            <button
              type="button"
              class="btn-secondary !py-2 !px-4 text-sm"
              :disabled="isSaving"
              @click="cancelEdit"
            >
              Cancel
            </button>
            <button
              type="button"
              class="btn-primary !py-2 !px-4 text-sm"
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
        <div class="fixed inset-0 bg-soft-black/40" @click="closeDeleteDialog"></div>
        <div class="relative card-pop max-w-md w-full p-6 animate-pop-in">
          <h3 class="text-xl font-bold text-soft-black mb-3">Delete Ingredient</h3>
          <p class="text-charcoal mb-4">
            Are you sure you want to delete <strong class="capitalize text-soft-black">{{ ingredient?.name }}</strong>?
            This action cannot be undone.
          </p>

          <div v-if="deleteError" class="mb-4 p-3 bg-primary/10 border-2 border-primary rounded-lg">
            <p class="text-sm font-bold text-primary">{{ deleteError }}</p>
          </div>

          <div class="flex justify-end gap-3">
            <button
              type="button"
              class="btn-secondary !py-2 !px-4 text-sm"
              :disabled="isDeleting"
              @click="closeDeleteDialog"
            >
              Cancel
            </button>
            <button
              type="button"
              class="btn-primary !py-2 !px-4 text-sm"
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
