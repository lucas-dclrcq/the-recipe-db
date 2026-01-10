import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { setLocale, getLocale } from '../i18n'

export type SupportedLocale = 'en' | 'fr'

export interface LocaleOption {
  code: SupportedLocale
  name: string
  nativeName: string
}

export const supportedLocales: LocaleOption[] = [
  { code: 'en', name: 'English', nativeName: 'English' },
  { code: 'fr', name: 'French', nativeName: 'Francais' },
]

export function useLocale() {
  const { locale } = useI18n()

  const currentLocale = computed(() => locale.value as SupportedLocale)

  const currentLocaleOption = computed(() =>
    supportedLocales.find((l) => l.code === currentLocale.value) ?? supportedLocales[0]
  )

  function changeLocale(newLocale: SupportedLocale) {
    setLocale(newLocale)
  }

  return {
    currentLocale,
    currentLocaleOption,
    supportedLocales,
    changeLocale,
    getLocale,
  }
}
