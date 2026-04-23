/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useMemo, useState, type PropsWithChildren } from 'react'

type ThemeContextValue = {
  theme: 'light' | 'dark'
  toggleTheme: () => void
}

const ThemeContext = createContext<ThemeContextValue | undefined>(undefined)

/**
 * Theme provider to host global UI theme preference.
 */
export const ThemeProvider = ({ children }: PropsWithChildren) => {
  const [theme, setTheme] = useState<'light' | 'dark'>('light')

  const value = useMemo(
    () => ({
      theme,
      toggleTheme: () => setTheme((current) => (current === 'light' ? 'dark' : 'light')),
    }),
    [theme],
  )

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
}

/**
 * Accesses theme context state.
 */
export const useThemeContext = () => {
  const context = useContext(ThemeContext)

  if (!context) {
    throw new Error('useThemeContext must be used within ThemeProvider')
  }

  return context
}
