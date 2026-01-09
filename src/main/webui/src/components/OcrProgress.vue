<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useOcrStream } from '../composables/useOcrStream'
import type { OcrResultDto } from '../types/ocr'

interface Props {
  cookbookId: string
}

interface Emits {
  (e: 'complete', results: OcrResultDto[]): void
  (e: 'error', message: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { status, results, errorMessage, isProcessing, startOcrProcessing } = useOcrStream()

const statusText = computed(() => {
  if (isProcessing.value) {
    return 'Processing index pages...'
  }
  switch (status.value) {
    case 'PENDING':
      return 'Preparing OCR processing...'
    case 'IN_PROGRESS':
      return 'Processing index pages...'
    case 'COMPLETED':
      return 'OCR processing complete!'
    case 'FAILED':
      return 'OCR processing failed'
    default:
      return 'Starting...'
  }
})

onMounted(() => {
  startOcrProcessing(
    props.cookbookId,
    (results) => emit('complete', results),
    (error) => emit('error', error)
  )
})
</script>

<template>
  <div class="space-y-6">
    <div class="text-center">
      <div v-if="status !== 'FAILED'" class="mb-4">
        <svg
          class="animate-spin mx-auto h-12 w-12 text-indigo-600"
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

      <div v-else class="mb-4">
        <svg
          class="mx-auto h-12 w-12 text-red-500"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          />
        </svg>
      </div>

      <h3 class="text-lg font-medium text-gray-900">{{ statusText }}</h3>

      <p v-if="errorMessage" class="mt-2 text-sm text-red-600">
        {{ errorMessage }}
      </p>
    </div>

    <div v-if="results.length > 0" class="text-sm text-gray-600 text-center">
      Found {{ results.length }} recipe{{ results.length === 1 ? '' : 's' }}
    </div>
  </div>
</template>
