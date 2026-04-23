/**
 * Validation utility helpers for common numeric checks.
 */
export const isPositiveInteger = (value: string): boolean => Number.isInteger(Number(value)) && Number(value) > 0
