# Graph Report - Lise-Tayfa  (2026-08-31)

## Corpus Check
- 68 files · ~266,463 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 380 nodes · 658 edges · 40 communities (19 shown, 17 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 21 edges (avg confidence: 0.87)
- Token cost: 422,099 input · 0 output

## Community Hubs (Navigation)
- Project Notes & Monetization Plan
- Core UI Screens & Navigation
- Authentication Flow
- Feed & Subject Filtering
- Direct Messaging Repository
- Chat Overview & Conversations
- Question & Solution Repository
- Auth Repository (Data Layer)
- Security & Play Store Publishing
- Notifications
- Navigation Routes
- Image Compression
- Add Question Flow
- Google Sign-In Troubleshooting
- Emoji Picker
- App Entry & Theming
- Exam Selection Dialog
- Gradle Wrapper Script
- App Icon & Brand Identity
- Direct Message Model
- App Icon (hdpi)
- App Icon (xxhdpi round)
- App Logo Icon
- App Icon (hdpi round)
- App Icon (mdpi)
- App Icon (mdpi round)
- App Icon (xhdpi)
- App Icon (xhdpi round)
- App Icon (xxhdpi)
- App Icon (xxxhdpi)
- App Icon (xxxhdpi round)
- Android README Doc
- Play Store Feature Graphic
- Play Store Guide Doc
- Technical Plan Doc
- Security Tasklist Doc

## God Nodes (most connected - your core abstractions)
1. `User` - 37 edges
2. `AuthRepository` - 23 edges
3. `AuthViewModel` - 21 edges
4. `CotxNavGraph()` - 20 edges
5. `ChatViewModel` - 20 edges
6. `FeedViewModel` - 20 edges
7. `QuestionRepository` - 19 edges
8. `ChatRepository` - 17 edges
9. `Question` - 11 edges
10. `CotxBottomTab` - 11 edges

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
- **cotx Uygulaması Kimliği (com.cotx.app) - Tüm Dokümanlarda** — google_signin_developer_error_10_cozum_rehberi_cotx_app, play_store_yayinlama_rehberi_cotx_app, proje_teknik_plan_ve_notlar_cotx_app, siber_guvenlik_tasklist_cotx_app, android_readme_cotx_app [INFERRED 0.90]
- **3 Günlük / 72 Saatlik Otomatik Silinme Sistemi (Çapraz Doküman)** — privacy_policy_ephemeral_72h_deletion, proje_notlari_ephemeral_3day_system, proje_teknik_plan_ve_notlar_ephemeral_expiresat_field, proje_teknik_plan_ve_notlar_storage_lifecycle_rule [INFERRED 0.90]
- **Play Store Yayın Öncesi Hazırlık Akışı** — play_store_yayinlama_rehberi_doc, siber_guvenlik_tasklist_doc, google_signin_developer_error_10_cozum_rehberi_doc, privacy_policy_doc [INFERRED 0.85]

## Communities (40 total, 17 thin omitted)

### Community 0 - "Project Notes & Monetization Plan"
Cohesion: 0.05
Nodes (44): cotx Android README, Proje Klasör Yapısı, Projeyi Çalıştırma Adımları, Android Teknoloji Yığını (Kotlin 2.0 + Compose + MVVM), Hesap Silme Hakkı, Toplanan Veriler ve Kullanım Amaçları, İletişim (destek@cotxapp.com), Veri Güvenliği ve Haklar (+36 more)

### Community 1 - "Core UI Screens & Navigation"
Cohesion: 0.11
Nodes (26): User, CotxBottomBar(), CotxBottomTab, EXPLORE, HOME, MESSAGES, NOTIFICATIONS, PROFILE (+18 more)

### Community 2 - "Authentication Flow"
Cohesion: 0.10
Nodes (17): AcademicBackground(), Color, Modifier, PeriodicElementChip(), GoogleCredentialAuth, GoogleSignInCancelled, GoogleSignInNoAccount, Context (+9 more)

### Community 3 - "Feed & Subject Filtering"
Cohesion: 0.11
Nodes (16): getSubjectColor(), Color, ExamSubjectHelper, Error, FeedTab, EXPLORE, FOLLOWING, FeedUiState (+8 more)

### Community 4 - "Direct Messaging Repository"
Cohesion: 0.16
Nodes (7): ChatMessage, ChatRepository, ListenerRegistration, Result, DirectMessageScreen(), formatHeaderDate(), MessageBubble()

### Community 5 - "Chat Overview & Conversations"
Cohesion: 0.13
Nodes (11): ChatConversation, ConversationItem(), GlobalMessageItem(), MessagesMainScreen(), ChatViewModel, ListenerRegistration, StateFlow, ViewModel (+3 more)

### Community 6 - "Question & Solution Repository"
Cohesion: 0.16
Nodes (7): Question, Solution, Context, Result, Uri, QuestionRepository, ProfanityFilter

### Community 7 - "Auth Repository (Data Layer)"
Cohesion: 0.18
Nodes (4): AuthRepository, android, Result, Exception

### Community 8 - "Security & Play Store Publishing"
Cohesion: 0.11
Nodes (21): Google Sign-In Developer Error 10 Çözüm Rehberi, Google Play Services Yayılma Bekleme Süresi (15-30 dk), Veri Güvenliği (Data Safety) Formu, cotx Google Play Store Yayınlama Rehberi, Firebase SHA-1 Parmak İzi Entegrasyonu, Keystore Yapılandırması (key.properties), Gizlilik Politikası Bağlantısı Zorunluluğu, İmzalı Release AAB Derleme Süreci (+13 more)

### Community 9 - "Notifications"
Cohesion: 0.16
Nodes (10): Notification, Result, NotificationRepository, Error, StateFlow, ViewModel, Loading, NotificationUiState (+2 more)

### Community 10 - "Navigation Routes"
Cohesion: 0.13
Nodes (11): AddQuestion, DirectMessage, EditProfile, Feed, Login, Messages, Notifications, Profile (+3 more)

### Community 11 - "Image Compression"
Cohesion: 0.32
Nodes (6): ImageCompressor, Context, Uri, Bitmap, BitmapFactory, ByteArray

### Community 12 - "Add Question Flow"
Cohesion: 0.20
Nodes (9): AddQuestionUiState, Error, Idle, Context, StateFlow, Uri, ViewModel, Loading (+1 more)

### Community 13 - "Google Sign-In Troubleshooting"
Cohesion: 0.22
Nodes (9): cotx App (com.cotx.app), Developer Error 10 (StatusCode 10), Internal App Sharing Sertifikası, Google Cloud Manual Android OAuth Client Oluşturma, OAuth Consent Screen Testing Durumu, Post-Quantum Cryptography SHA-1 Fingerprint, Play Store App Signing SHA-256 Sertifikası, Firebase Destek E-postası (Support Email) Kontrolü (+1 more)

### Community 14 - "Emoji Picker"
Cohesion: 0.29
Nodes (7): EmojiCategory, EMOTIONS, POPULAR, STUDY, SYMBOLS, EmojiPickerPanel(), Modifier

### Community 15 - "App Entry & Theming"
Cohesion: 0.43
Nodes (4): MainActivity, CotxTheme(), Bundle, ComponentActivity

### Community 16 - "Exam Selection Dialog"
Cohesion: 0.83
Nodes (3): ExamOptionCard(), ExamSelectionDialog(), Color

### Community 17 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 18 - "App Icon & Brand Identity"
Cohesion: 0.67
Nodes (3): App Launcher Icon Foreground (CotxAcademy Logo), CotxAcademy Brand Identity, Education Subject Domains (Math, Reading, Geography/Language, Science)

## Ambiguous Edges - Review These
- `Google Sign-In Developer Error 10 Çözüm Rehberi` → `Google Play App Signing SHA-1 Parmak İzi Entegrasyonu`  [AMBIGUOUS]
  SIBER_GUVENLIK_TASKLIST.md · relation: references
- `Görsel Depolama Mimarisi (AWS S3/GCS/Cloudinary)` → `Firebase Entegrasyonu Kurulumu`  [AMBIGUOUS]
  PROJE_TEKNIK_PLAN_VE_NOTLAR.md · relation: conceptually_related_to

## Knowledge Gaps
- **87 isolated node(s):** `DirectMessage`, `EXPLORE`, `HOME`, `MESSAGES`, `NOTIFICATIONS` (+82 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 124 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **17 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Google Sign-In Developer Error 10 Çözüm Rehberi` and `Google Play App Signing SHA-1 Parmak İzi Entegrasyonu`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **What is the exact relationship between `Görsel Depolama Mimarisi (AWS S3/GCS/Cloudinary)` and `Firebase Entegrasyonu Kurulumu`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `User` connect `Core UI Screens & Navigation` to `Authentication Flow`, `Feed & Subject Filtering`, `Direct Messaging Repository`, `Chat Overview & Conversations`, `Auth Repository (Data Layer)`?**
  _High betweenness centrality (0.247) - this node is a cross-community bridge._
- **Why does `Görsel Yüklemelerinde EXIF/GPS Veri Temizliği` connect `Project Notes & Monetization Plan` to `Security & Play Store Publishing`, `Image Compression`?**
  _High betweenness centrality (0.239) - this node is a cross-community bridge._
- **What connects `DirectMessage`, `EXPLORE`, `HOME` to the rest of the system?**
  _87 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Project Notes & Monetization Plan` be split into smaller, more focused modules?**
  _Cohesion score 0.05391120507399577 - nodes in this community are weakly interconnected._
- **Should `Core UI Screens & Navigation` be split into smaller, more focused modules?**
  _Cohesion score 0.10631229235880399 - nodes in this community are weakly interconnected._