/**
 * Reusable loading indicator for async data states.
 */
const Loader = ({ message = 'Loading...' }: { message?: string }) => <p className="text-sm text-slate-600">{message}</p>

export default Loader
