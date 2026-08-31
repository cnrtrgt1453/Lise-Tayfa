# Graph Report - Lise-Tayfa  (2026-08-31)

## Corpus Check
- 70 files · ~266,745 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 382 nodes · 656 edges · 39 communities (17 shown, 18 thin omitted)
- Extraction: 96% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 21 edges (avg confidence: 0.87)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Chat & Messaging
- Product Plan & Privacy Docs
- Question Posting & Navigation
- Auth UI & Google Sign-In
- Notifications
- Feed & Exam Subjects
- Questions & Solutions Repository
- User & Auth Repository
- Play Store Release & Security Checklist
- Navigation Routes
- Image Compression
- Google OAuth & SHA Setup
- Emoji Picker
- App Entry & Theme
- Badge System
- Exam Selection Dialog
- Gradle Wrapper Script
- Brand Identity & Icons
- DirectMessage Route
- App Icon (hdpi)
- App Icon (xxhdpi)
- App Logo Icon
- Round Icon (hdpi)
- Launcher Icon (mdpi)
- Round Icon (mdpi)
- Launcher Icon (xhdpi)
- Round Icon (xhdpi)
- App Icon (xxhdpi alt)
- Launcher Icon (xxxhdpi)
- Round Icon (xxxhdpi)
- cotx App Package
- Play Store Feature Graphic
- cotx App Package
- cotx App Package
- cotx App Package

## God Nodes (most connected - your core abstractions)
1. `AuthRepository` - 25 edges
2. `CotxNavGraph()` - 21 edges
3. `AuthViewModel` - 21 edges
4. `ChatViewModel` - 20 edges
5. `FeedViewModel` - 20 edges
6. `QuestionRepository` - 19 edges
7. `ChatRepository` - 17 edges
8. `UserSummary` - 17 edges
9. `User` - 16 edges
10. `Question` - 11 edges

## Surprising Connections (you probably didn't know these)
- `72 Saatlik Otomatik Görsel Silinme Sistemi` --semantically_similar_to--> `3 Günlük Dinamik Soru Yapısı (Story/Ephemeral System)`  [INFERRED] [semantically similar]
  PRIVACY_POLICY.md → PROJE_NOTLARI.md
- `72 Saatlik Otomatik Görsel Silinme Sistemi` --semantically_similar_to--> `expiresAt Alanı (3 Günlük Silinme)`  [INFERRED] [semantically similar]
  PRIVACY_POLICY.md → PROJE_TEKNIK_PLAN_VE_NOTLAR.md
- `3 Günlük Dinamik Soru Yapısı (Story/Ephemeral System)` --semantically_similar_to--> `expiresAt Alanı (3 Günlük Silinme)`  [INFERRED] [semantically similar]
  PROJE_NOTLARI.md → PROJE_TEKNIK_PLAN_VE_NOTLAR.md
- `Android Teknoloji Yığını (Kotlin 2.0 + Compose + MVVM)` --semantically_similar_to--> `Teknoloji Yığını Kararı (Kotlin+Compose+Firebase+Python)`  [INFERRED] [semantically similar]
  android/README.md → PROJE_TEKNIK_PLAN_VE_NOTLAR.md
- `Proje Klasör Yapısı` --semantically_similar_to--> `Clean Architecture / MVVM Klasör Yapısı`  [INFERRED] [semantically similar]
  android/README.md → PROJE_TEKNIK_PLAN_VE_NOTLAR.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Play Store Yayın Öncesi Hazırlık Akışı** — play_store_yayinlama_rehberi_doc, siber_guvenlik_tasklist_doc, google_signin_developer_error_10_cozum_rehberi_doc, privacy_policy_doc [INFERRED 0.85]
- **3 Günlük / 72 Saatlik Otomatik Silinme Sistemi (Çapraz Doküman)** — privacy_policy_ephemeral_72h_deletion, proje_notlari_ephemeral_3day_system, proje_teknik_plan_ve_notlar_ephemeral_expiresat_field, proje_teknik_plan_ve_notlar_storage_lifecycle_rule [INFERRED 0.90]
- **cotx Uygulaması Kimliği (com.cotx.app) - Tüm Dokümanlarda** — google_signin_developer_error_10_cozum_rehberi_cotx_app, play_store_yayinlama_rehberi_cotx_app, proje_teknik_plan_ve_notlar_cotx_app, siber_guvenlik_tasklist_cotx_app, android_readme_cotx_app [INFERRED 0.90]

## Communities (39 total, 18 thin omitted)

### Community 0 - "Chat & Messaging"
Cohesion: 0.08
Nodes (17): ChatConversation, ChatMessage, ChatRepository, ListenerRegistration, Result, formatHeaderDate(), MessageBubble(), ConversationItem() (+9 more)

### Community 1 - "Product Plan & Privacy Docs"
Cohesion: 0.05
Nodes (45): cotx Android README, Proje Klasör Yapısı, Projeyi Çalıştırma Adımları, Android Teknoloji Yığını (Kotlin 2.0 + Compose + MVVM), Veri Güvenliği (Data Safety) Formu, Hesap Silme Hakkı, Toplanan Veriler ve Kullanım Amaçları, İletişim (destek@cotxapp.com) (+37 more)

### Community 2 - "Question Posting & Navigation"
Cohesion: 0.12
Nodes (25): UserSummary, ReportDialog(), CotxNavGraph(), LoginScreen(), RegisterScreen(), DirectMessageScreen(), FeedScreen(), QuestionCard() (+17 more)

