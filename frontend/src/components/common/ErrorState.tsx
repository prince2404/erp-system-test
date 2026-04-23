/**
 * Reusable error state with optional retry callback.
 */
const ErrorState = ({ message, onRetry }: { message: string; onRetry?: () => void }) => (
  <div className="rounded-lg border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
    <p>{message}</p>
    {onRetry ? (
      <button type="button" onClick={onRetry} className="mt-2 rounded-md bg-rose-600 px-3 py-1 text-xs font-medium text-white">
        Retry
      </button>
    ) : null}
  </div>
)

export default ErrorState
