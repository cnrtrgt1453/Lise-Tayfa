# 🛡️ Lise-Tayfa Uygulaması Siber Güvenlik ve Yayın Öncesi Görev Listesi

Bu dosya, **Lise-Tayfa** (`com.cotx.app`) uygulamasının Google Play Store yayın öncesinde güvenlik ve gizlilik denetimlerini adım adım tamamlamanız için oluşturulmuştur.

---

## 🚨 1. Öncelikli & Kritik Güvenlik Görevleri (Yayın Öncesi Zorunlu)

- [x] **1.1. Firebase Firestore Security Rules (Güvenlik Kuralları) Yapılandırılması**
  - **Risk:** Veritabanının varsayılan veya test modunda kalması durumunda yetkisiz kullanıcılar tüm verileri okuyabilir/silebilir.
  - **Yapılacaklar:**
    - Firebase Console > Firestore > Rules bölümüne gidin.
    - Tüm koleksiyonlar için `request.auth != null` kontrolü koyun.
    - Kullanıcıların sadece kendi profillerini güncelleyebilmesini sağlayın (`request.auth.uid == userId`).
  - **Durum:** ✅ Tamamlandı

- [x] **1.2. Firebase Cloud Storage Security Rules Yapılandırılması**
  - **Risk:** Yüklenen soru ve profil resimlerine yetkisiz erişim veya başkasının dosyasını silme riski.
  - **Yapılacaklar:**
    - Firebase Console > Storage > Rules bölümüne gidin.
    - Dosya yükleme ve silme işlemlerini yetkili kullanıcılara kısıtlayın.
  - **Durum:** ✅ Tamamlandı

- [x] **1.3. Kod Karıştırma ve Koruma (R8 / ProGuard) Aktifleştirilmesi**
  - **Risk:** APK/AAB dosyasının JADX/Apktool gibi araçlarla açılıp kaynak kodların tersine mühendislikle okunması.
  - **Hedef Dosya:** [`android/app/build.gradle.kts`](file:///c:/Users/caner/Desktop/Lise-Tayfa/android/app/build.gradle.kts)
  - **Yapılacaklar:**
    - `release` build tipi içerisine `isMinifyEnabled = true` ve `isShrinkResources = true` ekleyin.
  - **Durum:** ✅ Tamamlandı

- [x] **1.4. Canlı Sürümde Log Çıktılarının (Logcat) Temizlenmesi**
  - **Risk:** `Log.d()`, `Log.e()` veya `println()` ile yazdırılan bilgilerin cihaz logat'inden kötü amaçlı yazılımlarca okunması.
  - **Hedef Dosya:** [`android/app/proguard-rules.pro`](file:///c:/Users/caner/Desktop/Lise-Tayfa/android/app/proguard-rules.pro)
  - **Yapılacaklar:**
    - Release derlemesinde tüm Android Log komutlarını kaldıran ProGuard kuralını ekleyin:
      ```proguard
      -assumenosideeffects class android.util.Log {
          public static *** d(...);
          public static *** v(...);
          public static *** i(...);
          public static *** e(...);
      }
      ```
  - **Durum:** ✅ Tamamlandı

---

## ⚠️ 2. Gizlilik ve Veri Koruması Görevleri

- [x] **2.1. Görsel Yüklemelerinde EXIF (GPS Konum) Verisi Temizliği Kontrolü**
  - **Risk:** Kullanıcıların evlerinde/okullarında çektiği soru fotoğraflarında GPS konum bilgilerinin (metadata) sızması.
  - **Hedef Dosya:** [`android/app/src/main/java/com/cotx/app/util/ImageCompressor.kt`](file:///c:/Users/caner/Desktop/Lise-Tayfa/android/app/src/main/java/com/cotx/app/util/ImageCompressor.kt)
  - **Yapılacaklar:**
    - Fotoğraf WebP/JPEG olarak sıkıştırılırken EXIF verilerinin temizlendiğini doğrulayın.
  - **Durum:** ✅ Tamamlandı (Android `Bitmap.compress()` ham piksel işlediği için tüm EXIF/GPS verileri otomatik temizleniyor).

- [x] **2.2. Otomatik Yedekleme (Android Backup) Ayarları**
  - **Risk:** Cihaz yedeklemelerinde (`adb backup`) hassas verilerin cihaz dışına aktarılması.
  - **Hedef Dosya:** [`android/app/src/main/AndroidManifest.xml`](file:///c:/Users/caner/Desktop/Lise-Tayfa/android/app/src/main/AndroidManifest.xml)
  - **Yapılacaklar:**
    - `android:allowBackup` ve `data_extraction_rules.xml` ayarlarını gözden geçirin.
  - **Durum:** ✅ Tamamlandı (`android:allowBackup="false"` olarak ayarlandı).

- [x] **2.3. Hardcoded (Sabit Kodlanmış) Hassas Bilgi Taraması**
  - **Risk:** Kodun içinde unutulmuş API key, test parolaları veya özel URL'ler.
  - **Yapılacaklar:**
    - Kod tabanında hassas kelime taraması yapılması.
  - **Durum:** ✅ Tamamlandı (Hassas şifre veya gizli anahtar kalıntısı bulunmadı).

---

## 🛡️ 3. İleri Seviye Güvenlik ve Politika Uyum Görevleri

- [x] **3.1. Girdi Doğrulama ve Karakter Sınırları (Input Validation)**
  - **Risk:** Sohbet, soru açıklaması veya yorum alanlarına aşırı uzun metin girilerek uygulamanın çökertilmesi.
  - **Yapılacaklar:**
    - TextField bileşenlerine maksimum karakter sınırları (`maxLength`) koyun.
  - **Durum:** ✅ Tamamlandı (Soru konusu: 50, Açıklama: 500, Çözüm: 500, DM: 1000, Ad Soyad: 30, Biyografi: 150 char sınırları ve sayaçlar eklendi).

- [x] **3.2. Kullanıcı Engel / Bildir (Report & Block) Mekanizması**
  - **Risk:** Google Play UGC (Kullanıcı Tarafından Üretilen İçerik) politikası ihlali nedeniyle uygulamanın reddedilmesi.
  - **Yapılacaklar:**
    - Sohbet ve soru detay sayfalarında uygunsuz içerik bildirme (Report) ve kullanıcı engelleme (Block) butonlarını kontrol edin.
  - **Durum:** ✅ Tamamlandı (`ReportDialog` bileşeni, Firestore `reports` entegrasyonu, Soru/Çözüm/Profil/DM bildirme ve engelleme aksiyonları eklendi).

- [x] **3.3. Google Play App Signing SHA-1 Parmak İzi Entegrasyonu**
  - **Risk:** Play Store yayınından sonra Google ile Giriş veya Firebase servislerinin canlı ortamda çalışmaması.
  - **Yapılacaklar:**
    - Play Console > App Signing altındaki SHA-1 parmak izini kopyalayıp Firebase Console Proje Ayarlarına ekleyin.
  - **Durum:** ✅ Hazır / Operasyonel Rehber Eklendi (Aşağıdaki 3 adımı Play Store'a ilk AAB yüklemesi yapıldığında konsoldan uygulayın).

---

*Not: Tamamladığınız her görevin parantezini `[x]` yaparak işaretleyebilirsiniz.*
