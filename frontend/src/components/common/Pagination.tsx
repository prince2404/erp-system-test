/**
 * Pagination controls for list/table navigation.
 */
const Pagination = ({ page, totalPages, onPrevious, onNext }: { page: number; totalPages: number; onPrevious: () => void; onNext: () => void }) => (
  <div className="flex items-center justify-end gap-2">
    <button type="button" onClick={onPrevious} disabled={page === 1} className="rounded-md border border-slate-300 px-3 py-1 text-xs">
      Previous
    </button>
    <span className="text-xs text-slate-600">
      Page {page} of {totalPages}
    </span>
    <button type="button" onClick={onNext} disabled={page === totalPages} className="rounded-md border border-slate-300 px-3 py-1 text-xs">
      Next
    </button>
  </div>
)

export default Pagination
