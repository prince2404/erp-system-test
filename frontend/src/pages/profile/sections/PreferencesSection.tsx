import { usePreferences, useUpdatePreferences } from '../../../hooks/useProfileData'
import Loader from '../../../components/common/Loader'

const NOTIFICATION_EVENTS = [
  { key: 'commissionCredited', label: 'Commission credited to wallet' },
  { key: 'walletTopUp', label: 'Wallet top-up received' },
  { key: 'invoicePayment', label: 'Invoice payment receipt' },
  { key: 'leaveRequest', label: 'Leave request approved/rejected' },
  { key: 'lowStock', label: 'Low stock alert' },
  { key: 'appointmentReminder', label: 'Appointment reminder' },
  { key: 'passwordChanged', label: 'Password changed' },
  { key: 'newLogin', label: 'New login from unknown device' },
  { key: 'withdrawalApproved', label: 'Withdrawal request approved' },
]

/**
 * Section 6 — Preferences.
 * Language, theme, notification toggles per event type.
 */
const PreferencesSection = () => {
  const { data: prefs, isLoading } = usePreferences()
  const update = useUpdatePreferences()

  if (isLoading) return <Loader message="Loading preferences..." />

  const language = String(prefs?.language ?? 'en')
  const theme = String(prefs?.theme ?? 'light')
  const notifInapp = (prefs?.notifInapp ?? {}) as Record<string, boolean>
  const notifSms = (prefs?.notifSms ?? {}) as Record<string, boolean>
  const notifEmail = (prefs?.notifEmail ?? {}) as Record<string, boolean>

  const handleChange = (field: string, value: unknown) => {
    update.mutate({ [field]: value })
  }

  const toggleNotif = (channel: 'notifInapp' | 'notifSms' | 'notifEmail', key: string, current: boolean) => {
    const channelObj = channel === 'notifInapp' ? notifInapp : channel === 'notifSms' ? notifSms : notifEmail
    update.mutate({ [channel]: { ...channelObj, [key]: !current } })
  }

  const selectCls = 'rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500'

  return (
    <div>
      <h2 className="text-lg font-bold text-slate-900">Preferences</h2>
      <p className="mb-6 text-sm text-slate-500">Personalize your experience</p>

      <div className="mb-8 grid gap-6 sm:grid-cols-2">
        <div>
          <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-500">Language</label>
          <select value={language} onChange={(e) => handleChange('language', e.target.value)} className={selectCls + ' w-full'}>
            <option value="en">English</option>
            <option value="hi">Hindi</option>
          </select>
        </div>
        <div>
          <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-500">Theme</label>
          <select value={theme} onChange={(e) => handleChange('theme', e.target.value)} className={selectCls + ' w-full'}>
            <option value="light">Light</option>
            <option value="dark">Dark</option>
          </select>
        </div>
      </div>

      <h3 className="mb-4 text-sm font-semibold text-slate-700">Notification Preferences</h3>
      <div className="overflow-hidden rounded-lg border border-slate-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-100 bg-slate-50">
              <th className="px-4 py-2.5 text-left font-semibold text-slate-600">Event</th>
              <th className="px-4 py-2.5 text-center font-semibold text-slate-600">In-App</th>
              <th className="px-4 py-2.5 text-center font-semibold text-slate-600">SMS</th>
              <th className="px-4 py-2.5 text-center font-semibold text-slate-600">Email</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {NOTIFICATION_EVENTS.map((evt) => (
              <tr key={evt.key} className="hover:bg-slate-50/50">
                <td className="px-4 py-3 text-slate-700">{evt.label}</td>
                {[
                  { channel: 'notifInapp' as const, data: notifInapp },
                  { channel: 'notifSms' as const, data: notifSms },
                  { channel: 'notifEmail' as const, data: notifEmail },
                ].map(({ channel, data }) => (
                  <td key={channel} className="px-4 py-3 text-center">
                    <button
                      type="button"
                      onClick={() => toggleNotif(channel, evt.key, Boolean(data[evt.key]))}
                      className={`relative inline-flex h-5 w-9 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors ${
                        data[evt.key] ? 'bg-indigo-600' : 'bg-slate-200'
                      }`}
                    >
                      <span className={`inline-block h-4 w-4 transform rounded-full bg-white shadow transition ${
                        data[evt.key] ? 'translate-x-4' : 'translate-x-0'
                      }`} />
                    </button>
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {update.isError && <p className="mt-3 text-xs text-red-600">Failed to update preferences.</p>}
    </div>
  )
}

export default PreferencesSection
