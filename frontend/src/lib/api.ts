import { TOKEN_STORAGE_KEY } from '../constants/appConstants'
import { apiClient } from '../api/axiosInstance'

/**
 * Backward-compatible API export.
 * Prefer domain APIs under src/api for new code.
 */
export { TOKEN_STORAGE_KEY }
export default apiClient
