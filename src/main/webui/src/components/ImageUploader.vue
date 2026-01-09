<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  modelValue: File[]
  disabled?: boolean
  accept?: string
}

interface Emits {
  (e: 'update:modelValue', value: File[]): void
  (e: 'submit'): void
}

const props = withDefaults(defineProps<Props>(), {
  accept: 'image/jpeg,image/png',
})
const emit = defineEmits<Emits>()

const isDragging = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

const previews = computed(() =>
  props.modelValue.map((file) => ({
    file,
    url: URL.createObjectURL(file),
  }))
)

function handleDragOver(event: DragEvent) {
  event.preventDefault()
  isDragging.value = true
}

function handleDragLeave() {
  isDragging.value = false
}

function handleDrop(event: DragEvent) {
  event.preventDefault()
  isDragging.value = false

  if (props.disabled) return

  const files = event.dataTransfer?.files
  if (files) {
    addFiles(Array.from(files))
  }
}

function handleFileSelect(event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files) {
    addFiles(Array.from(input.files))
  }
}

function addFiles(newFiles: File[]) {
  const validFiles = newFiles.filter((file) => {
    const validTypes = props.accept.split(',').map((t) => t.trim())
    return validTypes.includes(file.type)
  })

  if (validFiles.length > 0) {
    emit('update:modelValue', [...props.modelValue, ...validFiles])
  }
}

function removeFile(index: number) {
  const newFiles = [...props.modelValue]
  newFiles.splice(index, 1)
  emit('update:modelValue', newFiles)
}

function openFilePicker() {
  fileInput.value?.click()
}

function handleSubmit() {
  emit('submit')
}
</script>

<template>
  <div class="space-y-6">
    <div
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
      @click="openFilePicker"
      :class="[
        'border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors',
        isDragging ? 'border-indigo-500 bg-indigo-50' : 'border-gray-300 hover:border-gray-400',
        disabled ? 'opacity-50 cursor-not-allowed' : '',
      ]"
    >
      <input
        ref="fileInput"
        type="file"
        :accept="accept"
        multiple
        class="hidden"
        @change="handleFileSelect"
        :disabled="disabled"
      />

      <svg
        class="mx-auto h-12 w-12 text-gray-400"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
        />
      </svg>

      <p class="mt-4 text-sm text-gray-600">
        <span class="font-medium text-indigo-600">Click to upload</span>
        or drag and drop
      </p>
      <p class="mt-1 text-xs text-gray-500">PNG or JPEG images only</p>
    </div>

    <div v-if="previews.length > 0" class="space-y-4">
      <h3 class="text-sm font-medium text-gray-700">
        Uploaded Pages ({{ previews.length }})
      </h3>

      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        <div
          v-for="(preview, index) in previews"
          :key="index"
          class="relative group"
        >
          <div class="aspect-[3/4] rounded-lg overflow-hidden bg-gray-100">
            <img
              :src="preview.url"
              :alt="`Page ${index + 1}`"
              class="w-full h-full object-cover"
            />
          </div>

          <div
            class="absolute top-2 left-2 bg-black/60 text-white text-xs px-2 py-1 rounded"
          >
            Page {{ index + 1 }}
          </div>

          <button
            v-if="!disabled"
            @click.stop="removeFile(index)"
            class="absolute top-2 right-2 bg-red-500 text-white p-1 rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
            type="button"
          >
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <div class="flex justify-end">
      <button
        type="button"
        @click="handleSubmit"
        :disabled="disabled || modelValue.length === 0"
        class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-gray-400 disabled:cursor-not-allowed"
      >
        <span v-if="disabled" class="flex items-center">
          <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          Uploading...
        </span>
        <span v-else>Start OCR Processing</span>
      </button>
    </div>
  </div>
</template>
