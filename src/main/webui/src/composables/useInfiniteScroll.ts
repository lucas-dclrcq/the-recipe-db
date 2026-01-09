import { ref, onMounted, onUnmounted, watch } from 'vue'

interface UseInfiniteScrollOptions {
  threshold?: number
  rootMargin?: string
}

export function useInfiniteScroll(
  callback: () => void | Promise<void>,
  options: UseInfiniteScrollOptions = {}
) {
  const { threshold = 0.1, rootMargin = '100px' } = options

  const targetRef = ref<HTMLElement | null>(null)
  const isIntersecting = ref(false)
  const isEnabled = ref(true)

  let observer: IntersectionObserver | null = null

  function createObserver() {
    if (observer) {
      observer.disconnect()
    }

    observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0]
        isIntersecting.value = entry?.isIntersecting ?? false

        if (entry?.isIntersecting && isEnabled.value) {
          callback()
        }
      },
      {
        threshold,
        rootMargin,
      }
    )

    if (targetRef.value) {
      observer.observe(targetRef.value)
    }
  }

  function enable() {
    isEnabled.value = true
  }

  function disable() {
    isEnabled.value = false
  }

  watch(targetRef, (newTarget, oldTarget) => {
    if (observer) {
      if (oldTarget) {
        observer.unobserve(oldTarget)
      }
      if (newTarget) {
        observer.observe(newTarget)
      }
    }
  })

  onMounted(() => {
    createObserver()
  })

  onUnmounted(() => {
    if (observer) {
      observer.disconnect()
      observer = null
    }
  })

  return {
    targetRef,
    isIntersecting,
    isEnabled,
    enable,
    disable,
  }
}
