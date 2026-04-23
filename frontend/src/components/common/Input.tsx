import type { InputHTMLAttributes } from 'react'

/**
 * Common input wrapper for consistent form styling.
 */
const Input = (props: InputHTMLAttributes<HTMLInputElement>) => (
  <input {...props} className={`w-full rounded-md border border-slate-300 px-3 py-2 text-sm ${props.className ?? ''}`.trim()} />
)

export default Input
