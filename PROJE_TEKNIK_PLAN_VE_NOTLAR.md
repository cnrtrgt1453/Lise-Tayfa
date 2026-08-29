# 📝 cotx - Teknik Yol Haritası ve Görüşme Notları

*Tarih: 20 Ağustos 2026*  
*Konu: Proje Uygulama Stratejisi, Platform Seçimi, Para Kazanma (Monetization), Kotlin + Jetpack Compose & Firebase Mimarisi*

---

## 📌 1. Projenin Hayata Geçirilmesi ve Mimari Kararlar

`PROJE_NOTLARI.md` dokümanındaki gereksinimler doğrultusunda kararlaştırılan teknoloji yığını (Tech Stack):

- **Ön Yüz (Frontend):** Android Native (**Kotlin + Jetpack Compose**) — Şimdilik sadece Android mağazası hedefleniyor.
- **Arka Yüz & Veritabanı (BaaS):** **Google Firebase** (Auth, Cloud Firestore NoSQL, Cloud Storage).
- **Sunucu Tarafı Özel Kodlar (Serverless Backend):** **Firebase Cloud Functions (Python)** — Gerekli özel iş mantıkları (ödeme kontrolü, bildirimler, AI analizleri vb.) için.
- **IDE & Ortam:** **Android Studio** (Kotlin + Jetpack Compose geliştirme ve Simülatör için).

---

## 🛠️ 2. Firebase Mimarisi ve Backend Dili Analizi

### Neden Ayrı Bir Backend Sunucusuna (Spring Boot vb.) Gerek Yok?
1. **Firebase Zaten Backend'in Kendisidir:** Kotlin mobil uygulaması doğrudan Firebase Android SDK'ları aracılığıyla veri okur, yazar ve görsel yükler.
2. **Spring Boot Neden Önerilmez?** 
   - Spring Boot ağır bir Java framework'üdür ve yüksek RAM (JVM) tüketir; bu da ücretsiz hosting imkanını ortadan kaldırır.
   - Firebase varken önüne Spring Boot koymak, araya gereksiz katman eklemek demektir.
3. **Kararlaştırılan Backend Dili:** Arka planda özel çalışması gereken bir sunucu kodu (Cloud Functions) gerektiğinde **Python** dili kullanılacaktır.

---

## 🚀 3. Geliştirme Yol Haritası ve İlk Adımlar

1. **Ortam Kurulumu:**
   - Android Studio ve JDK ortamının hazır hale getirilmesi.
2. **Proje Başlatma:**
   - Android Studio üzerinden Kotlin + Jetpack Compose projesinin oluşturulması.
3. **Firebase Entegrasyonu:**
   - Firebase Console üzerinde `cotx-app` projesinin açılması.
   - `google-services.json` dosyasının Android projesine eklenmesi.
   - Firebase Auth, Firestore ve Storage kütüphanelerinin Gradle'a eklenmesi.
4. **Klasör Yapısı & Mimarinin Kurulması (Clean Architecture / MVVM):**
   - `data/` (Models, Firebase Repositories)
   - `ui/` (Screens: Feed, QuestionDetail, NewQuestion, Profile, DM)
   - `viewmodel/` (State Management - Jetpack ViewModel)

---

## ✅ 4. Son Görüşme Notları ve Tamamlanan İşler (Özet)

* **Proje İsmi:** `cotx` (`com.cotx.app`) olarak güncellendi.
* **Teknoloji Yığını:** Android Native (Kotlin + Jetpack Compose) + Firebase (Auth, Firestore, Cloud Storage) + Python (Cloud Functions).
* **Görsel Optimize Etme:** İstemci tarafı WebP dönüştürücü (`ImageCompressor.kt`) yazıldı. Görseller ~150-200 KB'a düşürülerek Firebase Storage'a yükleniyor.
* **⏳ 3 Günlük Dinamik Soru Yapısı:** Sorulara `expiresAt` (3 gün sonrasının tarihi) alanı eklendi. Hem istemci tarafında süresi dolan sorular filtreleniyor hem de Cloud Storage Lifecycle kuralı ile 3 günü geçen görseller sunucudan kalıcı olarak imha ediliyor.
* **Firebase Kurulumu:** Firebase Console üzerinde `cotx` projesi açıldı, `com.cotx.app` bağlandı ve `google-services.json` dosyası `android/app/` dizinine eklendi.
* **Kodlanan Dosyalar:**
  - `android/settings.gradle.kts` & `android/app/build.gradle.kts`
  - `data/model/`: `User.kt`, `Question.kt` (`expiresAt` eklendi), `Solution.kt`, `DirectMessage.kt`
  - `data/repository/`: `AuthRepository.kt`, `QuestionRepository.kt` (3 günlük zaman filtreli), `ChatRepository.kt`
  - `util/`: `ImageCompressor.kt` (WebP Sıkıştırma)
  - `ui/theme/`: `Color.kt`, `Type.kt`, `Theme.kt` (cotx Mor/Turuncu Modern Dark/Light Tema)
  - `viewmodel/`: `AuthViewModel.kt`, `FeedViewModel.kt`, `AddQuestionViewModel.kt`
  - `ui/screens/`: `LoginScreen.kt`, `RegisterScreen.kt`, `FeedScreen.kt`, `AddQuestionScreen.kt`, `QuestionDetailScreen.kt`, `DirectMessageScreen.kt`, `ProfileScreen.kt`
  - `ui/navigation/`: `NavGraph.kt`, `Screen.kt`
  - `MainActivity.kt` & `AndroidManifest.xml`

---

## 🚀 5. 20 Ağustos 2026 Oturumu Tamamlananlar Özeti
1. **Firebase Yapılandırması Tamamlandı:**
   - Firebase Console'da `cotx` projesi açıldı ve `com.cotx.app` paket ismiyle Android uygulaması kaydedildi.
   - `google-services.json` konfigürasyon dosyası projenin `android/app/google-services.json` dizinine kopyalandı.
   - **Authentication:** Email/Password seçeneği etkinleştirildi.
   - **Firestore Database:** Standard Edition test modunda oluşturuldu.
   - **Cloud Storage:** Blaze planında aktifleştirildi ve 25 TL bütçe uyarısı (Budget Alert) konuldu.
2. **Mimari & Ürün Özelliği:**
   - **3 Günlük Dinamik Soru Modu (Story/Ephemeral):** Sorulara `expiresAt` (3 gün sonrası) eklendi. `QuestionRepository` içerisinde süresi geçen sorular otomatik filtreleniyor.
   - **Görsel Sıkıştırma (`ImageCompressor.kt`):** Soru fotoğrafları istemci tarafında ~150 KB WebP formatına dönüştürülüyor.
3. **Mevcut Durum:**
   - Android Studio üzerinden projeyi açıp emülatörde veya gerçek cihazda test etmeye %100 hazır hale getirildi.


