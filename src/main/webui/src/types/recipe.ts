export interface Recipe {
  id: string
  name: string
  pageNumber: number
  cookbookId: string
  cookbookTitle: string | null
  cookbookAuthor: string | null
  ingredients: string[]
}

export interface RecipeListResponse {
  recipes: Recipe[]
  nextCursor: string | null
  hasMore: boolean
}

export interface Ingredient {
  id: string
  name: string
}

export interface SearchFilters {
  q?: string
  ingredient?: string
  cookbookId?: string
  availableNow?: boolean
}
