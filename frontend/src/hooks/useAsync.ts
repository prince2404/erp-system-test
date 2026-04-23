import { useCallback, useState } from 'react'

/**
 * Async execution helper that tracks loading and error state.
 */
export const useAsync = <TArgs extends unknown[], TResult>(asyncFn: (...args: TArgs) => Promise<TResult>) => {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const run = useCallback(
    async (...args: TArgs) => {
      setIsLoading(true)
      setError(null)

      try {
        return await asyncFn(...args)
      } catch {
        setError('Unexpected error occurred.')
        return null
      } finally {
        setIsLoading(false)
      }
    },
    [asyncFn],
  )

  return { run, isLoading, error }
}
