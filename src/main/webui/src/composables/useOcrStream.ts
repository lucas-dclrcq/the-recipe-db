import { ref } from 'vue'
import type { OcrProgressEvent, OcrResultDto, Status } from '../types/ocr'
import { getPostApiCookbooksIdOcrStartUrl } from '../api/client'

export function useOcrStream() {
  const status = ref<Status | null>(null)
  const currentPage = ref(0)
  const totalPages = ref(0)
  const results = ref<OcrResultDto[]>([])
  const errorMessage = ref<string | null>(null)
  const isProcessing = ref(false)

  async function startOcrProcessing(
    cookbookId: string,
    onComplete?: (results: OcrResultDto[]) => void,
    onError?: (error: string) => void
  ) {
    if (isProcessing.value) {
      return
    }

    isProcessing.value = true
    status.value = 'PENDING'
    errorMessage.value = null

    const url = getPostApiCookbooksIdOcrStartUrl(cookbookId)

    try {
      const response = await fetch(url, { method: 'POST' })

      if (!response.ok) {
        const errorText = await response.text()
        throw new Error(errorText || `HTTP error: ${response.status}`)
      }

      const data: OcrProgressEvent = await response.json()

      status.value = data.status ?? null
      currentPage.value = data.currentPage ?? 0
      totalPages.value = data.totalPages ?? 0
      results.value = data.results ?? []
      errorMessage.value = data.errorMessage ?? null

      if (data.status === 'COMPLETED') {
        onComplete?.(results.value)
      } else if (data.status === 'FAILED') {
        onError?.(data.errorMessage || 'OCR processing failed')
      }
    } catch (e) {
      status.value = 'FAILED'
      const message = e instanceof Error ? e.message : 'Failed to start OCR processing'
      errorMessage.value = message
      onError?.(message)
    } finally {
      isProcessing.value = false
    }
  }

  function reset() {
    status.value = null
    currentPage.value = 0
    totalPages.value = 0
    results.value = []
    errorMessage.value = null
    isProcessing.value = false
  }

  return {
    status,
    currentPage,
    totalPages,
    results,
    errorMessage,
    isProcessing,
    startOcrProcessing,
    reset,
  }
}