### Community 3 - "Auth UI & Google Sign-In"
Cohesion: 0.10
Nodes (17): AcademicBackground(), Color, Modifier, PeriodicElementChip(), GoogleCredentialAuth, GoogleSignInCancelled, GoogleSignInNoAccount, Context (+9 more)

### Community 4 - "Notifications"
Cohesion: 0.11
Nodes (19): Notification, Result, NotificationRepository, CotxBottomBar(), CotxBottomTab, EXPLORE, HOME, MESSAGES (+11 more)

### Community 5 - "Feed & Exam Subjects"
Cohesion: 0.11
Nodes (16): getSubjectColor(), Color, ExamSubjectHelper, Error, FeedTab, EXPLORE, FOLLOWING, FeedUiState (+8 more)

### Community 6 - "Questions & Solutions Repository"
Cohesion: 0.15
Nodes (7): Question, Solution, Context, Result, Uri, QuestionRepository, ProfanityFilter

### Community 7 - "User & Auth Repository"
Cohesion: 0.17
Nodes (5): User, AuthRepository, android, Result, Exception

### Community 8 - "Play Store Release & Security Checklist"
Cohesion: 0.12
Nodes (20): Google Sign-In Developer Error 10 Çözüm Rehberi, Google Play Services Yayılma Bekleme Süresi (15-30 dk), cotx Google Play Store Yayınlama Rehberi, Firebase SHA-1 Parmak İzi Entegrasyonu, Keystore Yapılandırması (key.properties), Gizlilik Politikası Bağlantısı Zorunluluğu, İmzalı Release AAB Derleme Süreci, Yayına Gönderim (Closed Testing / Production) (+12 more)

### Community 9 - "Navigation Routes"
Cohesion: 0.13
Nodes (11): AddQuestion, DirectMessage, EditProfile, Feed, Login, Messages, Notifications, Profile (+3 more)

### Community 10 - "Image Compression"
Cohesion: 0.32
Nodes (6): ImageCompressor, Context, Uri, Bitmap, BitmapFactory, ByteArray

### Community 11 - "Google OAuth & SHA Setup"
Cohesion: 0.22
Nodes (9): cotx App (com.cotx.app), Developer Error 10 (StatusCode 10), Internal App Sharing Sertifikası, Google Cloud Manual Android OAuth Client Oluşturma, OAuth Consent Screen Testing Durumu, Post-Quantum Cryptography SHA-1 Fingerprint, Play Store App Signing SHA-256 Sertifikası, Firebase Destek E-postası (Support Email) Kontrolü (+1 more)

### Community 12 - "Emoji Picker"
Cohesion: 0.29
Nodes (7): EmojiCategory, EMOTIONS, POPULAR, STUDY, SYMBOLS, EmojiPickerPanel(), Modifier

### Community 13 - "App Entry & Theme"
Cohesion: 0.43
Nodes (4): MainActivity, CotxTheme(), Bundle, ComponentActivity

### Community 15 - "Exam Selection Dialog"
Cohesion: 0.83
Nodes (3): ExamOptionCard(), ExamSelectionDialog(), Color

### Community 16 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 17 - "Brand Identity & Icons"
Cohesion: 0.67
Nodes (3): App Launcher Icon Foreground (CotxAcademy Logo), CotxAcademy Brand Identity, Education Subject Domains (Math, Reading, Geography/Language, Science)

## Ambiguous Edges - Review These
- `Google Sign-In Developer Error 10 Çözüm Rehberi` → `Google Play App Signing SHA-1 Parmak İzi Entegrasyonu`  [AMBIGUOUS]
  SIBER_GUVENLIK_TASKLIST.md · relation: references
- `Görsel Depolama Mimarisi (AWS S3/GCS/Cloudinary)` → `Firebase Entegrasyonu Kurulumu`  [AMBIGUOUS]
  PROJE_TEKNIK_PLAN_VE_NOTLAR.md · relation: conceptually_related_to

## Knowledge Gaps
- **87 isolated node(s):** `DirectMessage`, `EXPLORE`, `HOME`, `MESSAGES`, `NOTIFICATIONS` (+82 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 125 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **18 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Google Sign-In Developer Error 10 Çözüm Rehberi` and `Google Play App Signing SHA-1 Parmak İzi Entegrasyonu`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **What is the exact relationship between `Görsel Depolama Mimarisi (AWS S3/GCS/Cloudinary)` and `Firebase Entegrasyonu Kurulumu`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `Görsel Yüklemelerinde EXIF/GPS Veri Temizliği` connect `Product Plan & Privacy Docs` to `Play Store Release & Security Checklist`, `Image Compression`?**
  _High betweenness centrality (0.238) - this node is a cross-community bridge._
- **Why does `ImageCompressor` connect `Image Compression` to `Questions & Solutions Repository`?**
  _High betweenness centrality (0.173) - this node is a cross-community bridge._
- **What connects `DirectMessage`, `EXPLORE`, `HOME` to the rest of the system?**
  _87 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Chat & Messaging` be split into smaller, more focused modules?**
  _Cohesion score 0.07922705314009662 - nodes in this community are weakly interconnected._
- **Should `Product Plan & Privacy Docs` be split into smaller, more focused modules?**
  _Cohesion score 0.052525252525252523 - nodes in this community are weakly interconnected._