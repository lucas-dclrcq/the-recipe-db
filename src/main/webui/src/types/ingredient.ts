export interface Ingredient {
  id: string
  name: string
  disambiguations: string[]
  recipeCount: number
  availableMonths: number[]
}

export interface IngredientListResponse {
  ingredients: Ingredient[]
  nextCursor: string | null
  hasMore: boolean
}

export interface IngredientFilters {
  q?: string
  minRecipeCount?: number
  hasDisambiguations?: boolean
  availableNow?: boolean
}

export interface RecipeSummary {
  id: string
  name: string
  cookbookTitle: string | null
  pageNumber: number | null
}

export interface IngredientDetail {
  id: string
  name: string
  disambiguations: string[]
  recipeCount: number
  recipes: RecipeSummary[]
  availableMonths: number[]
  createdAt: string
  updatedAt: string
}
