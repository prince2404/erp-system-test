/**
 * Formatting helpers consumed by tables and dashboards.
 */
export const formatService = {
  /** Formats INR currency values for compact display. */
  formatCurrencyInr: (value: number | string) => {
    const numericValue = Number(value)
    if (Number.isNaN(numericValue)) {
      return String(value)
    }

    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(numericValue)
  },
}
