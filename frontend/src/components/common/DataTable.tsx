import { useMemo, useState } from 'react'

type Column<T> = {
  key: string
  header: string
  accessor: (item: T) => string | number
}

type DataTableProps<T> = {
  columns: Column<T>[]
  rows: T[]
  pageSize?: number
  emptyText?: string
  getRowKey?: (row: T) => string | number
}

const DataTable = <T,>({
  columns,
  rows,
  pageSize = 10,
  emptyText = 'No records found.',
  getRowKey,
}: DataTableProps<T>) => {
  const [currentPage, setCurrentPage] = useState(1)

  const totalPages = Math.max(1, Math.ceil(rows.length / pageSize))

  const safeCurrentPage = Math.min(currentPage, totalPages)

  const pagedRows = useMemo(() => {
    const start = (safeCurrentPage - 1) * pageSize
    return rows.slice(start, start + pageSize)
  }, [pageSize, rows, safeCurrentPage])

  return (
    <div className="space-y-3">
      <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50">
            <tr>
              {columns.map((column) => (
                <th
                  key={column.key}
                  className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600"
                >
                  {column.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {pagedRows.length > 0 ? (
              pagedRows.map((row, rowIndex) => (
                <tr key={getRowKey ? getRowKey(row) : rowIndex} className="hover:bg-slate-50">
                  {columns.map((column) => (
                    <td key={column.key} className="px-4 py-3 text-slate-700">
                      {column.accessor(row)}
                    </td>
                  ))}
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={columns.length} className="px-4 py-8 text-center text-slate-500">
                  {emptyText}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {rows.length > pageSize ? (
        <div className="flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={() => setCurrentPage(Math.max(1, safeCurrentPage - 1))}
            disabled={safeCurrentPage === 1}
            className="rounded-md border border-slate-300 px-3 py-1 text-xs font-medium text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Previous
          </button>
          <span className="text-xs text-slate-600">
            Page {safeCurrentPage} of {totalPages}
          </span>
          <button
            type="button"
            onClick={() => setCurrentPage(Math.min(totalPages, safeCurrentPage + 1))}
            disabled={safeCurrentPage === totalPages}
            className="rounded-md border border-slate-300 px-3 py-1 text-xs font-medium text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Next
          </button>
        </div>
      ) : null}
    </div>
  )
}

export default DataTable
export type { Column }
