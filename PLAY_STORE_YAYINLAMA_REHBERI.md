# 🚀 cotx - Google Play Store Yayınlama Rehberi

Bu doküman, **cotx** (`com.cotx.app`) uygulamasını Google Play Store'da canlıya almak için yapılması gereken tüm teknik ve idari adımları içerir.

---

## 📦 1. Imzalı Release AAB (Android App Bundle) Derleme

Google Play Store yeni uygulamalar için `.aab` formatını zorunlu kılar.

### Keystore Yapılandırması:
1. `android/key.properties.example` dosyasının bir kopyasını oluşturup `android/key.properties` olarak kaydedin.
2. İçerisine `cotx-release-key.jks` dosyanızın şifrelerini ve alias bilgisini yazın:
   ```properties
   storePassword=SERVISE_OZEL_PAROLANIZ
   keyPassword=SERVISE_OZEL_PAROLANIZ
   keyAlias=cotx-release-key
   storeFile=../cotx-release-key.jkshttps://127.0.0.1:56943/static/artifacts/0b16c284-fa6b-457b-bfe4-36400fbca23c/.user_uploaded/media_1787728371503.png?csrf=34aea053-75eb-40bc-b414-602ebab4e81d
   ```
3. AAB paketini üretmek için terminalde şu komutu çalıştırın:
   ```bash
   cd android
   ./gradlew bundleRelease
   ```
4. Çıktı Dosyası:  
   `android/app/build/outputs/bundle/release/app-release.aab`

---

## 📝 2. Google Play Console Mağaza Bilgileri Taslağı

### Uygulama Detayları:
* **Uygulama Adı:** `cotx - YKS & LGS Soru Yardım` (Max 30 karakter)
* **Kısa Açıklama:** `Dikkat dağıtıcı unsurlardan uzak, lise öğrencilerine özel ders ve soru paylaşım platformu.` (Max 80 karakter)
* **Tam Açıklama:**  
  ```text
  cotx; YKS (TYT, AYT), LGS ve lise müfredatına hazırlanan öğrenciler için geliştirilmiş niş bir sosyal eğitim platformudur.

  ✨ Öne Çıkan Özellikler:
  • Soru Paylaşımı: Çözemediğin soruların fotoğrafını çekip hemen paylaş, akranlarından yardım al.
  • 3 Günlük Dinamik Akış: Tüm soru ve çözümler 72 saat sonra otomatik olarak silinir. Güncel, temiz ve dinamik bir akış sunar.
  • Odaklanmış Alan: Sosyal medyanın dikkat dağıtıcı ögelerinden arındırılmış, yalnızca ders odaklı topluluk.
  • Birebir Mesajlaşma: Arkadaşlarınla soru ve çözüm görselleri üzerinden özel sohbet et.
  • Profil & Hedef Yönetimi: Alanını (Sayısal, Eşit Ağırlık, Sözel, Dil), hedef üniversitini ve sınıfını belirt.

  YKS ve LGS maratonunda yalnız değilsin. cotx ile sorularını paylaş, eksiklerini kapat!
  ```

### Görsel Materyal Ölçüleri:
* **Uygulama İkonu:** `512 x 512 px` (PNG veya JPEG, max 1 MB)
* **Öne Çıkarılan Görsel (Feature Graphic):** `1024 x 500 px` (PNG veya JPEG, max 1 MB)
* **Telefon Ekran Görüntüleri:** En az 2 adet, 16:9 veya 9:16 oranında (Örn: 1080x1920 px).

---

## 🛡️ 3. App Content (Uygulama İçeriği) Anketleri & Beyanlar

### A. Gizlilik Politikası (Privacy Policy)
* Play Console > **Gizlilik Politikası** alanına `PRIVACY_POLICY.md` dosyanızın canlı web bağlantısını ekleyin.

### B. Veri Güvenliği (Data Safety) Formu Cevapları:
* **Veri Toplanıyor mu?** Evet.
* **Toplanan Veri Türleri:**
  1. **Kişisel Bilgiler:** Ad-Soyad, E-posta adresi (Hesap yönetimi için).
  2. **Fotoğraflar ve Videolar:** Yüklenen soru ve çözüm görselleri (Uygulama işlevselliği için).
  3. **Uygulama Bilgileri ve Performans:** Hata günlükleri ve anonim analizler (Firebase Analytics).
* **Veri Aktarımı Şifreli mi?** Evet, tüm veriler transit halindeyken HTTPS/TLS ile şifrelenir.
* **Kullanıcı Veri Silme Talebinde Bulunabilir mi?** Evet, uygulama içi ayarlardan hesap silme imkanı mevcuttur. Sorular 72 saat sonra otomatik olarak tamamen silinmektedir.

### C. Hedef Kitle & İçerik Derecelendirmesi:
* **Hedef Kitle:** 13 yaş ve üzeri (Lise öğrencileri).
* **UGC (Kullanıcı Tarafından Üretilen İçerik):** Evet (Uygulamamızda Şikayet Et ve Engelle butonları mevcuttur).

---

## 🔑 4. Firebase SHA-1 Parmak İzi Entegrasyonu (Canlı Sürüm İçin)

1. AAB dosyasını Play Console'a ilk kez yükledikten sonra **Kurulum > Uygulama İmzası (App Signing)** sayfasına gidin.
2. Buradaki **SHA-1 parmak izini** kopyalayın.
3. [Firebase Console](https://console.firebase.google.com/) > Proje Ayarları > **Android Uygulamaları** bölümüne gelin.
4. Kopyaladığınız SHA-1 parmak izini buraya ekleyin. (Bu adım canlıda Firebase Auth ve Google ile Giriş servislerinin sorunsuz çalışması için zorunludur).

---

## ✅ 5. Yayına Gönderim
Tüm adımlar tamamlandıktan sonra uygulamayı **Kapalı Test (Closed Testing)** kulvarına veya doğrudan **Üretim (Production)** kulvarına göndererek Google incelemesine sunabilirsiniz. (İlk incelemeler genellikle 24-72 saat sürmektedir).
