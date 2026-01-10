<script setup lang="ts">
import { useCookbooks } from '../composables/useCookbooks'
import {useI18n} from "vue-i18n";

const { t } = useI18n()

defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const { cookbooks, isLoading } = useCookbooks()

function handleChange(event: Event) {
  const target = event.target as HTMLSelectElement
  emit('update:modelValue', target.value)
}
</script>

<template>
  <div>
    <select
      :value="modelValue"
      :disabled="isLoading"
      class="block w-full px-3 py-2 text-base border border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md disabled:bg-gray-100"
      @change="handleChange"
    >
      <option value="">{{t('filters.allCookbooks')}}</option>
      <option v-for="cookbook in cookbooks" :key="cookbook.id" :value="cookbook.id">
        {{ cookbook.title }}
        <template v-if="cookbook.author"> - {{ cookbook.author }}</template>
        ({{ cookbook.recipeCount }})
      </option>
    </select>
  </div>
</template>
