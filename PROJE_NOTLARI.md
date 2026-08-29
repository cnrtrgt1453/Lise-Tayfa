# 📚 cotx - Proje Planı ve Notlar

## 🎯 Proje Vizyonu & Hedef Kitle
- **Hedef Kitle:** Lise öğrencileri ve YKS (TYT/AYT), LGS gibi sınavlara hazırlanan öğrenciler.
- **Ana Konsept:** Genel sosyal medyanın (Instagram vb.) dikkat dağıtıcı ögelerinden arındırılmış, **yalnızca ders, soru paylaşımı ve akran yardımlaşması** odaklı niş sosyal medya platformu.

---

## 📌 Temel Özellikler (Core Features)

1. **Kullanıcı & Profil Yönetimi**
   - Kayıt / Giriş sistemi.
   - Profil oluşturma: Profil fotoğrafı yükleme/değiştirme, Biyografi.
   - **Öğrenciye Özel Bilgiler:** Alan (Sayısal, Eşit Ağırlık, Sözel, Dil), Hedef Üniversite/Bölüm, Sınıf seviyesi (9-12, Mezun).
   - Hesap silme ve profil düzenleme imkanı.

2. **Soru Paylaşımı ve Akış (Feed)**
   - **Görsel Soru Paylaşımı:** Çözülemeyen veya ilginç bulunan soru stillerinin fotoğraf/görsel olarak paylaşılması.
   - **Kategori & Etiketler:** Ders (Matematik, Fizik, Türkçe vb.) ve konu seçimi.
   - **Soru Modu:** "Yardım İstiyorum" veya "Bu Soru Tipine Bakın".
   - **Yorumlar & Çözümler:** Diğer öğrencilerin yorum atarak çözüm görselleri veya açıklamaları paylaşabilmesi.

3. **Sosyal Etkileşim**
   - Arkadaşlık isteği gönderme / kabul etme / reddetme.
   - Profil inceleme.
   - Soru beğenme, yorum yapma ve soruları daha sonra çözmek üzere kaydetme.

4. **Birebir Mesajlaşma (Direct Messaging)**
   - Arkadaş olan öğrenciler arasında özel sohbet (DM).
   - Sohbet içerisinde soru görselleri paylaşabilme.

5. **⏳ 3 Günlük Dinamik Soru Yapısı (Story / Ephemeral System)**
   - Paylaşılan tüm soru fotoğrafları ve sorular **tam 3 gün (72 saat)** sonra otomatik olarak silinir.
   - **Avantajlar:** Kullanıcılarda hızlı yanıt verme motivasyonu (FOMO), veritabanında gereksiz çöp veri birikmemesi ve sunucu/storage maliyetlerinin **her zaman sıfıra yakın (ücretsiz limitlerde)** kalması.

---

## 💡 İleride Eklenebilecek Değer Katan Özellikler
- **Doğru Çözüm İşareti (StackOverflow Mantığı):** Soru sahibinin gelen çözümler arasından en iyisini "Doğru Çözüm" seçip soruyu "Çözüldü" olarak kapatması.
- **Rozet & Gamification:** Çok soru çözen veya başkalarına yardım eden öğrencilere özel rozetler (Örn: "Matematik Canavarı", "Topluluk Lideri").
- **Ders & Konu Filtreleme:** Akışı istenen ders ve konulara göre filtreleme.

---

## 💰 Para Kazanma (Monetization) Stratejileri

1. **Yayıncı & Eğitim Kurumu İşbirlikleri (B2B):** Soru bankası yayıncılarının (3D, Palme, Bilgi Sarmal vb.) "Onaylı Yayıncı" hesabı açıp sponsorlu soru tipleri / denemeler paylaşması.
2. **YKS Koçluk / Derece Öğrencisi Pazaryeri:** Derece yapmış üniversite öğrencilerinin alt sınıflara ücretli soru çözümü veya koçluk sunması (Platform komisyonu %15-20).
3. **Freemium & VIP Üyelik ("cotx Pro"):** Günlük soru sorma/kaydetme limitlerini kaldırma, özel profiller, reklamsız deneyim.
4. **Hedeflenmiş Reklamlar (AdMob):** Eğitim, kitap ve üniversite odaklı yayınlar.

---

## 🏗️ Veri Tabanı & Görsel Depolama Mimarisi

- **Görsel Depolama:** Görseller veritabanına **kaydedilmez**. AWS S3, Google Cloud Storage veya Cloudinary üzerinde saklanır. Veritabanında sadece link tutulur.
- **İstemci Tarafı Sıkıştırma:** Görsel yüklenmeden önce mobil cihazda optimize edilir (Max 1080p, WebP formatı, ~200 KB).
- **CDN:** Cloudflare / CloudFront kullanılarak görseller kullanıcılara önbellekten yıldırım hızında sunulur.

---

## 🚀 Performans ve Ölçeklenebilirlik (1000+ Eşzamanlı Kullanıcı)

1. **Lazy Loading / Pagination:** Akış soruları 10'ar 10'ar parça parça yüklenir.
2. **Redis Caching:** Trend veriler ve profil bilgileri RAM önbelleğinde tutularak sunucu yanıt süreleri 2-5 milisaniyeye düşürülür.
3. **Mobile Image Caching:** Mobil uygulamada görseller cihaz hafızasına önbelleklenerek tekrar indirmelerin önüne geçilir.
4. **WebSockets:** Gerçek zamanlı mesajlaşma için WebSocket protokolu kullanılır.

---

*Not: Bu doküman sohbetimizin özetidir. 1-2 saat sonra kaldığımız yerden mobil teknoloji tercihleri (React Native / Flutter) ve uygulama geliştirme adımları ile devam edeceğiz.*
