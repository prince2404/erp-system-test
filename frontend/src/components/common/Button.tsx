import type { ButtonHTMLAttributes, PropsWithChildren } from 'react'

/**
 * Common button wrapper with default styles.
 */
const Button = ({ children, className = '', ...props }: PropsWithChildren<ButtonHTMLAttributes<HTMLButtonElement>>) => (
  <button {...props} className={`rounded-md px-4 py-2 text-sm font-medium ${className}`.trim()}>
    {children}
  </button>
)

export default Button
