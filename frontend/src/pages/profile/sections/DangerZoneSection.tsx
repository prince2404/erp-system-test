import { useState } from 'react'
import { useDeactivateSelf, useRequestDataDeletion } from '../../../hooks/useProfileData'
import { useAuth } from '../../../hooks/useAuth'

type Props = { isFamily: boolean }

/**
 * Section 7 — Danger Zone.
 * Self-deactivation (all roles) and data deletion request (Family only).
 */
const DangerZoneSection = ({ isFamily }: Props) => {
  const [deactivatePassword, setDeactivatePassword] = useState('')
  const [showDeactivate, setShowDeactivate] = useState(false)
  const [deletionReason, setDeletionReason] = useState('')
  const [showDeletion, setShowDeletion] = useState(false)

  const deactivateSelf = useDeactivateSelf()
  const requestDeletion = useRequestDataDeletion()
  const { logout } = useAuth()

  const handleDeactivate = async () => {
    await deactivateSelf.mutateAsync(deactivatePassword)
    logout()
  }

  const handleDeletion = async () => {
    await requestDeletion.mutateAsync(deletionReason)
    setShowDeletion(false)
    setDeletionReason('')
  }

  const inputCls = 'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-red-500 focus:ring-1 focus:ring-red-500'

  return (
    <div>
      <h2 className="text-lg font-bold text-red-700">⚠ Danger Zone</h2>
      <p className="mb-6 text-sm text-slate-500">Irreversible actions — proceed with caution</p>

      {/* Deactivate Account */}
      <div className="rounded-lg border-2 border-red-200 bg-red-50/50 p-5">
        <h3 className="text-sm font-semibold text-red-800">Deactivate My Account</h3>
        <p className="mt-1 text-xs text-red-700">
          Your account will be frozen immediately. You will be logged out. All data will be preserved. An admin can reactivate your account.
        </p>
        {!showDeactivate ? (
          <button
            type="button"
            onClick={() => setShowDeactivate(true)}
            className="mt-4 rounded-lg border border-red-300 bg-white px-4 py-2 text-sm font-semibold text-red-700 transition hover:bg-red-50"
          >
            I want to deactivate my account
          </button>
        ) : (
          <div className="mt-4 space-y-3">
            <div>
              <label className="mb-1 block text-xs font-semibold text-red-700">Confirm your password</label>
              <input
                type="password"
                value={deactivatePassword}
                onChange={(e) => setDeactivatePassword(e.target.value)}
                placeholder="Enter your password"
                className={inputCls}
              />
            </div>
            {deactivateSelf.isError && <p className="text-xs text-red-600">Wrong password or deactivation failed.</p>}
            <div className="flex gap-3">
              <button type="button" onClick={() => setShowDeactivate(false)} className="rounded-lg border border-slate-300 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50">
                Cancel
              </button>
              <button
                type="button"
                onClick={() => { void handleDeactivate() }}
                disabled={!deactivatePassword || deactivateSelf.isPending}
                className="rounded-lg bg-red-600 px-5 py-2 text-sm font-semibold text-white hover:bg-red-500 disabled:opacity-60"
              >
                {deactivateSelf.isPending ? 'Deactivating...' : 'Deactivate Now'}
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Data Deletion Request — Family only */}
      {isFamily && (
        <div className="mt-6 rounded-lg border-2 border-red-200 bg-red-50/50 p-5">
          <h3 className="text-sm font-semibold text-red-800">Request Data Deletion</h3>
          <p className="mt-1 text-xs text-red-700">
            Under the Digital Personal Data Protection Act 2023, you have the right to request deletion of your personal data.
            Health records and invoices will be retained for legal/audit purposes.
          </p>
          {!showDeletion ? (
            <button
              type="button"
              onClick={() => setShowDeletion(true)}
              className="mt-4 rounded-lg border border-red-300 bg-white px-4 py-2 text-sm font-semibold text-red-700 transition hover:bg-red-50"
            >
              Request data deletion
            </button>
          ) : (
            <div className="mt-4 space-y-3">
              <div>
                <label className="mb-1 block text-xs font-semibold text-red-700">Reason (optional)</label>
                <textarea
                  value={deletionReason}
                  onChange={(e) => setDeletionReason(e.target.value)}
                  placeholder="Why do you want your data deleted?"
                  rows={3}
                  className={inputCls}
                />
              </div>
              {requestDeletion.isError && <p className="text-xs text-red-600">Failed to submit request.</p>}
              {requestDeletion.isSuccess && <p className="text-xs text-emerald-600">Deletion request submitted. An admin will process it.</p>}
              <div className="flex gap-3">
                <button type="button" onClick={() => setShowDeletion(false)} className="rounded-lg border border-slate-300 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50">
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={() => { void handleDeletion() }}
                  disabled={requestDeletion.isPending}
                  className="rounded-lg bg-red-600 px-5 py-2 text-sm font-semibold text-white hover:bg-red-500 disabled:opacity-60"
                >
                  {requestDeletion.isPending ? 'Submitting...' : 'Submit Request'}
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default DangerZoneSection
