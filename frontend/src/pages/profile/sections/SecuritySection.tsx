import { useState } from 'react'
import { useForm } from 'react-hook-form'
import {
  useChangePassword, useSessions, useRevokeSession, useRevokeOtherSessions,
  useLoginHistory, useUpdateTwoFa,
} from '../../../hooks/useProfileData'
import Loader from '../../../components/common/Loader'

type PasswordForm = { currentPassword: string; newPassword: string; confirmPassword: string }

/**
 * Section 4 — Security Settings.
 * Change password, active sessions, 2FA, login history.
 */
const SecuritySection = () => {
  const [twoFaMethod, setTwoFaMethod] = useState('sms')

  const changePassword = useChangePassword()
  const { data: sessions, isLoading: sessionsLoading } = useSessions()
  const revokeSession = useRevokeSession()
  const revokeOthers = useRevokeOtherSessions()
  const { data: loginHistory, isLoading: historyLoading } = useLoginHistory()
  const updateTwoFa = useUpdateTwoFa()

  const { register, handleSubmit, reset, formState: { errors } } = useForm<PasswordForm>()

  const onChangePassword = async (values: PasswordForm) => {
    if (values.newPassword !== values.confirmPassword) return
    await changePassword.mutateAsync({ currentPassword: values.currentPassword, newPassword: values.newPassword })
    reset()
  }

  const inputCls = 'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500'

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-lg font-bold text-slate-900">Security Settings</h2>
        <p className="text-sm text-slate-500">Manage your password, sessions, and two-factor authentication</p>
      </div>

      {/* Change Password */}
      <div className="rounded-lg border border-slate-200 p-5">
        <h3 className="mb-4 text-sm font-semibold text-slate-800">🔑 Change Password</h3>
        <form className="max-w-sm space-y-3" onSubmit={handleSubmit(onChangePassword)}>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-500">Current Password</label>
            <input type="password" {...register('currentPassword', { required: 'Required' })} className={inputCls} />
            {errors.currentPassword && <p className="mt-1 text-xs text-red-600">{errors.currentPassword.message}</p>}
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-500">New Password</label>
            <input type="password" {...register('newPassword', { required: 'Required', minLength: { value: 8, message: 'Min 8 chars' } })} className={inputCls} />
            {errors.newPassword && <p className="mt-1 text-xs text-red-600">{errors.newPassword.message}</p>}
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-500">Confirm New Password</label>
            <input type="password" {...register('confirmPassword', { required: 'Required' })} className={inputCls} />
          </div>
          {changePassword.isError && <p className="text-xs text-red-600">Wrong current password or password too weak.</p>}
          {changePassword.isSuccess && <p className="text-xs text-emerald-600">Password changed! Other sessions invalidated.</p>}
          <button type="submit" disabled={changePassword.isPending} className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:opacity-60">
            {changePassword.isPending ? 'Changing...' : 'Change Password'}
          </button>
        </form>
      </div>

      {/* Active Sessions */}
      <div className="rounded-lg border border-slate-200 p-5">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-slate-800">📱 Active Sessions</h3>
          <button type="button" onClick={() => revokeOthers.mutate()} disabled={revokeOthers.isPending} className="rounded-lg border border-red-200 px-3 py-1.5 text-xs font-medium text-red-600 hover:bg-red-50">
            Log out all other devices
          </button>
        </div>
        {sessionsLoading ? <Loader message="Loading sessions..." /> : (
          <div className="space-y-2">
            {(sessions ?? []).map((s: Record<string, unknown>) => (
              <div key={String(s.id)} className="flex items-center justify-between rounded border border-slate-100 p-3">
                <div>
                  <p className="text-sm font-medium text-slate-800">{String(s.deviceInfo ?? 'Unknown device')}</p>
                  <p className="text-xs text-slate-500">{String(s.location ?? 'Unknown')} • Last active: {String(s.lastActive ?? '-')}</p>
                </div>
                <button type="button" onClick={() => revokeSession.mutate(Number(s.id))} className="text-xs font-medium text-red-600 hover:text-red-800">
                  Log out
                </button>
              </div>
            ))}
            {(sessions ?? []).length === 0 && <p className="text-sm text-slate-500">No active sessions found.</p>}
          </div>
        )}
      </div>

      {/* Two-Factor Authentication */}
      <div className="rounded-lg border border-slate-200 p-5">
        <h3 className="mb-4 text-sm font-semibold text-slate-800">🔐 Two-Factor Authentication</h3>
        <div className="space-y-3">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => updateTwoFa.mutate({ enabled: true, method: twoFaMethod })}
              disabled={updateTwoFa.isPending}
              className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-500 disabled:opacity-60"
            >
              Enable 2FA
            </button>
            <button
              type="button"
              onClick={() => updateTwoFa.mutate({ enabled: false, method: twoFaMethod })}
              disabled={updateTwoFa.isPending}
              className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Disable 2FA
            </button>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-500">Preferred Method</label>
            <select value={twoFaMethod} onChange={(e) => setTwoFaMethod(e.target.value)} className={inputCls + ' max-w-xs'}>
              <option value="sms">SMS</option>
              <option value="email">Email</option>
            </select>
          </div>
          {updateTwoFa.isError && <p className="text-xs text-red-600">Failed. Ensure your phone/email is verified first.</p>}
          {updateTwoFa.isSuccess && <p className="text-xs text-emerald-600">2FA settings updated.</p>}
        </div>
      </div>

      {/* Login History */}
      <div className="rounded-lg border border-slate-200 p-5">
        <h3 className="mb-4 text-sm font-semibold text-slate-800">📋 Login History</h3>
        {historyLoading ? <Loader message="Loading history..." /> : (
          <div className="space-y-2">
            {(loginHistory ?? []).map((entry: Record<string, unknown>, i: number) => (
              <div key={i} className="flex items-center justify-between rounded border border-slate-100 p-3">
                <div>
                  <p className="text-sm text-slate-800">{String(entry.deviceInfo ?? 'Unknown device')}</p>
                  <p className="text-xs text-slate-500">{String(entry.location ?? 'Unknown')} • {String(entry.createdAt ?? '-')}</p>
                </div>
                <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                  entry.success ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-700'
                }`}>
                  {entry.success ? 'Success' : 'Failed'}
                </span>
              </div>
            ))}
            {(loginHistory ?? []).length === 0 && <p className="text-sm text-slate-500">No login history.</p>}
          </div>
        )}
      </div>
    </div>
  )
}

export default SecuritySection
