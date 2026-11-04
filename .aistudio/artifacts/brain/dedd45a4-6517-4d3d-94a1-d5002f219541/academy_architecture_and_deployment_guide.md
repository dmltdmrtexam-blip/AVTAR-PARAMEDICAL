# AVTAR PARAMEDICAL ACADEMY
### Enterprise Native Android Application Architecture & Deployment Guide
**Tagline:** *Learn Better. Practice Smarter. Achieve Success.*

---

## 1. System Architecture Overview

The **Avtar Paramedical Academy** application is engineered with modern Android architecture following **Clean Architecture** principles and **MVVM (Model-View-ViewModel)** with Jetpack Compose.

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│   Jetpack Compose UI • Material 3 • Navigation Compose │
│   [Auth, Dashboard, Courses, CBT Engine, Analytics,    │
│    Secure PDF Viewer, Video Player, Admin Portal]     │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                    ViewModel Layer                     │
│   AcademyViewModel • Coroutines • StateFlow UI State   │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                    Repository Layer                    │
│   AcademyRepository • PdfExtractorService (OCR/Regex)  │
│   RazorpayPaymentService (Cryptographic HMAC-SHA256)   │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                       Data Layer                       │
│  - Supabase PostgreSQL Cloud Backend (28 Tables + RLS) │
│  - Offline Room / In-Memory State Cache                │
│  - Secure SharedPreferences / Encrypted DataStore      │
└────────────────────────────────────────────────────────┘
```

---

## 2. Relational Database Schema (28 Tables)

The complete PostgreSQL migration script is bundled in `app/src/main/assets/supabase_schema.sql`.

### Core Tables Summary:
1. `profiles` - Base user identities, avatars, device IDs, multi-device restriction.
2. `students` - Paramedical stream (DMLT, DRT, BMLT, OT, ECG), roll numbers, study streaks.
3. `admins` - Super admin, exam controllers, course managers with role checks.
4. `roles` - Role-based permissions matrix.
5. `course_categories` - Stream codes and categories (DMLT, DRT Radiology, BMLT, OT Tech, ECG).
6. `courses` - Titles, pricing, discount, instructor, validity, published states.
7. `course_materials` - High-yield study PDFs, page counts, preview flags.
8. `video_lessons` - Structured lecture videos, durations, playback orders.
9. `enrollments` - Student-to-course active subscriptions with expiry timestamps.
10. `payment_orders` - Razorpay order creation (`order_xxx`).
11. `payments` - Transaction IDs (`pay_xxx` / UTR), payment methods, cryptographic signatures, admin approvals.
12. `question_banks` - Categorized question banks per subject.
13. `questions` - Clinical question statements, difficulty levels, approval flags.
14. `options` - 4 options (A, B, C, D) per question with correct keys.
15. `mock_tests` - CBT test settings (durations, negative marks, passing marks, proctoring mode).
16. `test_questions` - Order and mapping of questions to tests.
17. `test_attempts` - Candidate exam sessions, timings, tab-switch counts.
18. `student_answers` - Auto-saved student responses, review flags, time spent per item.
19. `test_results` - Final calculated scores, percentages, ranks, accuracy.
20. `announcements` - Academy notices and urgent alerts.
21. `notifications` - Push and system alerts (course, test, payment).
22. `bookmarks` - Student saved questions for quick revision.
23. `certificates` - Merit and course completion digital certificates.
24. `referrals` - Student referral bonus tracking.
25. `proctoring_sessions` - Live proctoring status (camera authorization, GPS latitude/longitude).
26. `proctoring_events` - Full-screen exit logs, app background alerts.
27. `consent_records` - Explicit candidate camera and location consent timestamps.
28. `admin_audit_logs` - Immutable record of admin and director operations.

---

## 3. Screen Inventory

| Screen | Key Features |
|---|---|
| **Auth Screen** | Mobile OTP login, Student Registration, Academy Gold/Navy Branding, Single-session device security |
| **Student Dashboard** | Profile header, Streak counter, Paramedical Hero Graphic, KPI cards, Daily Clinical MCQ, Active CBT Mock Tests carousel, Announcements |
| **Courses Screen** | Filter pills by Stream (DMLT, DRT, BMLT, OT, ECG), pricing badges, enrolled status |
| **Course Detail Screen** | PDF notes list, Video lectures list, Razorpay UPI/Card checkout modal, Offline bank UTR transfer flow |
| **Secure PDF Viewer** | Zoom controls, page navigation, anti-piracy student watermark overlay (`Name + Mobile + Roll`) |
| **Video Player Screen** | 10s forward/rewind, seek bar, playback speed selector (0.75x to 2x), lecture notes |
| **CBT Online Exam Engine** | Countdown timer with color warning, Question palette with Green/Gray/Purple status indicators, Save & Next, Mark for Review, Clear Response, Live proctoring indicator, Background exit detection, Auto-submit |
| **Test Result Screen** | Scorecard, Percentage, Qualified/Failed badge, Accuracy %, Detailed question-by-question medical explanations |
| **Analytics Screen** | Subject-wise accuracy breakdown, Strong/Weak area recommendations, State Merit Leaderboard |
| **Admin Portal** | Course manager, PDF MCQ OCR extraction engine with duplicate detection, Offline UTR approvals, Registered student block/unblock, Live exam proctoring monitor, Audit logs |
| **Config & Credentials** | Supabase Project URL & Anon Key, Razorpay Key ID, FCM configuration, Live ping test |

---

## 4. Payment Gateway Verification (Razorpay)

Course access is **never granted purely from client signals**.
1. Order generated on server / service (`order_xxx`).
2. Signature verified using HMAC-SHA256:
   $$\text{Signature} = \text{HMAC-SHA256}(\text{order\_id} \parallel "|" \parallel \text{payment\_id}, \text{secret})$$
3. Offline transfers (NEFT/IMPS/UPI) are queued in `PENDING_VERIFICATION` until approved by the Academy Director in the Admin Portal.
