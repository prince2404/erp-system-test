import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useBankAccounts, useAddBankAccount, useRemoveBankAccount, useSetPrimaryBank } from '../../../hooks/useProfileData'
import Loader from '../../../components/common/Loader'
import Modal from '../../../components/common/Modal'

type AddFormValues = {
  holderName: string
  bankName: string
  accountNumber: string
  ifscCode: string
  accountType: string
  isPrimary: boolean
}

/**
 * Section 3 — Bank Account Details.
 * Add, remove, set primary (with password confirmation), masked account display.
 */
const BankAccountSection = () => {
  const [isAddOpen, setIsAddOpen] = useState(false)
  const [primaryTarget, setPrimaryTarget] = useState<number | null>(null)
  const [primaryPassword, setPrimaryPassword] = useState('')

  const { data: accounts, isLoading } = useBankAccounts()
  const addBank = useAddBankAccount()
  const removeBank = useRemoveBankAccount()
  const setPrimary = useSetPrimaryBank()

  const { register, handleSubmit, reset, formState: { errors } } = useForm<AddFormValues>({
    defaultValues: { accountType: 'SAVINGS', isPrimary: false },
  })

  const onAdd = async (values: AddFormValues) => {
    await addBank.mutateAsync(values)
    reset()
    setIsAddOpen(false)
  }

  const onRemove = async (id: number) => {
    if (!confirm('Remove this bank account?')) return
    await removeBank.mutateAsync(id)
  }

  const onSetPrimary = async () => {
    if (primaryTarget === null) return
    await setPrimary.mutateAsync({ id: primaryTarget, password: primaryPassword })
    setPrimaryTarget(null)
    setPrimaryPassword('')
  }

  const inputCls = 'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500'

  if (isLoading) return <Loader message="Loading bank accounts..." />

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-slate-900">Bank Accounts</h2>
          <p className="text-sm text-slate-500">Manage your bank accounts for payouts and withdrawals</p>
        </div>
        <button type="button" onClick={() => setIsAddOpen(true)} className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500">
          Add Account
        </button>
      </div>

      {(!accounts || accounts.length === 0) ? (
        <div className="py-8 text-center text-sm text-slate-500">No bank accounts added yet.</div>
      ) : (
        <div className="space-y-3">
          {accounts.map((acc: Record<string, unknown>) => (
            <div key={String(acc.id)} className="flex items-center justify-between rounded-lg border border-slate-200 p-4 transition hover:border-indigo-200">
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-medium text-slate-900">{String(acc.holderName)}</span>
                  {acc.isPrimary ? (
                    <span className="rounded-full bg-indigo-50 px-2 py-0.5 text-xs font-semibold text-indigo-700">Primary</span>
                  ) : null}
                  <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                    acc.isVerified ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'
                  }`}>
                    {acc.isVerified ? 'Verified' : 'Unverified'}
                  </span>
                </div>
                <p className="mt-1 text-sm text-slate-500">
                  {String(acc.bankName)} • {String(acc.maskedAccountNumber ?? acc.accountNumber)} • {String(acc.ifscCode)} • {String(acc.accountType)}
                </p>
              </div>
              <div className="flex gap-1">
                {!acc.isPrimary && (
                  <button type="button" onClick={() => setPrimaryTarget(Number(acc.id))} className="rounded px-2.5 py-1.5 text-xs font-medium text-indigo-600 hover:bg-indigo-50">
                    Set Primary
                  </button>
                )}
                <button type="button" onClick={() => { void onRemove(Number(acc.id)) }} className="rounded px-2.5 py-1.5 text-xs font-medium text-red-600 hover:bg-red-50">
                  Remove
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Bank Modal */}
      <Modal title="Add Bank Account" isOpen={isAddOpen} onClose={() => setIsAddOpen(false)}>
        <form className="space-y-4" onSubmit={handleSubmit(onAdd)}>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-500">Account Holder Name</label>
            <input {...register('holderName', { required: 'Required' })} className={inputCls} />
            {errors.holderName ? <p className="mt-1 text-xs text-red-600">{errors.holderName.message}</p> : null}
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-500">Bank Name</label>
            <input {...register('bankName', { required: 'Required' })} className={inputCls} />
            {errors.bankName ? <p className="mt-1 text-xs text-red-600">{errors.bankName.message}</p> : null}
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-500">Account Number</label>
            <input {...register('accountNumber', { required: 'Required' })} className={inputCls} />
            {errors.accountNumber ? <p className="mt-1 text-xs text-red-600">{errors.accountNumber.message}</p> : null}
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-500">IFSC Code</label>
            <input {...register('ifscCode', { required: 'Required', pattern: { value: /^[A-Z]{4}0[A-Z0-9]{6}$/, message: 'Invalid IFSC format' } })} className={inputCls} />
            {errors.ifscCode ? <p className="mt-1 text-xs text-red-600">{errors.ifscCode.message}</p> : null}
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-500">Account Type</label>
            <select {...register('accountType')} className={inputCls}>
              <option value="SAVINGS">Savings</option>
              <option value="CURRENT">Current</option>
            </select>
          </div>
          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input type="checkbox" {...register('isPrimary')} className="rounded border-slate-300" />
            Set as primary account
          </label>
          {addBank.isError && <p className="text-xs text-red-600">Failed to add account.</p>}
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={() => setIsAddOpen(false)} className="rounded-lg border border-slate-300 px-4 py-2 text-sm">Cancel</button>
            <button type="submit" disabled={addBank.isPending} className="rounded-lg bg-indigo-600 px-5 py-2 text-sm font-semibold text-white disabled:opacity-60">
              {addBank.isPending ? 'Saving...' : 'Add Account'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Set Primary Confirmation Modal */}
      <Modal title="Confirm Primary Account" isOpen={primaryTarget !== null} onClose={() => setPrimaryTarget(null)}>
        <div className="space-y-4">
          <p className="text-sm text-slate-600">Enter your password to confirm changing the primary bank account.</p>
          <input
            type="password"
            value={primaryPassword}
            onChange={(e) => setPrimaryPassword(e.target.value)}
            placeholder="Enter password"
            className={inputCls}
          />
          {setPrimary.isError && <p className="text-xs text-red-600">Password incorrect or operation failed.</p>}
          <div className="flex justify-end gap-3">
            <button type="button" onClick={() => setPrimaryTarget(null)} className="rounded-lg border border-slate-300 px-4 py-2 text-sm">Cancel</button>
            <button type="button" onClick={() => { void onSetPrimary() }} disabled={!primaryPassword || setPrimary.isPending} className="rounded-lg bg-indigo-600 px-5 py-2 text-sm font-semibold text-white disabled:opacity-60">
              Confirm
            </button>
          </div>
        </div>
      </Modal>
    </div>
  )
}

export default BankAccountSection
