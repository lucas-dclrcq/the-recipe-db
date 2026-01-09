export interface OcrResultDto {
  ingredient?: string
  recipeName?: string
  pageNumber?: number
  confidence?: number
  needsReview?: boolean
}

export interface OcrProgressEvent {
  status?: Status
  currentPage?: number
  totalPages?: number
  results?: OcrResultDto[]
  errorMessage?: string
}

export type Status = (typeof Status)[keyof typeof Status]

export const Status = {
  PENDING: 'PENDING',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
} as const
