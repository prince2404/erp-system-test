import { Component, type ErrorInfo, type ReactNode } from 'react'

type ErrorBoundaryProps = {
  children: ReactNode
}

type ErrorBoundaryState = {
  hasError: boolean
}

/**
 * Captures runtime render errors and shows fallback UI.
 */
class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { hasError: false }

  static getDerivedStateFromError(): ErrorBoundaryState {
    return { hasError: true }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    void error
    void errorInfo
    // noop
  }

  render() {
    if (this.state.hasError) {
      return <p className="text-sm text-rose-600">Something went wrong.</p>
    }

    return this.props.children
  }
}

export default ErrorBoundary
