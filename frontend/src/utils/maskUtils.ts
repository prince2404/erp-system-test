/**
 * Masking utility for partially hiding sensitive card-like values.
 */
export const maskMiddle = (value: string): string => {
  if (value.length <= 4) {
    return value
  }

  const visible = value.slice(-4)
  return `${'*'.repeat(Math.max(0, value.length - 4))}${visible}`
}
