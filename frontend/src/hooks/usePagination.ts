import { useMemo, useState } from 'react'

/**
 * Generic pagination helper hook for client-side table/list pagination.
 */
export const usePagination = <T,>(items: T[], pageSize = 10) => {
  const [page, setPage] = useState(1)

  const totalPages = Math.max(1, Math.ceil(items.length / pageSize))
  const safePage = Math.min(page, totalPages)

  const pagedItems = useMemo(() => {
    const start = (safePage - 1) * pageSize
    return items.slice(start, start + pageSize)
  }, [items, pageSize, safePage])

  return { page: safePage, totalPages, setPage, pagedItems }
}
