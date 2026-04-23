/**
 * Currency utility for INR-prefixed display fallback.
 */
export const toInr = (value: number | string): string => `₹${value}`
