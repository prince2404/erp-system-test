/**
 * Confirmation dialog wrapper based on shared modal pattern.
 */
const ConfirmDialog = ({
  title,
  description,
  onConfirm,
  onCancel,
}: {
  title: string
  description: string
  onConfirm: () => void
  onCancel: () => void
}) => (
  <div className="rounded-lg bg-white p-4 shadow">
    <h3 className="text-base font-semibold text-slate-900">{title}</h3>
    <p className="mt-2 text-sm text-slate-600">{description}</p>
    <div className="mt-4 flex justify-end gap-2">
      <button type="button" onClick={onCancel} className="rounded-md border border-slate-300 px-3 py-1 text-sm">
        Cancel
      </button>
      <button type="button" onClick={onConfirm} className="rounded-md bg-rose-600 px-3 py-1 text-sm text-white">
        Confirm
      </button>
    </div>
  </div>
)

export default ConfirmDialog
