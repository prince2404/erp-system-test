import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useSearchParams } from 'react-router-dom'
import { useTopUpWallet, useWallet } from '../../hooks/useBillingData'

const schema = z.object({
  healthCardNumber: z.string().trim().min(3, 'Health card number is required'),
  amount: z.number().positive('Amount must be greater than 0'),
})

type FormValues = z.infer<typeof schema>

const formatCurrency = (value: string | number) => `₹${Number(value).toFixed(2)}`

const WalletTopupPage = () => {
  const [searchParams] = useSearchParams()
  const healthCardFromUrl = searchParams.get('healthCardNumber') ?? ''
  const invoiceId = searchParams.get('invoiceId')
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const topUpWallet = useTopUpWallet()
  const walletQuery = useWallet(healthCardFromUrl || undefined)

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      healthCardNumber: healthCardFromUrl,
      amount: 0,
    },
  })

  const onSubmit = async (values: FormValues) => {
    setSuccessMessage(null)
    setSubmitError(null)

    try {
      await topUpWallet.mutateAsync({
        healthCardNumber: values.healthCardNumber,
        amount: values.amount,
      })
      setSuccessMessage(`Wallet topped up with ${formatCurrency(values.amount)}.`)
      reset({
        healthCardNumber: values.healthCardNumber,
        amount: 0,
      })
    } catch {
      setSubmitError('Unable to top-up wallet. Please verify the health card number and amount.')
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-xl font-semibold text-slate-900">Family Wallet Top-Up</h2>
        {invoiceId ? (
          <Link to="/billing/invoices" className="text-sm font-medium text-indigo-700 underline">
            Back to Invoice #{invoiceId}
          </Link>
        ) : null}
      </div>

      {walletQuery.data ? (
        <div className="rounded-md border border-slate-200 bg-white p-4 text-sm text-slate-700">
          <p>
            <span className="font-medium">Family Head:</span> {walletQuery.data.familyHeadName}
          </p>
          <p>
            <span className="font-medium">Health Card:</span> {walletQuery.data.healthCardNumber}
          </p>
          <p>
            <span className="font-medium">Current Balance:</span> {formatCurrency(walletQuery.data.walletBalance)}
          </p>
        </div>
      ) : null}

      <form onSubmit={handleSubmit(onSubmit)} className="grid gap-3 rounded-lg border border-slate-200 bg-white p-4 md:grid-cols-3">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Health Card Number</label>
          <input {...register('healthCardNumber')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          {errors.healthCardNumber ? <p className="mt-1 text-xs text-red-600">{errors.healthCardNumber.message}</p> : null}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Top-Up Amount</label>
          <input
            type="number"
            step="0.01"
            min="0.01"
            {...register('amount', { valueAsNumber: true })}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
          />
          {errors.amount ? <p className="mt-1 text-xs text-red-600">{errors.amount.message}</p> : null}
        </div>
        <div className="flex items-end">
          <button
            type="submit"
            disabled={topUpWallet.isPending}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
          >
            {topUpWallet.isPending ? 'Processing...' : 'Top-Up Wallet'}
          </button>
        </div>
        {successMessage ? <p className="text-xs text-emerald-700 md:col-span-3">{successMessage}</p> : null}
        {submitError ? <p className="text-xs text-red-600 md:col-span-3">{submitError}</p> : null}
      </form>
    </div>
  )
}

export default WalletTopupPage
