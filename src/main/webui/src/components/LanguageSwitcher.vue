<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useLocale, type SupportedLocale } from '../composables/useLocale'

const { currentLocale, supportedLocales, changeLocale } = useLocale()

const isOpen = ref(false)
const containerRef = ref<HTMLElement | null>(null)

function toggleDropdown() {
  isOpen.value = !isOpen.value
}

function selectLocale(locale: SupportedLocale) {
  changeLocale(locale)
  isOpen.value = false
}

function handleClickOutside(event: MouseEvent) {
  if (containerRef.value && !containerRef.value.contains(event.target as Node)) {
    isOpen.value = false
  }
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div
    ref="containerRef"
    class="relative"
    @keydown="handleKeydown"
  >
    <button
      type="button"
      @click="toggleDropdown"
      class="w-9 h-9 flex items-center justify-center rounded-lg text-xs font-black uppercase tracking-wide text-soft-black bg-cream border-2 border-soft-black/20 hover:border-soft-black/40 hover:bg-white transition-colors"
      :aria-expanded="isOpen"
      aria-haspopup="listbox"
      aria-label="Select language"
    >
      {{ currentLocale }}
    </button>

    <!-- Dropdown -->
    <Transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="opacity-0 scale-95"
      enter-to-class="opacity-100 scale-100"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="opacity-100 scale-100"
      leave-to-class="opacity-0 scale-95"
    >
      <ul
        v-if="isOpen"
        class="absolute bottom-full right-0 mb-2 w-32 bg-white rounded-lg border-2 border-soft-black shadow-[3px_3px_0_var(--color-soft-black)] overflow-hidden z-50"
        role="listbox"
        :aria-activedescendant="`lang-${currentLocale}`"
      >
        <li
          v-for="locale in supportedLocales"
          :key="locale.code"
          :id="`lang-${locale.code}`"
          role="option"
          :aria-selected="currentLocale === locale.code"
          @click="selectLocale(locale.code)"
          class="px-3 py-2 text-sm font-bold cursor-pointer transition-colors flex items-center gap-2"
          :class="currentLocale === locale.code
            ? 'bg-primary/10 text-primary'
            : 'text-soft-black hover:bg-cream'"
        >
          <span class="w-6 text-xs font-black uppercase text-center">{{ locale.code }}</span>
          <span>{{ locale.nativeName }}</span>
        </li>
      </ul>
    </Transition>
  </div>
</template>
