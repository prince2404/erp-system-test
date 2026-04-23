/**
 * Simple status badge component.
 */
const Badge = ({ label }: { label: string }) => (
  <span className="inline-flex rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-700">{label}</span>
)

export default Badge
