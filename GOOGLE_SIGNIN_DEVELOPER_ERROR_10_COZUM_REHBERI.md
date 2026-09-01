# 🛠️ Google Sign-In Developer Error 10 (StatusCode 10) Çözüm Rehberi

Bu doküman, **cotx** (`com.cotx.app`) uygulamasının Android Studio emülatöründe sorunsuz çalışıp, Google Play Store **İç Test (Internal Test)** ve **Kapalı Test (Closed Test)** sürümlerinde alınan **`Google ApiException (StatusCode: 10)`** hatasının kesin çözümlerini içerir.

---

## 🧭 TL;DR — Kök Neden

Play Store'a `.aab` yüklediğinizde uygulama **Play App Signing**'e dahil olur. Google, testçilere dağıtılan APK'yı **sizin upload (yükleme) anahtarınızla değil, kendi "uygulama imzalama anahtarı" (app signing key) ile yeniden imzalar.**

Google Sign-In (Credential Manager), Google OAuth sunucularına şu üçlüyü gönderir:

1. **Paket adı:** `com.cotx.app`
2. **Çalışan APK'yı imzalayan sertifikanın SHA-1'i** → İç/Kapalı testte bu **Play App Signing anahtarının SHA-1'idir**
3. **Web Client ID:** `901025625071-jnrlh6u7fammldvpu21db6cbnok6s6ar.apps.googleusercontent.com`

Firebase / Google Cloud OAuth istemcilerinde **bu SHA-1 kayıtlı değilse** → `StatusCode 10 (DEVELOPER_ERROR)`.

Emülatörde çalışmasının nedeni: emülatör **debug** APK'yı **debug keystore** ile imzalar ve o SHA-1 zaten `google-services.json` içinde kayıtlıdır. Ayrıca debug derlemesinde R8/minify kapalıdır.

> ⚠️ **`google-services.json` içindeki `certificate_hash` değerleri hatayı çözmez.** Bu dosya uygulamaya yalnızca `default_web_client_id` sağlar; SHA-1 doğrulaması Google'ın OAuth backend'inde, **projeye kayıtlı Android OAuth istemcileri listesine** göre yapılır. Yani asıl iş Firebase/GCP Console'da SHA eklemektir; sonra dosyayı yeniden indirmek sadece iyi bir pratiktir.

---

## ✅ Adım Adım Çözüm (sırayla yapın)

### 1️⃣ Çalışan APK'nın gerçek imzasını öğrenin (artık uygulama söylüyor)

Bu repoda `SignatureInfo` yardımcısı eklendi. İç/Kapalı test derlemesinde **Google ile Giriş** butonuna basıp hata alınca çıkan **🐞 DEBUG pop-up'ında** şu alan görünür:

```
Bu APK'nın İmzası (SHA-1 / SHA-256)
Package : com.cotx.app
SHA-1   : XX:XX:...
SHA-256 : XX:XX:...
```

**"Kopyala"** ile panoya alın. Play tarafından imzalanan bir derlemede bu değer **doğrudan Play App Signing parmak izidir** — Firebase'e eklemeniz gereken tam değer budur.

> Not: Release derlemesinde `proguard-rules.pro` tüm `android.util.Log` çağrılarını sildiği için `AuthViewModel` içindeki `Log.e(...)` satırları Logcat'e **hiçbir şey basmaz**. İç/Kapalı testte tek güvenilir teşhis kanalı bu ekran pop-up'ıdır.

### 2️⃣ Play Console'dan tüm sertifika parmak izlerini toplayın

**Play Console → Test ve yayınlama → Uygulama bütünlüğü (App integrity) → Play uygulama imzalama:**

| Sertifika | Kopyalanacaklar |
|---|---|
| **Uygulama imzalama anahtarı sertifikası** (App signing key) | SHA-1 **ve** SHA-256 |
| **Yükleme anahtarı sertifikası** (Upload key) | SHA-1 **ve** SHA-256 |

Adım 1'deki SHA-1 ile buradaki "Uygulama imzalama anahtarı" SHA-1'i **aynı olmalı**. Farklıysa cihaza mağaza/İç Test dışı bir yoldan (yan yükleme, farklı kanal) kurmuşsunuz demektir.

### 3️⃣ Bu parmak izlerini Firebase'e ekleyin

**Firebase Console → `cotx-c167c` → Proje Ayarları → Uygulamalarınız → `com.cotx.app` → "Parmak izi ekle":**

