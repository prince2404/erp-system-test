/**
 * Shared API envelope and normalized API result types.
 */
export type ApiEnvelope<T> = {
  success: boolean
  message: string
  data: T
}

export type ApiResult<T> = {
  data: T | null
  error: string | null
  status: number
}
