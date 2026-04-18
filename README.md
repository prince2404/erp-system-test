# 📋 Apana Swastha Kendra ERP – Complete System Design (Enhanced Version)

---

# 🎯 1. Project Overview

**Apana Swastha Kendra ERP** is a scalable Rural Healthcare Distribution & Operations Platform designed to transform healthcare delivery in Bihar, Jharkhand, and Uttar Pradesh.

## Core Objectives:
- Establish healthcare centers at block/panchayat level
- Provide digital health identity via smart health cards
- Enable OPD/IPD clinical workflows
- Implement a commission-based ecosystem
- Ensure transparency, scalability, and auditability

## Target Scale:
- ~6 million users
- ₹30 crore/month revenue
- High transaction throughput

## Key Innovation:
A telecom-style hierarchical network (similar to Jio model) enabling:
- Structured expansion
- Automated commission distribution
- Decentralized execution with centralized monitoring

---

# 🏗️ 2. System Architecture

## Frontend:
- React 18+
- Tailwind CSS
- React Query
- Axios
- React Router
- Recharts
- Bun

## Backend:
- Spring Boot 3 (Java 17+)
- Spring Security (JWT)
- Hibernate + JPA
- REST APIs (Layered Architecture)

## Database:
- MySQL 8+
- Indexed queries
- Foreign key constraints

## Future Enhancements:
- Redis (Caching)
- AWS S3 (File storage)
- Queue system (Notifications)

---

# 👥 3. User Roles & Hierarchy

Super Admin  
↓  
Admin  
↓  
State Manager  
↓  
District Manager  
↓  
Block Manager  
↓  
Center Level:  
- HR Manager  
- Doctor  
- Pharmacist  
- Receptionist  
- Staff  
↓  
Associate  
↓  
Family  

## Rules:
- Users can only create/manage lower roles
- Strict geographic filtering
- No cross-level access

---

# 🔐 4. Access Control System

## Structure:
User → Role → Permissions (Many-to-Many)

## Permission Examples:
- CREATE_USER
- EDIT_USER
- DELETE_USER
- VIEW_REPORTS
- CREATE_INVOICE
- DISPENSE_MEDICINE
- MANAGE_INVENTORY
- APPROVE_PAYROLL
- VIEW_FINANCIALS

## Features:
- Dynamic permission assignment
- Custom role creation
- Permission overrides by Super Admin

---

# 👑 5. Super Admin Capabilities

## Allowed:
- Full CRUD across system
- Modify hierarchy
- Manage permissions
- Configure commission rules
- Access all reports and logs

## Restricted:
- Cannot delete financial transactions
- Cannot modify completed invoices
- Cannot delete audit logs
- Cannot alter medical history

## Correction Mechanism:
- Reversal transactions
- Full audit logging

---

# 🌍 6. Geographic Hierarchy

State → District → Block → Center

## Rules:
- Region-based data visibility
- Managers limited to assigned geography

---

# 👨‍👩‍👧‍👦 7. Family & Identity System

## Features:
- Unique Health Card
- QR Code generation
- Digital Wallet

## Card Format:
ASK-BH-PTN-003-00045

---

# 🔄 8. Core Workflows

## 8.1 System Setup
- Create hierarchy
- Assign staff
- Send credentials

## 8.2 Family Enrollment
- Register family
- Generate card
- Create wallet
- Generate QR

## 8.3 OPD Workflow
Receptionist → Token  
Doctor → Diagnosis  
Pharmacist → Dispense Medicine  
Receptionist → Billing  
System → Commission Distribution  

## 8.4 Payment Flow

### Modes:
- Wallet
- Cash
- UPI
- Card
- Insurance

## 8.5 Commission System

### Distribution:
- Associate → 4%
- Block → 3%
- District → 2%
- State → 1%
- Platform → 0.5%

### Rules:
- Based on invoice total
- Atomic transactions
- Center fallback if no associate

