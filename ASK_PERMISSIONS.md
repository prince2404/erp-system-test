# ASK Platform — Permission Management System

> **Apana Swastha Kendra** | Bihar · Jharkhand · Uttar Pradesh  
> This document is the single source of truth for implementing the complete permission and authority system.  
> Read this entire file before writing any code.

---

## Table of Contents

1. [Overview](#1-overview)
2. [User Hierarchy](#2-user-hierarchy)
3. [Role Permissions — What Each Role Can and Cannot Do](#3-role-permissions)
4. [Authority Rules — Hardcoded and Cannot Be Changed](#4-authority-rules)
5. [Flexible Permission Toggle System](#5-flexible-permission-toggle-system)
6. [Dropdown and UI Filtering Rules](#6-dropdown-and-ui-filtering-rules)
7. [Soft Deactivation Rules](#7-soft-deactivation-rules)
8. [Implementation Instructions](#8-implementation-instructions)
9. [Tests Required for Every Rule](#9-tests-required-for-every-rule)
10. [User Profile System](#10-user-profile-system)

---

## 1. Overview

The permission system has two layers working together:

**Layer 1 — Role-Based Authority (Hardcoded)**  
Every role has a fixed set of rules that never change. Geographic scope, hierarchy direction, and dangerous edit protection are enforced at the database layer on every request. No configuration can override these.

**Layer 2 — Flexible Permission Toggles (Controlled by Super Admin)**  
Within their role's maximum capability, each individual user's CREATE / EDIT / DELETE permissions are controlled by toggle switches that only Super Admin can set. By default every new user gets CREATE = ON, EDIT = OFF, DELETE = OFF.

**The one-line summary:**  
> Roles define the ceiling. Toggles define the floor. Scope and hierarchy are walls that never move.

---

## 2. User Hierarchy

```
Super Admin          → Entire platform (all 3 states)
  └── Admin          → Entire platform (all 3 states)
        └── State Manager       → One state only
              └── District Manager    → One district only
                    └── Block Manager       → One block only
                          ├── Doctor              → One center only
                          ├── Pharmacist          → One center only
                          ├── Receptionist        → One center only
                          ├── HR Manager          → One center only
                          ├── Center Staff        → One center only
                          └── Associate           → Field agent (no center)
Family  → End customer (independent of the above hierarchy)
```

**Key rule:** A user can only create, edit, and deactivate users who are at a **strictly lower level** AND within their **own geographic scope**. Nobody can touch a user at their own level or above.

---

## 3. Role Permissions

### 👑 Super Admin
- **Count:** 1–2 accounts total
- **Scope:** Entire platform — all 3 states
- **Can create:** Admin, State Manager

**CAN DO:**
- Full CRUD on every single user on the entire platform — no toggles needed for Super Admin
- Create and manage Admin and State Manager accounts
- Change core system configuration — **only this role can do this**
- Access and control the Permission Toggle panel for every user on the platform
- Toggle CREATE / EDIT / DELETE on or off for any individual user at any time
- Approve or reject wallet withdrawal requests
- Receive nightly wallet reconciliation mismatch alerts
- View Platform Wallet balance (receives 0.5% of all transactions)
- Access all reports across all centers, districts, and states — export as PDF or Excel
- Revoke / deactivate any user's session tokens instantly
- Approve or reject vendor applications
- View complete audit trail of every wallet transaction
- Edit safe details of any user (name, phone, email, address, photo)
- Edit role or scope of any user
- Deactivate or reactivate any user on the entire platform including the other Super Admin

**CANNOT DO:**
- Hard delete any user — only soft deactivation is ever allowed
- Give any user permission to manage someone at their own level or above via toggles
- Override geographic scope rules via toggles — those are permanently hardcoded

---

### 🛡️ Admin
- **Count:** Few accounts
- **Scope:** Entire platform — all 3 states
- **Can create:** State Manager, District Manager, Block Manager, Doctor, Pharmacist, Receptionist, HR Manager, Center Staff, Associate

**CAN DO:**
- See all data across all 3 states
- Handle all day-to-day platform operations
- Mark payroll as PAID so salary credits to staff wallets
- Approve or reject wallet withdrawal requests and vendor applications
- Create and manage most user account types
- Access all reports and all wallet balances
- Edit safe details of any user (name, phone, email, address)
- Edit role or scope of any user — shared privilege with Super Admin only
- Deactivate or reactivate any user below State Manager level

**CANNOT DO:**
- Change core system configuration — only Super Admin can
- Deactivate a Super Admin account
- View or change anyone's permission toggles — only Super Admin controls toggles
- Hard delete any user

---

### 🗺️ State Manager
- **Count:** Exactly 3 — one per state (Bihar, Jharkhand, UP)
- **Scope:** Their own state only
- **Can create:** District Manager (if CREATE toggle is ON — default ON)
- **Example:** Amit Kumar manages all of Bihar

**CAN DO:**
- See all districts, all centers, all data within their own state
- Create District Manager accounts — if CREATE toggle is ON
- View State Wallet balance (receives 1% of all transactions in their state)
- See revenue, enrollment, and performance data for every center in their state
- Access and export all reports for their state
- Edit safe details of District Managers in their state — **only if EDIT toggle is ON**
- Deactivate District Managers in their state — **only if DELETE toggle is ON**

**CANNOT DO:**
- See any data from the other 2 states — **no toggle can change this**
- Edit or deactivate anyone unless Super Admin has toggled it ON
- Change system configuration or approve withdrawals
- Change anyone's role or scope
- Hard delete any user

---

### 🏛️ District Manager
- **Count:** One per district (up to 38 in Bihar alone)
- **Scope:** Their own district only
- **Can create:** Block Manager (if CREATE toggle is ON — default ON)
- **Example:** Sunita Devi manages all centers in Muzaffarpur

**CAN DO:**
- See all blocks and centers within their district
- Create Block Manager accounts — if CREATE toggle is ON
- View District Wallet balance (receives 2% of transactions in their district)
- See revenue, OPD, and enrollment data for every center in the district
- See active alerts and pending leave requests across the district
- Export district-level reports as PDF or Excel
- Edit safe details of Block Managers in their district — **only if EDIT toggle is ON**
- Deactivate Block Managers in their district — **only if DELETE toggle is ON**

**CANNOT DO:**
- See data from other districts — **no toggle can change this**
- Edit or deactivate anyone unless Super Admin has toggled it ON
- Approve payroll or withdrawals
- Change anyone's role or scope
- Hard delete any user

---

### 🏢 Block Manager
- **Count:** One per block
- **Scope:** Their own block only
- **Can create:** Doctor, Pharmacist, Receptionist, HR Manager, Center Staff, Associate (if CREATE toggle is ON — default ON)
- **Example:** Rajan Singh oversees 5 centers in Block 3, Patna

**CAN DO:**
- Create and configure health centers within their block
- Create staff accounts within their block — if CREATE toggle is ON
- View Block Wallet balance (receives 3% of transactions in their block)
- See all center-level performance data within their block
- Edit safe details of staff in their block — **only if EDIT toggle is ON**
- Deactivate staff in their block — **only if DELETE toggle is ON**

**CANNOT DO:**
- See data from other blocks — **no toggle can change this**
- Create Block Managers or any role above them
- Edit or deactivate anyone unless Super Admin has toggled it ON
- Approve payroll or withdrawals
- Change anyone's role or reassign staff to centers outside their block
- Hard delete any user

---

### 📋 HR Manager
- **Count:** Works at one specific center
- **Scope:** Their assigned center only
- **Can create:** None

**CAN DO:**
- Mark daily attendance: present, absent, half day, on leave, on holiday
- Bulk attendance marking — mark all present, then correct absences individually
- Review and approve or reject leave requests from staff
- Leave balance per staff per year: 12 sick + 12 casual + 15 earned (resets every January)
- Generate monthly payroll — move from DRAFT to PROCESSED state
- Add bonuses to individual staff in payroll
- PDF payslip auto-generated per staff per month

**CANNOT DO:**
- Mark payroll as PAID — only Admin can do that final step
- See data from other centers
- Create any user accounts
- Access patient records, billing, or pharmacy
- Deactivate or edit any user account

---

### 🩺 Doctor
- **Count:** Works at one specific center
- **Scope:** Their assigned center only
- **Can create:** None

**CAN DO:**
- View OPD token queue for their own patients
- Change token status: WAITING → IN_PROGRESS when calling a patient
- Enter diagnosis and prescription for each patient
- Mark visit as COMPLETED — queue auto-advances to next token
- Mark patient as NO_SHOW if called but did not appear
- Admit patient to a ward: General, ICU, Maternity, or Pediatric
- Select a bed and set daily bed charge for admitted patients
- Initiate discharge — bill is auto-calculated
- Set own weekly schedule: working days, start/end time, slot duration in minutes
- View full patient health history: past visits, prescriptions, medicines dispensed

**CANNOT DO:**
- Access billing or generate invoices — Receptionist's job
- Dispense medicines — Pharmacist's job
- Create, edit, or deactivate any user accounts
- See data from other centers
- Access HR or payroll

---

### 💊 Pharmacist
- **Count:** Works at one specific center
- **Scope:** Their assigned center only
- **Can create:** None

**CAN DO:**
- View doctor's prescription linked to each OPD token
- Dispense medicines — system auto-deducts stock using FIFO (oldest batch first)
- Add new stock entries: batch number, expiry date, quantity, price
- View all current stock levels and batch details
- Receive alerts: LOW_STOCK (below 10 units), EXPIRY_SOON (within 30 days), OUT_OF_STOCK
- Browse vendor catalog and place restock orders
- Mark deliveries as received — stock auto-updates, alert resolves
- Track order status: PENDING → CONFIRMED → DISPATCHED → RECEIVED

**CANNOT DO:**
- See data from other centers
- Generate or edit patient invoices
- Create, edit, or deactivate any user accounts
- Access HR or payroll

---

### 🪟 Receptionist
- **Count:** Works at one specific center
- **Scope:** Their assigned center only
- **Can create:** None

**CAN DO:**
- Register new patients: name, DOB, gender, blood group, phone, address, allergies
- Link patient to a family card by scanning QR code
- Create OPD visit and assign next token number automatically
- Assign token to a specific doctor
- Book appointments — view doctor's available slots and book for patient
- Mark tokens as CANCELLED if patient leaves without being seen
- Accept cash wallet top-ups: scan card, enter amount, wallet updates instantly
- Generate itemized invoices: consultation + medicines + procedures + bed charges
- Apply discounts to invoices
- Process payments: card wallet, cash, UPI, debit/credit card, or insurance
- Record insurance provider name and claim ID for reimbursement tracking
- Process refunds on invoices
- Send SMS receipt to patient's registered phone number
- Change family card status: ACTIVE, SUSPENDED, EXPIRED

**CANNOT DO:**
- See data from other centers
- Dispense medicines — Pharmacist's job
- Enter diagnoses or prescriptions — Doctor's job
- Create, edit, or deactivate any user accounts
- Access HR or payroll

---

### 👤 Center Staff
- **Count:** Works at one specific center
- **Scope:** Their assigned center only
- **Can create:** None

**CAN DO:**
- View basic center information — read-only
- View general data relevant to day-to-day duties

**CANNOT DO:**
- Modify any records whatsoever
- Handle money or billing
- Register patients or create OPD visits
- Access pharmacy, HR, or payroll
- Create, edit, or deactivate any user accounts
- Most restricted role — read-only access only

---

### 🚶 Associate
- **Count:** Village-level field agent — not stationed at a center
- **Scope:** Only families they personally enrolled
- **Can create:** None

**CAN DO:**
- Enroll new families: head name, member count, phone, address
- System auto-generates unique card number and QR code on enrollment
- Help families top up their card wallets
- View own Associate Wallet balance in real time
- Earn 4% commission automatically on every purchase by enrolled families — forever
- Commission is credited instantly when payment is recorded
- Submit withdrawal request to transfer wallet balance to bank account
- Store bank account and IFSC details in profile for withdrawals

**CANNOT DO:**
- Receive a salary — commission only, no fixed pay
- Access patient health records
- Access center-level clinical or billing data
- Create, edit, or deactivate any user accounts
- Access HR, payroll, or management reports
- Withdrawal requests require Admin approval before money is transferred
- **If an Associate is deactivated: their enrolled families and all commission records must stay fully intact**

---

### 🏠 Family
- **Count:** End customer — one card per family
- **Scope:** Their own family account only
- **Can create:** None

**CAN DO:**
- View own family health records: OPD visits, diagnoses, prescriptions
- View all medicines dispensed to family members
- View all past invoices and billing history
- Check card wallet balance at any time
- See full wallet transaction history: every top-up and every purchase
- Add money to wallet online via UPI or debit/credit card
- Book appointments with doctors at enrolled centers
- View upcoming and past appointments
- Manage own account profile and personal information
- Request deletion of own data — under DPDP Act 2023

**CANNOT DO:**
- See any other family's data
- Access center operations, staff info, or management data
- Create any user accounts
- Edit medical records — only doctors can enter diagnoses

---

## 4. Authority Rules

These rules are **hardcoded**. No configuration, no toggle, no admin setting can ever override them. They must be enforced at the **database query layer** — not in application code after fetching, and not only on the frontend.

### Rule 1 — Hierarchy is always downward only
A user can only manage (create, edit, deactivate) users who are at a **strictly lower level** than themselves. No exceptions.

```
✅ State Manager can manage District Manager
✅ District Manager can manage Block Manager
❌ District Manager cannot manage State Manager
❌ Block Manager cannot manage Block Manager (same level)
❌ Anyone cannot manage Super Admin except the other Super Admin
```

### Rule 2 — Geographic scope is always enforced at database layer

Every data query must include a scope filter **inside the SQL/query itself** — not applied after fetching results.

| Role | Scope |
|---|---|
| Super Admin | All states |
| Admin | All states |
| State Manager | Their state only |
| District Manager | Their district only |
| Block Manager | Their block only |
| Center-level staff | Their center only |
| Associate | Families they enrolled only |
| Family | Their own account only |

A Bihar State Manager querying Jharkhand data via direct API call must receive a **403 error**, not an empty array.

### Rule 3 — Every API call checks three things

```
Request arrives
    │
    ├── Check 1: Is this user's role allowed to perform this action?
    │       If NO → return 403
    │
    ├── Check 2: Does this request fall within the user's geographic scope?
    │       If NO → return 403
    │
    └── Check 3: Does this user have the specific toggle permission for this action?
            If NO → return 403
            If YES → allow the request
```

All three checks must be on the **server/backend**. Frontend permission hiding is for UX only and is never the security check.

### Rule 4 — Safe edits vs dangerous edits

**Safe edits** (controlled by EDIT toggle):
- Name, phone, email, address, profile photo

**Dangerous edits** (hardcoded to Admin and Super Admin ONLY — no toggle can grant this):
- Role assignment
- Geographic scope / center assignment
- Commission rate
- System configuration

When a lower-level user opens an edit form, dangerous fields must be **completely absent from the API response** — not just hidden on the frontend, not read-only. The server must not return those fields at all.

### Rule 5 — Dropdown role options come from backend

Every role assignment dropdown must call a backend endpoint that returns only the roles the logged-in user is allowed to assign. The frontend renders exactly what the backend returns — nothing more.

```
GET /api/roles/assignable
Authorization: Bearer <token>

Response for State Manager:
{ "roles": ["district_manager"] }

Response for Block Manager:
{ "roles": ["doctor", "pharmacist", "receptionist", "hr_manager", "center_staff", "associate"] }

Response for Admin:
{ "roles": ["state_manager", "district_manager", "block_manager", "doctor", "pharmacist", "receptionist", "hr_manager", "center_staff", "associate"] }
```

A State Manager must **never** see Admin or Super Admin in any dropdown, anywhere in the system.

Similarly, location dropdowns must be scoped:
- State Manager → sees only their own state's districts
- District Manager → sees only their own district's blocks
- Block Manager → sees only their own block's centers

---

## 5. Flexible Permission Toggle System

### What it is

The role defines the **maximum** a user can do.  
Super Admin controls exactly **how much** of that maximum each individual user gets — using toggle switches per user per role.

### Default state for every new user

| Permission | Default |
|---|---|
| CREATE | **ON** |
| EDIT | OFF |
| DELETE | OFF |

### Toggle matrix example — State Manager: Amit Kumar (Bihar)

| Can manage | CREATE | EDIT | DELETE |
|---|---|---|---|
| District Manager | ✅ ON | ❌ OFF | ❌ OFF |
| Block Manager | ❌ OFF | ❌ OFF | ❌ OFF |
| Doctor | ❌ OFF | ❌ OFF | ❌ OFF |
| Geographic scope | Bihar only — permanently hardcoded, not a toggle | | |

### Who controls toggles

- **Super Admin only** — the toggle panel is visible and editable only by Super Admin
- **Admin** — cannot see or change any user's toggles
- **All other roles** — no access to toggle panel

### Super Admin does not need toggles

Super Admin has full CRUD on every user on the platform without any toggle being required. The toggle system is for managing what other users can do — it does not restrict Super Admin in any way.

### How toggles work at runtime

```
1. User logs in
   └── Backend reads their toggle settings from DB
   └── Returns permission profile with login response

2. Frontend receives permission profile
   └── Renders only the UI elements the user has permission for
   └── Absent = not in DOM at all (not disabled, not hidden with CSS)

3. User performs an action (e.g. clicks Edit button)
   └── API call goes to backend
   └── Backend re-checks toggle permission + scope + role level
   └── If any check fails → 403

4. Super Admin edits a user's toggles
   └── Backend updates toggle record in DB
   └── Takes effect on the user's next API call (no re-login required)
```

### Database schema suggestion for toggles

```sql
CREATE TABLE user_permission_toggles (
  id            UUID PRIMARY KEY,
  user_id       UUID NOT NULL REFERENCES users(id),
  target_role   VARCHAR(50) NOT NULL,  -- e.g. 'district_manager'
  can_create    BOOLEAN NOT NULL DEFAULT true,
  can_edit      BOOLEAN NOT NULL DEFAULT false,
  can_delete    BOOLEAN NOT NULL DEFAULT false,
  updated_by    UUID REFERENCES users(id),  -- must be Super Admin
  updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE(user_id, target_role)
);
```

---

## 6. Dropdown and UI Filtering Rules

Every UI element the user has no permission for must be **completely absent from the DOM** — not grayed out, not disabled, not hidden with `display:none`. The server must not return data for elements the user cannot use.

### Role assignment dropdown
- Must call `/api/roles/assignable` — backend returns only allowed roles
- Never hardcode role lists in the frontend

### Location dropdowns
- State dropdown → only shown to Super Admin and Admin
- District dropdown → scoped to user's state for State Manager; all districts for Admin/Super Admin
- Block dropdown → scoped to user's district
- Center dropdown → scoped to user's block

### Action buttons (Edit, Deactivate, Create)
- Read toggle settings from login response
- Render button only if the toggle is ON for that action
- Backend re-validates on every request regardless of frontend state

### Edit form fields
- Safe fields (name, phone, email) → shown if EDIT toggle is ON
- Dangerous fields (role, scope, commission) → **never shown to anyone below Admin level**, the API response must not include these fields at all for lower-level users

---

## 7. Soft Deactivation Rules

**Hard delete is never allowed for any user, by anyone, ever — including Super Admin.**

### What soft deactivation does
- Blocks login immediately — all active session tokens are invalidated
- Account is frozen — no new actions can be taken by this account
- All data is preserved forever:
  - Wallet transaction history
  - Commission records
  - Health records
  - Invoice history
  - Attendance and payroll records

### What soft deactivation does NOT do
- Does not delete any records
- Does not unlink the user from any historical data
- Does not break commission chains (e.g. deactivating an Associate keeps their family enrollments and 4% commission records intact)

### Reactivation
- A deactivated user can be reactivated by anyone who had permission to deactivate them
- On reactivation the user can log in again with their existing credentials
- All historical data is immediately accessible again

### Database pattern
```sql
-- Never delete users. Use status field only.
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'active';
-- status values: 'active', 'deactivated'
-- Never add a 'deleted' status. It does not exist in this system.
```

---

## 8. Implementation Instructions

### Order of implementation — do not skip phases

#### Phase 1 — Geographic scope and hierarchy (Foundation)
Implement scope filtering for Super Admin, Admin, State Manager, District Manager, Block Manager.
- Each level sees only data within their geographic scope
- Each level can only create the level directly below them (if CREATE toggle is ON)
- Scope filter must be inside the database query, not applied after fetching
- Do NOT implement edit or deactivate yet

#### Phase 2 — Center-level staff isolation
Implement scope for Doctor, Pharmacist, Receptionist, HR Manager, Center Staff.
- Each must be locked to their assigned center only at the API level
- A Doctor must not see another center's patients even via direct API call
- A Pharmacist must not see another center's stock

#### Phase 3 — Toggles and safe edits/deactivation
- Build the toggle database table and Super Admin toggle panel UI
- Implement EDIT permission: safe fields only, within scope, toggle must be ON
- Implement DELETE permission: soft deactivation only, within scope, toggle must be ON
- Dangerous fields must be absent from API responses for non-Admin roles

#### Phase 4 — Privilege escalation protection
- Role and scope changes locked to Admin and Super Admin — hardcoded, not toggle-based
- Validate on every edit request that non-Admin users cannot change role/scope fields

#### Phase 5 — Associate and Family
- Associate commission records must remain intact if Associate is deactivated
- Family can only access their own account — no cross-family data access

### General rules for implementation

1. **Never refactor existing working code** to add permissions. Add the permission layer on top.
2. **If a change requires modifying existing logic**, document the conflict before changing anything.
3. **All permission checks live in the backend**. Frontend is UX only.
4. **Dropdown options come from backend API calls**, never hardcoded in the frontend.
5. **Every action must be logged** with: who did it, what they did, when, and on which user.

---

## 9. Tests Required for Every Rule

Every permission rule must have an explicit test that attempts to violate the rule and confirms the violation is rejected.

### Scope tests
```
✅ Bihar State Manager queries Bihar data → 200 OK
❌ Bihar State Manager queries Jharkhand data → 403
❌ Bihar State Manager queries Jharkhand data via direct API call (no frontend) → 403

✅ Patna District Manager queries Patna center data → 200 OK
❌ Patna District Manager queries Muzaffarpur center data → 403

✅ Doctor at Center A queries Center A patient → 200 OK
❌ Doctor at Center A queries Center B patient → 403
```

### Hierarchy tests
```
✅ State Manager creates District Manager → 201 Created
❌ State Manager creates Block Manager (skipping level) → 403
❌ District Manager creates State Manager (going up) → 403
❌ Block Manager creates another Block Manager (same level) → 403
```

### Toggle tests
```
✅ State Manager with EDIT=OFF tries to edit District Manager → 403
✅ Super Admin turns EDIT=ON for State Manager → 200 OK
✅ State Manager with EDIT=ON edits District Manager in own state → 200 OK
❌ State Manager with EDIT=ON edits District Manager in other state → 403

✅ Block Manager with DELETE=OFF tries to deactivate Doctor → 403
✅ Super Admin turns DELETE=ON for Block Manager → 200 OK
✅ Block Manager with DELETE=ON deactivates Doctor in own block → 200 OK
```

### Dropdown tests
```
✅ State Manager calls /api/roles/assignable → returns ["district_manager"] only
❌ State Manager calls /api/roles/assignable → must NOT return "admin" or "super_admin"
✅ Block Manager calls /api/roles/assignable → returns center-level roles only
✅ State Manager location dropdown → returns only their own state's districts
```

### Deactivation tests
```
✅ Deactivated user tries to log in → 401
✅ Deactivated Associate's enrolled families still exist in DB → true
✅ Deactivated Associate's commission records still exist → true
❌ Any attempt to hard delete a user → must be blocked at API level → 405 or 403
```

### Privilege escalation tests
```
❌ Block Manager tries to change Doctor's center assignment → 403
❌ District Manager tries to change Block Manager's role → 403
❌ State Manager tries to edit role field via direct API PATCH → 403
❌ Admin tries to deactivate Super Admin → 403
✅ Super Admin deactivates any user including other Super Admin → 200 OK
```

### Dangerous field tests
```
❌ Block Manager opens edit form for Doctor → API response must NOT contain role/scope fields
❌ State Manager sends PATCH with role field → 403
✅ Admin sends PATCH with role field → 200 OK
✅ Super Admin sends PATCH with role field → 200 OK
```

### Toggle panel access tests
```
✅ Super Admin opens toggle panel for any user → 200 OK
❌ Admin tries to access toggle panel → 403
❌ State Manager tries to access toggle panel → 403
❌ Any role below Super Admin tries to modify toggles → 403
```

---

## Summary Table — Who Can Do What

| Action | Super Admin | Admin | State Mgr | District Mgr | Block Mgr | Others |
|---|---|---|---|---|---|---|
| Create user below them | ✅ | ✅ | If toggle ON | If toggle ON | If toggle ON | ❌ |
| Edit safe details | ✅ | ✅ | If toggle ON | If toggle ON | If toggle ON | ❌ |
| Edit role / scope | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Deactivate user | ✅ | ✅ (not SA) | If toggle ON | If toggle ON | If toggle ON | ❌ |
| Hard delete user | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Control toggles | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Deactivate Super Admin | ✅ (other SA) | ❌ | ❌ | ❌ | ❌ | ❌ |
| Edit commission rates | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Change system config | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

*Generated for Apana Swastha Kendra — April 2026*  
*Author: Prince | github.com/prince2404 | princeconnect2602@gmail.com*

---

## 10. User Profile System

Every user on the platform has a profile page. The profile is divided into 7 sections. Which sections are visible depends on the user's role. All sections are accessible from a sidebar or tab navigation inside the profile page.

---

### Profile Section Visibility by Role

| Section | All Roles | Associates + Managers + Staff | Family Only | Super Admin Only |
|---|---|---|---|---|
| 1. Personal Information | ✅ | ✅ | ✅ | ✅ |
| 2. Verification | ✅ | ✅ | ✅ | ✅ |
| 3. Bank Account | ❌ | ✅ | ❌ | ✅ |
| 4. Security Settings | ✅ | ✅ | ✅ | ✅ |
| 5. Permission Summary | ✅ read-only | ✅ read-only | ❌ | ✅ editable toggles |
| 6. Preferences | ✅ | ✅ | ✅ | ✅ |
| 7. Danger Zone | ✅ | ✅ | ✅ | ✅ |

---

### Section 1 — Personal Information

Fields the user can view and edit themselves:

| Field | Type | Notes |
|---|---|---|
| Full name | Text | Required |
| Profile photo | Image upload | Stored securely, shown across the platform |
| Date of birth | Date picker | Required |
| Gender | Dropdown | Male / Female / Other / Prefer not to say |
| Phone number | Text | Linked to mobile verification — changing it resets verification status |
| Email address | Text | Linked to email verification — changing it resets verification status |
| Home address | Textarea | Street, city, district, state, pincode |
| Emergency contact name | Text | Optional |
| Emergency contact number | Text | Optional |

**Rules:**
- Changing phone or email immediately marks that field as unverified and requires re-verification
- Profile photo is optional but recommended — shown in OPD queue, attendance, and payroll screens
- All fields are editable by the user themselves
- Super Admin and Admin can also edit any user's personal information fields
- Dangerous fields (role, scope) are never in this section

**Database schema:**
```sql
CREATE TABLE user_profiles (
  user_id           UUID PRIMARY KEY REFERENCES users(id),
  full_name         VARCHAR(100) NOT NULL,
  photo_url         VARCHAR(500),
  date_of_birth     DATE,
  gender            VARCHAR(20),
  phone             VARCHAR(15) NOT NULL,
  email             VARCHAR(100) NOT NULL,
  address_street    VARCHAR(200),
  address_city      VARCHAR(100),
  address_district  VARCHAR(100),
  address_state     VARCHAR(50),
  address_pincode   VARCHAR(10),
  emergency_name    VARCHAR(100),
  emergency_phone   VARCHAR(15),
  updated_at        TIMESTAMP DEFAULT NOW()
);
```

---

### Section 2 — Verification

Every user can verify their identity through multiple methods. Each method has an independent verified / unverified status shown as a badge on the profile.

#### 2a — Mobile Verification
- User clicks "Verify Phone"
- System sends a 6-digit OTP to their registered phone number via SMS
- User enters the OTP within 10 minutes
- On correct entry → phone is marked verified
- If phone number is changed later → verification resets to unverified automatically

#### 2b — Email Verification
- User clicks "Verify Email"
- System sends a 6-digit OTP to their registered email address
- User enters the OTP within 30 minutes
- On correct entry → email is marked verified
- If email is changed later → verification resets to unverified automatically

#### 2c — Aadhaar Verification
> ⚠️ **Legal note:** Storing full Aadhaar numbers requires a UIDAI license. Full OTP-based Aadhaar verification (where OTP is sent to the Aadhaar-linked mobile) also requires a separate UIDAI license application that takes several weeks. Until that license is obtained, use the manual verification flow below.

**Manual verification flow (pre-UIDAI license):**
- User enters their last 4 digits of Aadhaar manually
- User uploads a photo of their Aadhaar card (front side)
- Admin reviews the uploaded image and manually marks as verified or rejected
- Only last 4 digits are stored in the database — never store the full 12-digit Aadhaar number

**Future UIDAI flow (post-license):**
- User enters full Aadhaar number
- System calls UIDAI API → sends OTP to Aadhaar-linked mobile
- User enters OTP → verified
- After verification, delete the full Aadhaar number from the system — store only last 4 digits

#### 2d — Photo ID Verification
- User selects ID type: Driving License / Voter Card / Passport / PAN Card
- User uploads front image of the selected ID
- Admin reviews and marks as verified or rejected with a reason
- Rejected users see the rejection reason and can re-upload

**Database schema:**
```sql
CREATE TABLE user_verifications (
  user_id               UUID PRIMARY KEY REFERENCES users(id),
  phone_verified        BOOLEAN DEFAULT false,
  phone_verified_at     TIMESTAMP,
  email_verified        BOOLEAN DEFAULT false,
  email_verified_at     TIMESTAMP,
  aadhaar_last4         VARCHAR(4),
  aadhaar_doc_url       VARCHAR(500),
  aadhaar_status        VARCHAR(20) DEFAULT 'unverified',
  -- aadhaar_status values: 'unverified', 'pending_review', 'verified', 'rejected'
  aadhaar_verified_at   TIMESTAMP,
  aadhaar_reviewed_by   UUID REFERENCES users(id),
  photo_id_type         VARCHAR(30),
  -- photo_id_type values: 'driving_license', 'voter_card', 'passport', 'pan_card'
  photo_id_doc_url      VARCHAR(500),
  photo_id_status       VARCHAR(20) DEFAULT 'unverified',
  -- photo_id_status values: 'unverified', 'pending_review', 'verified', 'rejected'
  photo_id_reject_reason VARCHAR(300),
  photo_id_verified_at  TIMESTAMP,
  photo_id_reviewed_by  UUID REFERENCES users(id)
);
```

**OTP table:**
```sql
CREATE TABLE otp_codes (
  id          UUID PRIMARY KEY,
  user_id     UUID NOT NULL REFERENCES users(id),
  type        VARCHAR(20) NOT NULL,
  -- type values: 'phone', 'email'
  code        VARCHAR(6) NOT NULL,
  expires_at  TIMESTAMP NOT NULL,
  used        BOOLEAN DEFAULT false,
  created_at  TIMESTAMP DEFAULT NOW()
);
```

---

### Section 3 — Bank Account Details

Only visible to: Associates, all Manager levels (State, District, Block), HR Manager, Doctor, Pharmacist, Receptionist, Center Staff, and Super Admin.  
Not visible to: Family accounts.

Users who receive money (commission, salary, withdrawals) must add their bank account before any transfer can be processed.

#### Fields per bank account

| Field | Type | Notes |
|---|---|---|
| Account holder name | Text | Must match the name on the bank account exactly |
| Bank name | Text | e.g. State Bank of India |
| Account number | Text | Masked on display after saving — shown as ••••••4521 |
| IFSC code | Text | 11-character code, validated against IFSC format |
| Account type | Dropdown | Savings / Current |
| Is primary | Boolean | One account marked as primary for payouts |
| Verified | Boolean | Admin manually verifies before any withdrawal is processed |

#### Rules
- A user can add multiple bank accounts
- Only one account can be marked as primary at a time
- Changing the primary account requires password confirmation
- Admin must verify a new bank account before it can receive any transfer
- Account number is encrypted in the database — never stored as plain text
- IFSC code is validated against standard format: 4 letters + 0 + 6 alphanumeric characters
- Withdrawal requests always use the primary verified account

**Database schema:**
```sql
CREATE TABLE user_bank_accounts (
  id              UUID PRIMARY KEY,
  user_id         UUID NOT NULL REFERENCES users(id),
  holder_name     VARCHAR(100) NOT NULL,
  bank_name       VARCHAR(100) NOT NULL,
  account_number  TEXT NOT NULL,
  -- account_number must be encrypted at application layer before storing
  ifsc_code       VARCHAR(11) NOT NULL,
  account_type    VARCHAR(10) NOT NULL DEFAULT 'savings',
  is_primary      BOOLEAN DEFAULT false,
  is_verified     BOOLEAN DEFAULT false,
  verified_by     UUID REFERENCES users(id),
  verified_at     TIMESTAMP,
  created_at      TIMESTAMP DEFAULT NOW(),
  CONSTRAINT one_primary_per_user UNIQUE (user_id, is_primary)
  -- Enforce at application layer: only one is_primary=true per user_id
);
```

---

### Section 4 — Security Settings

All users have access to this section.

#### 4a — Change Password
- User must enter their current password correctly first
- New password must be at least 8 characters with at least one number and one special character
- After successful change → all other active sessions are invalidated immediately
- User stays logged in on the current device only

#### 4b — Active Sessions
- Shows a list of all devices where the user is currently logged in
- Each session shows: device type, approximate location, login time, last active time
- User can click "Log out this device" on any individual session
- User can click "Log out all other devices" to invalidate all sessions except the current one

#### 4c — Two-Factor Authentication (2FA)
- User can enable 2FA
- When enabled, every login requires entering an OTP after the password
- OTP is sent via SMS to verified phone number, or via email to verified email
- User chooses preferred 2FA method
- If phone is not verified, SMS 2FA cannot be enabled
- If email is not verified, email 2FA cannot be enabled

#### 4d — Login History
- Shows last 10 successful logins
- Each entry shows: date, time, device, approximate location
- Read-only — user cannot delete entries

**Database schema:**
```sql
CREATE TABLE user_sessions (
  id            UUID PRIMARY KEY,
  user_id       UUID NOT NULL REFERENCES users(id),
  token_hash    TEXT NOT NULL,
  device_info   VARCHAR(300),
  ip_address    VARCHAR(45),
  location      VARCHAR(200),
  created_at    TIMESTAMP DEFAULT NOW(),
  last_active   TIMESTAMP DEFAULT NOW(),
  expires_at    TIMESTAMP NOT NULL,
  is_revoked    BOOLEAN DEFAULT false
);

CREATE TABLE login_history (
  id          UUID PRIMARY KEY,
  user_id     UUID NOT NULL REFERENCES users(id),
  device_info VARCHAR(300),
  ip_address  VARCHAR(45),
  location    VARCHAR(200),
  success     BOOLEAN NOT NULL,
  created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_2fa_settings (
  user_id         UUID PRIMARY KEY REFERENCES users(id),
  enabled         BOOLEAN DEFAULT false,
  method          VARCHAR(10) DEFAULT 'sms',
  -- method values: 'sms', 'email'
  updated_at      TIMESTAMP DEFAULT NOW()
);
```

---

### Section 5 — Permission Summary

#### For all roles except Super Admin — read-only view
- Shows the user a clear list of what they are allowed to do
- Shows their geographic scope
- Shows their toggle permissions (CREATE / EDIT / DELETE) per role below them
- No edit controls — purely informational
- Helps users understand why certain buttons are not visible to them

**Example display for a State Manager with EDIT toggled ON:**
```
Your Role: State Manager
Your Scope: Bihar

Your Permissions:
  District Manager → CREATE ✅  EDIT ✅  DELETE ❌
  Block Manager    → CREATE ❌  EDIT ❌  DELETE ❌
  Doctor           → CREATE ❌  EDIT ❌  DELETE ❌
```

#### For Super Admin — editable toggle panel
- Same layout as the read-only view but with interactive toggle switches
- Super Admin can toggle any permission ON or OFF for the user being viewed
- Changes take effect immediately — no save button needed, each toggle auto-saves
- Every toggle change is logged: who changed it, what was changed, when
- Super Admin sees this panel when viewing any other user's profile
- Super Admin does not see this panel on their own profile (they have no toggle restrictions)

**Toggle change audit log schema:**
```sql
CREATE TABLE permission_toggle_log (
  id              UUID PRIMARY KEY,
  changed_by      UUID NOT NULL REFERENCES users(id),
  -- changed_by must always be a Super Admin
  target_user_id  UUID NOT NULL REFERENCES users(id),
  target_role     VARCHAR(50) NOT NULL,
  permission      VARCHAR(10) NOT NULL,
  -- permission values: 'create', 'edit', 'delete'
  old_value       BOOLEAN NOT NULL,
  new_value       BOOLEAN NOT NULL,
  changed_at      TIMESTAMP DEFAULT NOW()
);
```

---

### Section 6 — Preferences

All users can set their own preferences. These are personal settings that affect only their own experience.

| Preference | Options | Default |
|---|---|---|
| Language | Hindi / English | English |
| Notification — In-app bell | On / Off per event type | On for all |
| Notification — SMS | On / Off per event type | On for payments only |
| Notification — Email | On / Off per event type | On for account events only |
| Theme | Light / Dark | Light |

#### Notification event types users can control

| Event | In-App | SMS | Email |
|---|---|---|---|
| Commission credited to wallet | ✅ | ✅ | ❌ |
| Wallet top-up received | ✅ | ✅ | ❌ |
| Invoice payment receipt | ✅ | ✅ | ✅ |
| Leave request approved or rejected | ✅ | ❌ | ✅ |
| Low stock alert | ✅ | ❌ | ✅ |
| Appointment reminder | ✅ | ✅ | ❌ |
| Password changed | ✅ | ❌ | ✅ |
| New login from unknown device | ✅ | ✅ | ✅ |
| Withdrawal request approved | ✅ | ✅ | ✅ |

**Database schema:**
```sql
CREATE TABLE user_preferences (
  user_id       UUID PRIMARY KEY REFERENCES users(id),
  language      VARCHAR(10) DEFAULT 'en',
  theme         VARCHAR(10) DEFAULT 'light',
  notif_inapp   JSONB DEFAULT '{}',
  notif_sms     JSONB DEFAULT '{}',
  notif_email   JSONB DEFAULT '{}',
  updated_at    TIMESTAMP DEFAULT NOW()
);
```

---

### Section 7 — Danger Zone

Shown to all users. Displayed at the bottom of the profile page, visually separated with a red border, clearly labelled as irreversible actions.

#### 7a — Deactivate My Own Account
- User can request to deactivate their own account
- Requires password confirmation before proceeding
- Shows a warning: "Your account will be frozen. You will be logged out immediately. All your data will be preserved. An admin can reactivate your account."
- On confirm → account is soft-deactivated, all sessions invalidated, user is logged out
- This is a soft deactivation — all data remains intact

#### 7b — Request Data Deletion (Family accounts only)
- Only available to Family role accounts
- Under the Digital Personal Data Protection Act 2023, families have the right to request deletion of their personal data
- User submits a deletion request with a reason
- Admin reviews the request and processes it
- Personal fields (name, phone, address) can be anonymised
- Health records, invoices, and wallet history are retained for legal and audit purposes and cannot be deleted
- User receives confirmation when the request is processed

**Database schema:**
```sql
CREATE TABLE data_deletion_requests (
  id            UUID PRIMARY KEY,
  user_id       UUID NOT NULL REFERENCES users(id),
  reason        TEXT,
  status        VARCHAR(20) DEFAULT 'pending',
  -- status values: 'pending', 'processed', 'rejected'
  requested_at  TIMESTAMP DEFAULT NOW(),
  processed_by  UUID REFERENCES users(id),
  processed_at  TIMESTAMP,
  notes         TEXT
);
```

---

### Profile API Endpoints

```
GET    /api/profile                          → Get own profile (all sections)
PATCH  /api/profile/personal                 → Update own personal information
POST   /api/profile/verify/phone/send        → Send phone OTP
POST   /api/profile/verify/phone/confirm     → Confirm phone OTP
POST   /api/profile/verify/email/send        → Send email OTP
POST   /api/profile/verify/email/confirm     → Confirm email OTP
POST   /api/profile/verify/aadhaar           → Submit Aadhaar last 4 + upload
POST   /api/profile/verify/photo-id          → Submit photo ID upload
GET    /api/profile/bank-accounts            → List own bank accounts
POST   /api/profile/bank-accounts            → Add a bank account
PATCH  /api/profile/bank-accounts/:id/primary → Set as primary
DELETE /api/profile/bank-accounts/:id        → Remove a bank account (soft)
PATCH  /api/profile/security/password        → Change password
GET    /api/profile/security/sessions        → List active sessions
DELETE /api/profile/security/sessions/:id    → Log out a specific session
DELETE /api/profile/security/sessions        → Log out all other sessions
GET    /api/profile/security/login-history   → Get last 10 logins
PATCH  /api/profile/security/2fa             → Enable or disable 2FA
GET    /api/profile/preferences              → Get preferences
PATCH  /api/profile/preferences              → Update preferences
POST   /api/profile/deactivate               → Self-deactivate (password required)
POST   /api/profile/data-deletion-request    → Request data deletion (Family only)

-- Super Admin only
GET    /api/users/:id/profile                → View any user's full profile
PATCH  /api/users/:id/personal               → Edit any user's personal information
GET    /api/users/:id/toggles                → Get toggle settings for a user
PATCH  /api/users/:id/toggles                → Update toggle settings for a user
POST   /api/users/:id/bank-accounts/:bid/verify → Verify a bank account
POST   /api/users/:id/verify/aadhaar/review  → Approve or reject Aadhaar verification
POST   /api/users/:id/verify/photo-id/review → Approve or reject photo ID verification
```

---

### Profile — Implementation Rules

1. **Changing phone or email resets verification immediately** — enforce this at the database trigger level, not just application code
2. **Account number must be encrypted** at the application layer before it reaches the database — never store plain text account numbers
3. **Full Aadhaar numbers must never be stored** — only last 4 digits. Validate this at the API layer and reject any request that sends a full 12-digit number
4. **Document uploads** (Aadhaar, photo ID) must be stored in a private bucket — never publicly accessible URLs. Generate signed short-lived URLs for viewing
5. **OTPs expire in 10 minutes for SMS and 30 minutes for email** — enforce expiry server-side
6. **A user can request a new OTP only after 60 seconds** — rate limit OTP sends to prevent SMS abuse
7. **Failed OTP attempts** — lock OTP verification for 15 minutes after 5 consecutive failed attempts
8. **2FA OTPs are single-use** — mark as used immediately on first successful verification
9. **Session list** must show approximate location using IP geolocation — never show exact GPS coordinates
10. **All profile changes are audit-logged** — who changed what, from what value to what value, when

---

### Profile — Tests Required

```
-- Personal information
✅ User updates own name → 200 OK, name updated
✅ User changes phone → phone_verified resets to false automatically
✅ User changes email → email_verified resets to false automatically
❌ User tries to update another user's personal info → 403
✅ Super Admin updates any user's personal info → 200 OK
❌ Any user sends role or scope field in personal info PATCH → 403

-- Phone verification
✅ User sends OTP → 200 OK, OTP created in DB
✅ User enters correct OTP within 10 min → phone_verified = true
❌ User enters wrong OTP → 400, attempt counted
❌ User enters OTP after 10 min → 400, expired
❌ User requests new OTP within 60 seconds → 429, rate limited
❌ User fails OTP 5 times → locked for 15 minutes

-- Email verification
✅ Same tests as phone verification with 30 min expiry

-- Aadhaar
✅ User submits last 4 digits + image → status = pending_review
❌ User submits full 12 digit Aadhaar → 400, rejected at API
✅ Admin approves → status = verified
✅ Admin rejects → status = rejected, reason visible to user

-- Bank account
✅ User adds bank account → saved, is_verified = false
✅ Admin verifies account → is_verified = true
❌ Withdrawal requested with unverified bank account → 400
✅ User sets new primary account with password confirm → updated
❌ User sets primary without password confirm → 400
✅ Account number masked on GET response → shows only last 4 digits

-- Security
✅ User changes password with correct current password → 200, other sessions invalidated
❌ User changes password with wrong current password → 401
✅ User logs out specific session → session revoked, that device logged out
✅ User enables 2FA → next login requires OTP
❌ User enables SMS 2FA without verified phone → 400
❌ User enables email 2FA without verified email → 400

-- Self deactivation
✅ User deactivates own account with correct password → account deactivated, logged out
❌ User deactivates own account with wrong password → 401
✅ Deactivated user tries to log in → 401
✅ All data preserved after self-deactivation → true

-- Toggle panel
✅ Super Admin views toggle panel for State Manager → 200 OK
✅ Super Admin changes EDIT to ON → toggle updated, logged in permission_toggle_log
❌ Admin tries to view toggle panel → 403
❌ State Manager tries to view toggle panel → 403

-- Data deletion (Family only)
✅ Family submits deletion request → request created, status = pending
❌ Doctor submits deletion request → 403 (not a Family account)
✅ Admin processes request → status = processed
```

---

*Generated for Apana Swastha Kendra — April 2026*  
*Author: Prince | github.com/prince2404 | princeconnect2602@gmail.com*
