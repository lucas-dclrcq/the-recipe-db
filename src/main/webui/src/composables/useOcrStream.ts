import { ref, onUnmounted } from 'vue'
import type { OcrProgressEvent, OcrResultDto, Status } from '../types/ocr'
import { getPostApiCookbooksIdOcrStartUrl, getGetApiCookbooksIdOcrResultsUrl } from '../api/client'

export function useOcrStream() {
  const status = ref<Status | null>(null)
  const currentPage = ref(0)
  const totalPages = ref(0)
  const results = ref<OcrResultDto[]>([])
  const errorMessage = ref<string | null>(null)
  const isProcessing = ref(false)

  let pollInterval: ReturnType<typeof setInterval> | null = null

  function stopPolling() {
    if (pollInterval) {
      clearInterval(pollInterval)
      pollInterval = null
    }
  }

  async function checkStatus(
    cookbookId: string,
    onComplete?: (results: OcrResultDto[]) => void,
    onError?: (error: string) => void
  ) {
    try {
      const url = getGetApiCookbooksIdOcrResultsUrl(cookbookId)
      const response = await fetch(url)

      if (!response.ok) {
        throw new Error(`HTTP error: ${response.status}`)
      }

      const data: OcrProgressEvent = await response.json()

      status.value = data.status ?? null
      currentPage.value = data.currentPage ?? 0
      totalPages.value = data.totalPages ?? 0
      results.value = data.results ?? []
      errorMessage.value = data.errorMessage ?? null

      if (data.status === 'COMPLETED' || data.status === 'COMPLETED_WITH_ERRORS') {
        stopPolling()
        isProcessing.value = false
        onComplete?.(results.value)
      } else if (data.status === 'FAILED') {
        stopPolling()
        isProcessing.value = false
        onError?.(data.errorMessage || 'OCR processing failed')
      }
    } catch (e) {
      // Don't stop polling on transient errors
      console.error('Error checking OCR status:', e)
    }
  }

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

      if (response.status === 409) {
        // Already processing - start polling to check status
        status.value = 'IN_PROGRESS'
      } else if (!response.ok) {
        const errorText = await response.text()
        throw new Error(errorText || `HTTP error: ${response.status}`)
      } else {
        status.value = 'IN_PROGRESS'
      }

      // Start polling for results
      pollInterval = setInterval(() => {
        checkStatus(cookbookId, onComplete, onError)
      }, 3000)

      // Also check immediately
      await checkStatus(cookbookId, onComplete, onError)
    } catch (e) {
      status.value = 'FAILED'
      const message = e instanceof Error ? e.message : 'Failed to start OCR processing'
      errorMessage.value = message
      isProcessing.value = false
      onError?.(message)
    }
  }

  function reset() {
    stopPolling()
    status.value = null
    currentPage.value = 0
    totalPages.value = 0
    results.value = []
    errorMessage.value = null
    isProcessing.value = false
  }

  onUnmounted(() => {
    stopPolling()
  })

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