## 8.6 Inventory Flow
- FIFO dispensing
- Batch tracking
- Expiry alerts
- Vendor ordering

## 8.7 IPD Flow
- Admission
- Bed allocation
- Daily billing
- Discharge

## 8.8 HR & Payroll
- Attendance
- Leave
- Salary calculation
- Wallet credit

---

# 🏗️ 9. Core Modules

1. User Management  
2. Profile Management  
3. Patient Registration  
4. OPD Queue  
5. IPD Management  
6. Scheduling  
7. Pharmacy  
8. Vendor Management  
9. Billing  
10. HR & Payroll  
11. Reporting  

## Cross-Cutting:
- Notifications  
- Audit Logging  
- Security  
- Config Management  

---

# 💰 10. Business Logic

## Transaction Safety:
@Transactional

## Idempotency:
- Unique request IDs

## Inventory:
- FIFO + expiry priority

## Payroll:
- Prorated salary
- Deductions + bonuses

---

# 🧾 11. Audit Logging System

## Tracks:
- User actions
- Financial operations
- Data changes

## Example:
- USER_CREATED
- PAYMENT_SUCCESS
- INVOICE_GENERATED
- STOCK_UPDATED

## Fields:
- user_id
- action
- timestamp
- IP
- entity

---

# 🧹 12. Soft Delete System

is_deleted = true  
deleted_at  

---

# ⚙️ 13. API Standards

## Response Format:
{
  "success": true,
  "message": "",
  "data": {}
}

## Features:
- Global exception handling
- Input validation
- Consistent error format

---

# 💳 14. Payment Safety

## Idempotency:
- Prevent duplicate payments

## Atomicity:
- All-or-nothing transactions

---

# 🔔 15. Notification System

## Channels:
- In-app
- Email
- SMS

## Architecture:
- Queue-based
- Retry mechanism

---

# 📁 16. File Storage

## Used For:
- QR codes
- Reports
- Prescriptions

## Options:
- Local (dev)
- AWS S3 (prod)

---

# 🔒 17. Security System

## Features:
- JWT authentication
- BCrypt password hashing
- Bank data encryption
- Aadhaar (last 4 digits only)

## Advanced:
- Rate limiting
- Account lock
- Access logs

---

# ⚡ 18. Performance Optimization

- Indexed DB queries
- Pagination
- Redis caching (future)

---

# 🌱 19. Environment Configuration

## Backend `.env`
DB_URL=  
DB_USER=  
DB_PASS=  
JWT_SECRET=  
EMAIL_KEY=  
SMS_KEY=  

## Frontend `.env`
VITE_API_BASE_URL=  
VITE_PUBLIC_KEY=  

## Shared Template:
.env.example  

---

# 🐳 20. Docker Compose Setup

1. Copy `.env.example` to `.env`
2. Run:
   ```bash
   docker compose up --build
   ```
3. Access:
   - Frontend: `http://localhost:3000`
   - Backend: `http://localhost:8080`

---

# 🧪 21. Testing Strategy

## Required:
- Unit testing
- Integration testing

## Critical Tests:
- Commission system
- Wallet transactions
- Inventory FIFO

---

# 📊 22. Reporting & Analytics

- Revenue dashboards
- Patient analytics
- Commission reports
- Inventory reports

---

# 🚀 23. Development Phases

1. Auth & Users  
2. Geography & Centers  
3. Family & Wallet  
4. OPD  
5. Inventory  
6. Billing + Commission  
7. IPD + HR  
8. Reports + Notifications  

---

# ⚠️ 24. Critical Rules

## Financial:
- No deletion of transactions
- Only reversal entries

## Medical:
- Immutable patient history

## Security:
- All actions logged
- Permission enforced

---

# 🧠 Final Note

This system is:

- NOT a simple CRUD app  
- NOT just a college project  

It is:

👉 Enterprise-grade Healthcare ERP + Fintech System
