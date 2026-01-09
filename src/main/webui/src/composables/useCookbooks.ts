import { ref, onMounted } from 'vue'
import { getApiCookbooks } from '../api/client'
import type { Cookbook } from '../types/cookbook'

export function useCookbooks() {
  const cookbooks = ref<Cookbook[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  async function fetchCookbooks() {
    isLoading.value = true
    error.value = null

    try {
      const response = await getApiCookbooks()

      if (response.status === 200) {
        cookbooks.value = response.data as Cookbook[]
      } else {
        error.value = 'Failed to load cookbooks'
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'An error occurred'
    } finally {
      isLoading.value = false
    }
  }

  async function searchCookbooks(q: string) {
    isLoading.value = true
    error.value = null
    try {
      const url = q && q.trim().length > 0 ? `/api/cookbooks?q=${encodeURIComponent(q.trim())}` : '/api/cookbooks'
      const res = await fetch(url, { headers: { Accept: 'application/json' } })
      if (!res.ok) {
        throw new Error(`Failed to search cookbooks: ${res.status}`)
      }
      const data = (await res.json()) as Cookbook[]
      cookbooks.value = data
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'An error occurred while searching'
    } finally {
      isLoading.value = false
    }
  }

  onMounted(() => {
    fetchCookbooks()
  })

  return {
    cookbooks,
    isLoading,
    error,
    fetchCookbooks,
    searchCookbooks,
  }
}
