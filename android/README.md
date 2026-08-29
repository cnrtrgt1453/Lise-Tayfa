# 📱 cotx - Android Native (Kotlin + Jetpack Compose)

**cotx**, YKS ve LGS öğrencileri için özel olarak tasarlanmış, dikkat dağıtmayan, soru paylaşımı ve akran yardımlaşması odaklı mobil uygulamadır.

---

## 🛠️ Teknoloji Yığını (Tech Stack)

- **Dil & UI:** Kotlin 2.0 + Jetpack Compose (Material3)
- **Mimari:** MVVM + Clean Architecture
- **State Management & Async:** StateFlow + Coroutines
- **Görsel İşleme:** İstemci tarafı WebP sıkıştırma (~150-200 KB) + Coil Compose Image Caching
- **Arka Yüz (Backend):** Google Firebase (Auth, Firestore, Cloud Storage)
- **Sunucu Tarafı Mantığı:** Firebase Cloud Functions (Python)

---

## 🚀 Projeyi Çalıştırma Adımları

1. **Android Studio'da Açın:**
   `android/` klasörünü Android Studio (Ladybug / Hedgehog veya daha güncel) ile açın.

2. **Firebase Kurulumu:**
   - [Firebase Console](https://console.firebase.google.com/) üzerinden `cotx-app` adlı yeni bir proje oluşturun.
   - Android uygulaması ekleyin (`package: com.cotx.app`).
   - İndirdiğiniz `google-services.json` dosyasını `android/app/` klasörüne yapıştırın.
   - Firebase Console'dan **Authentication** (Email/Password), **Firestore Database** ve **Cloud Storage** servislerini aktif edin.

3. **Cihazda / Emülatörde Çalıştırın:**
   - Android Studio üzerinden `app` konfigürasyonunu seçip `Run (Shift + F10)` butonuna basın.

---

## 📂 Proje Yapısı

```
android/app/src/main/java/com/cotx/app/
├── data/
│   ├── model/ (User, Question, Solution, DirectMessage)
│   └── repository/ (AuthRepository, QuestionRepository, ChatRepository)
├── ui/
│   ├── navigation/ (NavGraph, Screen routes)
│   ├── screens/ (LoginScreen, RegisterScreen, FeedScreen, AddQuestionScreen, QuestionDetailScreen, DirectMessageScreen, ProfileScreen)
│   └── theme/ (Color, Theme, Type)
├── util/ (ImageCompressor - WebP optimizer)
├── viewmodel/ (AuthViewModel, FeedViewModel, AddQuestionViewModel)
└── MainActivity.kt
```
