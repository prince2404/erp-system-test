import { useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { useOwnProfile } from '../../hooks/useProfileData'
import Loader from '../../components/common/Loader'
import ErrorState from '../../components/common/ErrorState'
import PersonalInfoSection from './sections/PersonalInfoSection'
import VerificationSection from './sections/VerificationSection'
import BankAccountSection from './sections/BankAccountSection'
import SecuritySection from './sections/SecuritySection'
import PermissionSummarySection from './sections/PermissionSummarySection'
import PreferencesSection from './sections/PreferencesSection'
import DangerZoneSection from './sections/DangerZoneSection'

const TABS = [
  { key: 'personal', label: 'Personal Info', icon: '👤' },
  { key: 'verification', label: 'Verification', icon: '✅' },
  { key: 'bank', label: 'Bank Accounts', icon: '🏦' },
  { key: 'security', label: 'Security', icon: '🔒' },
  { key: 'permissions', label: 'Permissions', icon: '🛡️' },
  { key: 'preferences', label: 'Preferences', icon: '⚙️' },
  { key: 'danger', label: 'Danger Zone', icon: '⚠️' },
] as const

type TabKey = (typeof TABS)[number]['key']

/** Roles that can see the bank account section */
const BANK_VISIBLE_ROLES = [
  'SUPER_ADMIN', 'ADMIN', 'STATE_MANAGER', 'DISTRICT_MANAGER', 'BLOCK_MANAGER',
  'HR_MANAGER', 'DOCTOR', 'PHARMACIST', 'RECEPTIONIST', 'STAFF', 'CENTER_STAFF', 'ASSOCIATE',
]

/**
 * User profile page with 7 tabbed sections.
 * Section visibility is role-dependent.
 */
const ProfilePage = () => {
  const [activeTab, setActiveTab] = useState<TabKey>('personal')
  const { user } = useAuth()
  const { data: profile, isLoading, isError, refetch } = useOwnProfile()

  const role = user?.role ?? ''
  const isFamily = role === 'FAMILY'
  const showBank = BANK_VISIBLE_ROLES.includes(role)
  const showPermissions = role !== 'FAMILY'

  const visibleTabs = TABS.filter((t) => {
    if (t.key === 'bank' && !showBank) return false
    if (t.key === 'permissions' && !showPermissions) return false
    return true
  })

  if (isLoading) return <Loader message="Loading profile..." />
  if (isError) return <ErrorState message="Unable to load profile." onRetry={() => { void refetch() }} />

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      {/* Header */}
      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <div className="flex items-center gap-4">
          <div className="flex h-16 w-16 items-center justify-center rounded-full bg-indigo-100 text-2xl font-bold text-indigo-600">
            {String(profile?.fullName ?? user?.username ?? '?').charAt(0).toUpperCase()}
          </div>
          <div>
            <h1 className="text-2xl font-bold text-slate-900">
              {String(profile?.fullName || user?.username || 'User')}
            </h1>
            <p className="text-sm text-slate-500">
              {role.replace(/_/g, ' ')} {profile?.scope ? `• ${String(profile.scope)}` : ''}
            </p>
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="flex flex-wrap gap-1 rounded-xl border border-slate-200 bg-white p-1.5 shadow-sm">
        {visibleTabs.map((tab) => (
          <button
            key={tab.key}
            type="button"
            onClick={() => setActiveTab(tab.key)}
            className={`inline-flex items-center gap-1.5 rounded-lg px-4 py-2 text-sm font-medium transition ${
              activeTab === tab.key
                ? 'bg-indigo-600 text-white shadow-sm'
                : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            <span>{tab.icon}</span>
            {tab.label}
          </button>
        ))}
      </div>

      {/* Active Section */}
      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        {activeTab === 'personal' && <PersonalInfoSection profile={profile} />}
        {activeTab === 'verification' && <VerificationSection profile={profile} />}
        {activeTab === 'bank' && showBank && <BankAccountSection />}
        {activeTab === 'security' && <SecuritySection />}
        {activeTab === 'permissions' && showPermissions && <PermissionSummarySection profile={profile} />}
        {activeTab === 'preferences' && <PreferencesSection />}
        {activeTab === 'danger' && <DangerZoneSection isFamily={isFamily} />}
      </div>
    </div>
  )
}

export default ProfilePage
