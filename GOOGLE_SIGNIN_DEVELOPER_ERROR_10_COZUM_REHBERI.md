# 🛠️ Google Sign-In Developer Error 10 (StatusCode 10) Çözüm Rehberi

Bu doküman, **cotx** (`com.cotx.app`) uygulamasının Android Studio emülatöründe sorunsuz çalışıp, Google Play Store **İç Test (Internal Test)** ve **Kapalı Test (Closed Test)** sürümlerinde alınan **`Google ApiException (StatusCode: 10)`** hatasının kesin çözümlerini içerir.

---

## 📌 StatusCode 10 (DEVELOPER_ERROR) Nedir?

Google Play Services dokümantasyonuna göre **Developer Error 10**, cihazdaki Google Play Services'ın şu 3 bilgiyi Google OAuth sunucularına gönderip eşleşen bir yetki bulamaması sonucu oluşur:

1. **Paket Adı (Package Name):** `com.cotx.app`
2. **Cihazda Çalışan APK'nın SHA-1 İmzası** (Play Store re-sign imzası)
3. **Web Client ID:** `901025625071-jnrlh6u7fammldvpu21db6cbnok6s6ar.apps.googleusercontent.com`

---

## 🔍 Hatanın Çözümü İçin 3 Kritik Kontrol Noktası

---

### 1️⃣ Firebase Console "Destek E-postası" (Support Email) Kontrolü *(EN SIK GÖZDEN KAÇAN NEDEN)*

Google OAuth politikaları gereği, Canlı / Release sürümlerinden gelen kimlik doğrulama isteklerinde projede tanımlı bir iletişim e-postası bulunması zorunludur.

* **Yapılacak İşlem:**
  1. [Firebase Console](https://console.firebase.google.com/)'a giriş yapın ➔ **cotx-c167c** projesini seçin.
  2. Proje Ayarları (Sol üstteki çark simgesi) ➔ **Genel (General)** sekmesine gelin.
  3. En üstte yer alan **Destek e-postası (Support email)** alanını kontrol edin.
  4. Eğer orada "Seçilmedi" veya boş görünüyorsa, e-posta adresinizi seçip **Kaydet** butonuna basın.

> [!WARNING]
> Destek e-postası boş bırakıldığında Google OAuth sunucuları canlı/release derlemelerinden gelen istekleri doğrudan `StatusCode: 10 (DEVELOPER_ERROR)` ile reddeder.

---

### 2️⃣ Google Cloud Credentials Tarafında Manuel Android İstemcisi Oluşturma

Firebase Console'a eklenen bazı yeni SHA-1 parmak izleri (özellikle Play Store'un yeni **Post-quantum cryptography key** SHA-1'i) Google Cloud Console tarafına otomatik olarak yansımayabilir.

* **Yapılacak İşlem:**
  1. [Google Cloud Console Credentials](https://console.cloud.google.com/apis/credentials) sayfasına gidin (Üstte `cotx-c167c` projesi seçili olsun).
  2. **OAuth 2.0 Client IDs** listesini kontrol edin.
  3. Listede Play Console'dan aldığınız **Post-Quantum SHA-1** (`ADC723A7FA5D69F736413EBEF7A880F20692DC62`) değerine sahip bir Android İstemcisi var mı kontrol edin.
  4. **Eğer görünmüyorsa manuel ekleyin:**
     - En üstten **`+ CREATE CREDENTIALS`** ➔ **`OAuth client ID`** butonuna tıklayın.
     - **Application type:** `Android` seçin.
     - **Name:** `Play App Signing Post-Quantum` yazın.
     - **Package name:** `com.cotx.app`
     - **SHA-1 certificate fingerprint:** `ADC723A7FA5D69F736413EBEF7A880F20692DC62` yapıştırın.
     - **CREATE** butonuna basarak kaydedin.

---

### 3️⃣ Dahili Uygulama Paylaşımı (Internal App Sharing) Sertifikası Kontrolü

Uygulamayı telefonunuza yüklerken mağaza sayfasından mı indirdiniz yoksa dahili indirme bağlantısı (Internal App Sharing linki) mı kullandınız?

* **Eğer Internal App Sharing bağlantısı kullandıysanız:**
  - Play Store bu bağlantı üzerinden indirilen APK'ları mağazadaki ana key ile değil, **Dahili Uygulama Paylaşımı Sertifikası** ile imzalar.
  - **Yapılacak İşlem:** Play Console ➔ **Kurulum (Setup) > Dahili Uygulama Paylaşımı (Internal App Sharing)** ➔ **Sertifikalar** sekmesindeki SHA-1 parmak izini kopyalayıp Firebase Console'a ekleyin.

### 4️⃣ Play Store App Signing SHA-256 Sertifikasını Ekleme
Google Play Services yeni OAuth politikalarında release derlemeler için SHA-1'in yanı sıra SHA-256 sertifikasının da kaydedilmiş olmasını şart koşar.

* **Yapılacak İşlem:**
  1. Google Play Console ➔ **Kurulum (Setup) > Uygulama Bütünlüğü (App Integrity)** sekmesine gidin.
  2. **Uygulama imzalama anahtarı sertifikası (App signing key certificate)** kısmındaki **SHA-256** değerini kopyalayın.
  3. Firebase Console ➔ Proje Ayarları ➔ `com.cotx.app` uygulamasına SHA-256 parmak izini ekleyin.

---

### 5️⃣ Google Cloud OAuth Consent Screen "Testing" Durum Kontrolü
Google Cloud Console'da OAuth Rıza Ekranı (OAuth Consent Screen) **"Testing"** modundaysa, test kullanıcılarının e-posta adresleri GCP üzerindeki Test Users listesinde değilse Google Sign-In `StatusCode 10` veya erişim engeli verir.

* **Yapılacak İşlem:**
  1. [Google Cloud Console OAuth Consent Screen](https://console.cloud.google.com/apis/credentials/consent) sayfasına gidin.
  2. **Publishing status** alanını kontrol edin.
  3. **`PUBLISH APP (UYGULAMAYI YAYINLA)`** butonuna basarak rıza ekranını "Yayında (In Production)" moduna geçirin.

---

## ⏰ Güncelleme Sonrası Bekleme Süresi

Firebase Console ve Google Cloud Credentials üzerinde yapılan SHA-1, SHA-256 ve Destek E-postası değişikliklerinin Google'ın tüm dünya üzerindeki Play Services sunucularına yayılması **15 ila 30 dakika** sürmektedir.

Değişiklikleri tamamladıktan **20 dakika sonra** cihazdaki **Google Play Hizmetleri önbelleğini temizleyip** İç/Kapalı test uygulamanızı test edebilirsiniz.
