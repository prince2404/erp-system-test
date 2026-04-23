/**
 * Reusable empty state display for empty list/table results.
 */
const EmptyState = ({ message }: { message: string }) => (
  <div className="rounded-lg border border-slate-200 bg-white p-6 text-center text-sm text-slate-600">{message}</div>
)

export default EmptyState
