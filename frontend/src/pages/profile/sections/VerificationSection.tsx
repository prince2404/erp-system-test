import { useState } from 'react'
import {
  useSendPhoneOtp, useConfirmPhoneOtp,
  useSendEmailOtp, useConfirmEmailOtp,
  useSubmitAadhaar, useSubmitPhotoId,
} from '../../../hooks/useProfileData'

type Props = { profile: Record<string, unknown> | undefined }

const StatusBadge = ({ verified, label }: { verified: boolean; label: string }) => (
  <span className={`inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-semibold ${
    verified ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'
  }`}>
    {verified ? '✓' : '✕'} {label}
  </span>
)

/**
 * Section 2 — Verification (phone, email, Aadhaar, photo ID).
 */
const VerificationSection = ({ profile }: Props) => {
  const [phoneOtpCode, setPhoneOtpCode] = useState('')
  const [emailOtpCode, setEmailOtpCode] = useState('')
  const [aadhaarLast4, setAadhaarLast4] = useState('')
  const [aadhaarDocUrl, setAadhaarDocUrl] = useState('')
  const [photoIdType, setPhotoIdType] = useState('')
  const [photoIdDocUrl, setPhotoIdDocUrl] = useState('')

  const sendPhoneOtp = useSendPhoneOtp()
  const confirmPhoneOtp = useConfirmPhoneOtp()
  const sendEmailOtp = useSendEmailOtp()
  const confirmEmailOtp = useConfirmEmailOtp()
  const submitAadhaar = useSubmitAadhaar()
  const submitPhotoId = useSubmitPhotoId()

  const phoneVerified = Boolean(profile?.phoneVerified)
  const emailVerified = Boolean(profile?.emailVerified)
  const aadhaarStatus = String(profile?.aadhaarStatus ?? 'unverified')
  const photoIdStatus = String(profile?.photoIdStatus ?? 'unverified')

  const inputCls = 'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500'
  const btnPrimary = 'rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:opacity-60'

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-lg font-bold text-slate-900">Identity Verification</h2>
        <p className="text-sm text-slate-500">Verify your identity through multiple methods</p>
      </div>

      {/* Phone */}
      <div className="rounded-lg border border-slate-200 p-4">
        <div className="mb-3 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-slate-800">📱 Mobile Verification</h3>
          <StatusBadge verified={phoneVerified} label={phoneVerified ? 'Verified' : 'Unverified'} />
        </div>
        {!phoneVerified && (
          <div className="space-y-3">
            <button type="button" onClick={() => sendPhoneOtp.mutate()} disabled={sendPhoneOtp.isPending} className={btnPrimary}>
              {sendPhoneOtp.isPending ? 'Sending...' : 'Send OTP'}
            </button>
            {sendPhoneOtp.isSuccess && (
              <div className="flex gap-2">
                <input value={phoneOtpCode} onChange={(e) => setPhoneOtpCode(e.target.value)} placeholder="Enter 6-digit OTP" maxLength={6} className={inputCls} />
                <button type="button" onClick={() => confirmPhoneOtp.mutate(phoneOtpCode)} disabled={confirmPhoneOtp.isPending || phoneOtpCode.length < 6} className={btnPrimary}>
                  Verify
                </button>
              </div>
            )}
            {confirmPhoneOtp.isError && <p className="text-xs text-red-600">Verification failed. Check OTP and try again.</p>}
            {sendPhoneOtp.isError && <p className="text-xs text-red-600">Failed to send OTP. Please wait 60 seconds.</p>}
          </div>
        )}
      </div>

      {/* Email */}
      <div className="rounded-lg border border-slate-200 p-4">
        <div className="mb-3 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-slate-800">📧 Email Verification</h3>
          <StatusBadge verified={emailVerified} label={emailVerified ? 'Verified' : 'Unverified'} />
        </div>
        {!emailVerified && (
          <div className="space-y-3">
            <button type="button" onClick={() => sendEmailOtp.mutate()} disabled={sendEmailOtp.isPending} className={btnPrimary}>
              {sendEmailOtp.isPending ? 'Sending...' : 'Send OTP'}
            </button>
            {sendEmailOtp.isSuccess && (
              <div className="flex gap-2">
                <input value={emailOtpCode} onChange={(e) => setEmailOtpCode(e.target.value)} placeholder="Enter 6-digit OTP" maxLength={6} className={inputCls} />
                <button type="button" onClick={() => confirmEmailOtp.mutate(emailOtpCode)} disabled={confirmEmailOtp.isPending || emailOtpCode.length < 6} className={btnPrimary}>
                  Verify
                </button>
              </div>
            )}
            {confirmEmailOtp.isError && <p className="text-xs text-red-600">Verification failed. Check OTP and try again.</p>}
            {sendEmailOtp.isError && <p className="text-xs text-red-600">Failed to send OTP. Please wait 60 seconds.</p>}
          </div>
        )}
      </div>

      {/* Aadhaar */}
      <div className="rounded-lg border border-slate-200 p-4">
        <div className="mb-3 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-slate-800">🪪 Aadhaar Verification</h3>
          <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${
            aadhaarStatus === 'verified' ? 'bg-emerald-50 text-emerald-700'
            : aadhaarStatus === 'pending_review' ? 'bg-blue-50 text-blue-700'
            : aadhaarStatus === 'rejected' ? 'bg-red-50 text-red-700'
            : 'bg-amber-50 text-amber-700'
          }`}>
            {aadhaarStatus.replace(/_/g, ' ').toUpperCase()}
          </span>
        </div>
        {aadhaarStatus !== 'verified' && (
          <div className="space-y-3">
            <div>
              <label className="mb-1 block text-xs font-semibold text-slate-500">Last 4 digits of Aadhaar</label>
              <input value={aadhaarLast4} onChange={(e) => setAadhaarLast4(e.target.value.slice(0, 4))} maxLength={4} placeholder="XXXX" className={inputCls} />
            </div>
            <div>
              <label className="mb-1 block text-xs font-semibold text-slate-500">Aadhaar Document URL (optional)</label>
              <input value={aadhaarDocUrl} onChange={(e) => setAadhaarDocUrl(e.target.value)} placeholder="URL to uploaded image" className={inputCls} />
            </div>
            <button
              type="button"
              onClick={() => submitAadhaar.mutate({ aadhaarLast4, aadhaarDocUrl: aadhaarDocUrl || undefined })}
              disabled={submitAadhaar.isPending || aadhaarLast4.length !== 4}
              className={btnPrimary}
            >
              {submitAadhaar.isPending ? 'Submitting...' : 'Submit for Review'}
            </button>
            {submitAadhaar.isError && <p className="text-xs text-red-600">Submission failed. Must be exactly 4 digits.</p>}
          </div>
        )}
      </div>

      {/* Photo ID */}
      <div className="rounded-lg border border-slate-200 p-4">
        <div className="mb-3 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-slate-800">🆔 Photo ID Verification</h3>
          <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${
            photoIdStatus === 'verified' ? 'bg-emerald-50 text-emerald-700'
            : photoIdStatus === 'pending_review' ? 'bg-blue-50 text-blue-700'
            : photoIdStatus === 'rejected' ? 'bg-red-50 text-red-700'
            : 'bg-amber-50 text-amber-700'
          }`}>
            {photoIdStatus.replace(/_/g, ' ').toUpperCase()}
          </span>
        </div>
        {profile?.photoIdRejectReason ? (
          <div className="mb-3 rounded-lg bg-red-50 p-3 text-xs text-red-700">
            Rejection reason: {String(profile.photoIdRejectReason)}
          </div>
        ) : null}
        {photoIdStatus !== 'verified' && (
          <div className="space-y-3">
            <div>
              <label className="mb-1 block text-xs font-semibold text-slate-500">ID Type</label>
              <select value={photoIdType} onChange={(e) => setPhotoIdType(e.target.value)} className={inputCls}>
                <option value="">Select type</option>
                <option value="DRIVING_LICENSE">Driving License</option>
                <option value="VOTER_CARD">Voter Card</option>
                <option value="PASSPORT">Passport</option>
                <option value="PAN_CARD">PAN Card</option>
              </select>
            </div>
            <div>
              <label className="mb-1 block text-xs font-semibold text-slate-500">Document URL</label>
              <input value={photoIdDocUrl} onChange={(e) => setPhotoIdDocUrl(e.target.value)} placeholder="URL to uploaded image" className={inputCls} />
            </div>
            <button
              type="button"
              onClick={() => submitPhotoId.mutate({ photoIdType, photoIdDocUrl: photoIdDocUrl || undefined })}
              disabled={submitPhotoId.isPending || !photoIdType}
              className={btnPrimary}
            >
              {submitPhotoId.isPending ? 'Submitting...' : 'Submit for Review'}
            </button>
            {submitPhotoId.isError && <p className="text-xs text-red-600">Submission failed.</p>}
          </div>
        )}
      </div>
    </div>
  )
}

export default VerificationSection
