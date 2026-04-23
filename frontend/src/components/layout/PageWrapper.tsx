import type { PropsWithChildren } from 'react'

/**
 * Standard page content wrapper for dashboard routes.
 */
const PageWrapper = ({ children }: PropsWithChildren) => <section className="p-6">{children}</section>

export default PageWrapper