Şunların **hepsini** ayrı ayrı ekleyin:
- App signing key **SHA-1**
- App signing key **SHA-256**  ← yeni OAuth politikası bunu da şart koşuyor
- Upload key **SHA-1**
- Upload key **SHA-256**

### 4️⃣ Destek e-postasını kontrol edin

**Firebase Console → Proje Ayarları → Genel → Destek e-postası (Support email)** boş olmamalı. Boşsa bir adres seçip **Kaydet**. (Boş destek e-postası, release isteklerinde doğrudan `StatusCode 10` sebebidir.)

### 5️⃣ Güncel `google-services.json` dosyasını indirin

Firebase Console → Proje Ayarları → `com.cotx.app` → **google-services.json indir** → repodaki `android/app/google-services.json` ile değiştirin → commit'leyin.

### 6️⃣ Google Cloud tarafında OAuth istemcisini doğrulayın

**Google Cloud Console → APIs & Services → Credentials → OAuth 2.0 Client IDs:**

Her SHA-1 için `Android` tipinde, `com.cotx.app` paketli bir istemci olmalı. App signing key SHA-1'ine ait istemci **yoksa** manuel ekleyin:
- **+ CREATE CREDENTIALS → OAuth client ID**
- Application type: **Android**
- Name: `Play App Signing`
- Package name: `com.cotx.app`
- SHA-1: (Adım 2'deki app signing SHA-1)
- **CREATE**

### 7️⃣ OAuth Consent Screen "Testing" durumu (Kapalı test için kritik)

**Google Cloud Console → APIs & Services → OAuth consent screen:**

- **Publishing status = Testing** ise: yalnızca "Test users" listesindeki Gmail'ler giriş yapabilir; diğer herkes `StatusCode 10` / erişim engeli alır.
- Çözüm: ya tüm kapalı-test kullanıcılarının e-postalarını **Test users**'a ekleyin, ya da **PUBLISH APP** ile "In production"a geçin (yalnızca `email` + `profile` kapsamları için Google doğrulaması gerekmez).

### 8️⃣ Yayılmayı bekleyin ve önbelleği temizleyin

- SHA / e-posta değişiklikleri Google sunucularına **15–30 dakikada** yayılır.
- Test cihazında: **Ayarlar → Uygulamalar → Google Play Hizmetleri → Depolama → Önbelleği temizle.**
- Uygulamayı kapatıp yeniden açın ve tekrar deneyin.

---

## 🔎 Kod tarafında yapılan iyileştirmeler (bu repo)

| Dosya | Değişiklik |
|---|---|
| `util/SignatureInfo.kt` *(yeni)* | Çalışan APK'nın imza SHA-1 / SHA-256'sını runtime'da hesaplar. |
| `util/GoogleCredentialAuth.kt` | `NoCredentialException` alınca `GetSignInWithGoogleOption` (klasik buton akışı) ile otomatik ikinci deneme. Hata zincirinde `DEVELOPER_ERROR` / `code 10` görülürse, çalışan SHA-1'i gömülü içeren `GoogleSignInConfigError` fırlatır. |
| `util/DebugErrorDialog.kt` | DEBUG pop-up'ına "Bu APK'nın İmzası (SHA-1 / SHA-256)" alanı eklendi; "Kopyala" bu bilgiyi de kopyalar. |
| `ui/screens/auth/LoginScreen.kt` | `DebugErrorInfo.from(e, context)` — imza bilgisinin hesaplanabilmesi için context geçiliyor. |

Bu değişiklikler **sunucu tarafı yapılandırmayı** yerine getirmez; sadece hangi SHA-1'i eklemeniz gerektiğini net gösterir ve giriş akışını biraz daha dayanıklı yapar. Asıl düzeltme yukarıdaki 2–7. adımlardır.

---

## 🧪 Doğrulama

1. Yukarıdaki 2–7 adımları tamamlayın, 20 dk bekleyin, Play Hizmetleri önbelleğini temizleyin.
2. İç Test sürümünü mağazadan (veya İç Test bağlantısından) yeniden indirin.
3. "Google ile Giriş" → hesap seçin → uygulamaya giriş yapılmalı.
4. Hâlâ hata alıyorsanız: DEBUG pop-up'taki **SHA-1**'i, Firebase'e eklediğiniz app-signing SHA-1 ile **karakter karakter** karşılaştırın. Eşleşmiyorsa cihaza yanlış kanaldan kurulmuş demektir.
