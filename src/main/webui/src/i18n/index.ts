import { createI18n } from 'vue-i18n'
import en from './locales/en.json'
import fr from './locales/fr.json'

export type MessageSchema = typeof en

const LOCALE_KEY = 'recipe-db-locale'

function getInitialLocale(): 'en' | 'fr' {
  // Check for saved preference
  const saved = localStorage.getItem(LOCALE_KEY)
  if (saved === 'en' || saved === 'fr') {
    return saved
  }

  // Check browser language
  const browserLang = navigator.language.split('-')[0]
  if (browserLang === 'fr') {
    return 'fr'
  }

  // Default to English
  return 'en'
}

export const i18n = createI18n<[MessageSchema], 'en' | 'fr'>({
  legacy: false,
  locale: getInitialLocale(),
  fallbackLocale: 'en',
  messages: {
    en,
    fr,
  },
})

export function setLocale(locale: 'en' | 'fr') {
  // @ts-expect-error - vue-i18n type issue with locale.value
  i18n.global.locale.value = locale
  localStorage.setItem(LOCALE_KEY, locale)
  document.documentElement.lang = locale
}

export function getLocale(): 'en' | 'fr' {
  // @ts-expect-error - vue-i18n type issue with locale.value
  return i18n.global.locale.value
}
