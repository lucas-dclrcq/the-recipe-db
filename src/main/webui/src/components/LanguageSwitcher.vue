<script setup lang="ts">
import { useLocale, type SupportedLocale } from '../composables/useLocale'

const { currentLocale, supportedLocales, changeLocale } = useLocale()

function handleChange(event: Event) {
  const select = event.target as HTMLSelectElement
  changeLocale(select.value as SupportedLocale)
}
</script>

<template>
  <div class="relative">
    <select
      :value="currentLocale"
      @change="handleChange"
      class="appearance-none bg-white border-2 border-soft-black/20 rounded-lg px-3 py-1.5 pr-8 text-sm font-bold text-soft-black cursor-pointer hover:border-soft-black/40 focus:outline-none focus:border-primary transition-colors"
      aria-label="Select language"
    >
      <option
        v-for="locale in supportedLocales"
        :key="locale.code"
        :value="locale.code"
      >
        {{ locale.nativeName }}
      </option>
    </select>
    <div class="absolute inset-y-0 right-0 flex items-center pr-2 pointer-events-none">
      <svg class="w-4 h-4 text-soft-black/60" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
      </svg>
    </div>
  </div>
</template>
