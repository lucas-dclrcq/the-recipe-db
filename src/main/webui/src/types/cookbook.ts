export type OcrStatus = 'NONE' | 'PROCESSING' | 'COMPLETED' | 'COMPLETED_WITH_ERRORS' | 'FAILED'

export interface Cookbook {
  id: string
  title: string
  author: string
  createdAt: string
  recipeCount: number
  hasCover: boolean
  ocrStatus: OcrStatus
  ocrErrorMessage: string | null
}
