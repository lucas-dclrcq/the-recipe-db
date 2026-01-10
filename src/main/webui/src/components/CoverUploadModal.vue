<script setup lang="ts">
import { ref, computed } from 'vue'
import { PhotoIcon, XMarkIcon } from '@heroicons/vue/24/outline'

interface Props {
  cookbookId: string
  isOpen: boolean
}

interface Emits {
  (e: 'close'): void
  (e: 'uploaded'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const uploadError = ref<string | null>(null)
const isUploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const isDragging = ref(false)

const previewUrl = computed(() => {
  if (!selectedFile.value) return null
  return URL.createObjectURL(selectedFile.value)
})

function isValidFile(file: File): boolean {
  const validTypes = ['image/jpeg', 'image/png']
  if (!validTypes.includes(file.type)) {
    uploadError.value = 'Please select a JPEG or PNG image'
    return false
  }
  return true
}

function onFileChange(e: Event) {
  const t = e.target as HTMLInputElement
  const file = t.files && t.files.length > 0 ? t.files[0] : null
  if (file && isValidFile(file)) {
    selectedFile.value = file
    uploadError.value = null
  }
}

function handleDragOver(e: DragEvent) {
  e.preventDefault()
  isDragging.value = true
}

function handleDragLeave() {
  isDragging.value = false
}

function handleDrop(e: DragEvent) {
  e.preventDefault()
  isDragging.value = false
  if (isUploading.value) return

  const file = e.dataTransfer?.files?.[0]
  if (file && isValidFile(file)) {
    selectedFile.value = file
    uploadError.value = null
  }
}

function openFilePicker() {
  if (!isUploading.value) {
    fileInput.value?.click()
  }
}

function clearSelection() {
  selectedFile.value = null
  uploadError.value = null
  if (fileInput.value) fileInput.value.value = ''
}

function handleClose() {
  if (!isUploading.value) {
    clearSelection()
    emit('close')
  }
}

async function uploadCover() {
  uploadError.value = null
  if (!selectedFile.value) {
    uploadError.value = 'Please choose an image file (JPEG or PNG)'
    return
  }
  const form = new FormData()
  form.append('file', selectedFile.value)
  isUploading.value = true
  try {
    const res = await fetch(`/api/cookbooks/${props.cookbookId}/cover`, {
      method: 'POST',
      body: form,
    })
    if (!res.ok) {
      const txt = await res.text()
      throw new Error(txt || `Upload failed (${res.status})`)
    }
    clearSelection()
    emit('uploaded')
    emit('close')
  } catch (e) {
    uploadError.value = e instanceof Error ? e.message : 'Upload failed'
  } finally {
    isUploading.value = false
  }
}
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 overflow-y-auto">
    <div class="flex min-h-screen items-center justify-center p-4">
      <div class="fixed inset-0 bg-soft-black/40" @click="handleClose"></div>
      <div class="relative card-pop max-w-md w-full p-6 animate-pop-in">
        <h3 class="text-xl font-bold text-soft-black mb-4">Upload Cover Image</h3>

        <input
          ref="fileInput"
          type="file"
          accept="image/jpeg,image/png"
          class="hidden"
          @change="onFileChange"
        />

        <!-- Drop zone / file picker -->
        <div
          v-if="!selectedFile"
          @dragover="handleDragOver"
          @dragleave="handleDragLeave"
          @drop="handleDrop"
          @click="openFilePicker"
          :class="[
            'border-3 border-dashed rounded-xl p-6 text-center cursor-pointer transition-all',
            isDragging ? 'border-primary bg-primary/5' : 'border-soft-black/30 hover:border-soft-black hover:bg-cream',
          ]"
        >
          <PhotoIcon class="mx-auto h-10 w-10 text-charcoal" />
          <p class="mt-2 text-sm text-charcoal">
            <span class="font-bold text-primary">Click to upload</span>
            or drag and drop
          </p>
          <p class="mt-1 text-xs text-charcoal">JPEG or PNG</p>
        </div>

        <!-- Preview selected file -->
        <div v-else class="space-y-4">
          <div class="relative inline-block">
            <div class="w-32 aspect-[3/4] rounded-xl overflow-hidden bg-cream border-3 border-soft-black shadow-[3px_3px_0_var(--color-soft-black)]">
              <img :src="previewUrl!" alt="Preview" class="w-full h-full object-cover" />
            </div>
            <button
              type="button"
              @click="clearSelection"
              class="absolute -top-2 -right-2 bg-soft-black hover:bg-charcoal text-white p-1.5 rounded-lg shadow-sm transition-colors"
              :disabled="isUploading"
            >
              <XMarkIcon class="h-4 w-4" />
            </button>
          </div>
        </div>

        <p v-if="uploadError" class="mt-3 text-sm font-bold text-primary">{{ uploadError }}</p>

        <div class="flex justify-end gap-3 mt-6">
          <button
            type="button"
            class="btn-secondary !py-2 !px-4 text-sm"
            :disabled="isUploading"
            @click="handleClose"
          >
            Cancel
          </button>
          <button
            type="button"
            class="btn-primary !py-2 !px-4 text-sm"
            :disabled="isUploading || !selectedFile"
            @click="uploadCover"
          >
            <svg
              v-if="isUploading"
              class="animate-spin -ml-1 mr-2 h-4 w-4 text-white inline"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ isUploading ? 'Uploading...' : 'Upload Cover' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
