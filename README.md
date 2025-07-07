# GreenSup 🌱

**GreenSup**, bitki severlerin günlük yaşamlarını kolaylaştırmak için geliştirilmiş modern bir Android uygulamasıdır. Yapay zeka destekli bitki tanıma, hastalık tespiti, hava durumu takibi, bahçe takibi ve bitki bakım tavsiyeleri gibi kapsamlı özellikler sunar.

## 📱 Özellikler

### 🔍 Bitki Araştırma, Tanıma ve Analiz
- **AI Destekli Bitki Tanıma**: Gemini AI ve Plant.ID API'leri kullanarak fotoğraftan bitki türü tanıma
- **Hastalık Tespiti**: Bitki hastalıklarının erken tespiti ve tedavi önerileri
- **Detaylı Bitki Bilgileri**: Bilimsel isim, bakım tavsiyeleri, sulama periyodu
- **Bitki araştırma**: Perenual API kullanılarak bir çok bitkinin taksonomik, ve yetiştirme bilgilerine ulaşma

### 🌤️ Hava Durumu Entegrasyonu
- **Anlık Hava Durumu**: OpenWeatherMap API ile güncel hava durumu bilgileri
- **7 Günlük Tahmin**: Haftalık hava durumu tahminleri
- **Lokasyon Bazlı Takip**: Farklı şehirler için hava durumu

### 🗨️ Sosyal Forum
- **Topluluk Paylaşımı**: Bitki severlerle deneyim paylaşımı
- **Soru-Cevap**: Uzmanlardan ve topluluktan yardım alma
- **Fotoğraf Paylaşımı**: Bitki fotoğrafları ile etkileşimli paylaşım

### 🌿 Kişisel Bahçe Yönetimi
- **Bahçe Takibi**: Sahip olunan bitkilerin listesi ve durumu
- **Sulama Hatırlatıcıları**: Otomatik bildirimlerle sulama zamanları
- **Favori Bitkiler**: Beğenilen bitkileri kaydetme

### 📰 Bitki Haberleri
- **Güncel Makaleler**: News API ile güncel bitki haberleri
- **Eğitici İçerik**: Bitki bakımı hakkında bilgilendirici yazılar

## 🚀 Kurulum

### Gereksinimler

- **Android Studio**: Arctic Fox (2020.3.1) veya daha yeni
- **JDK**: 17 veya üzeri
- **Android SDK**: API Level 27 (Android 8.1) minimum, API Level 34 (Android 14) hedef
- **Git**: Proje klonlama için

### Projeyi İndirme

```bash
git clone https://github.com/kullanici-adi/GreenSup.git
cd GreenSup
```

### Gradle Sync

```bash
./gradlew build
```

## 🔑 API Konfigürasyonu

Bu uygulama birden fazla harici API kullanmaktadır. Uygulamayı çalıştırmak için aşağıdaki API anahtarlarını edinmeniz gerekmektedir:

### 1. OpenWeatherMap API
**Amaç**: Hava durumu bilgileri

1. [OpenWeatherMap](https://openweathermap.org/api) sitesine gidin
2. Ücretsiz hesap oluşturun
3. API anahtarınızı alın
4. `app/src/main/java/com/gizemir/plantapp/core/util/Constants.kt` dosyasında `WEATHER_API_KEY` değerini güncelleyin

```kotlin
const val WEATHER_API_KEY = "BURAYA_API_ANAHTARINIZI_YAZIN"
```

### 2. Perenual API
**Amaç**: Bitki bilgileri ve arama

1. [Perenual](https://perenual.com/docs/api) sitesine gidin
2. API anahtarı için kayıt olun
3. `Constants.kt` dosyasında `PERENUAL_API_KEY` değerini güncelleyin

```kotlin
const val PERENUAL_API_KEY = "BURAYA_API_ANAHTARINIZI_YAZIN"
```

### 3. News API
**Amaç**: Bitki haberleri ve makaleler

1. [NewsAPI](https://newsapi.org/) sitesine gidin
2. Ücretsiz API anahtarı alın
3. `Constants.kt` dosyasında `NEWS_API_KEY` değerini güncelleyin

```kotlin
const val NEWS_API_KEY = "BURAYA_API_ANAHTARINIZI_YAZIN"
```

### 4. Plant.ID API
**Amaç**: Bitki hastalık tespiti

1. [Plant.ID](https://plant.id/) sitesine gidin
2. API anahtarı için kayıt olun
3. `Constants.kt` dosyasında `PLANT_ID_API_KEY` değerini güncelleyin

```kotlin
const val PLANT_ID_API_KEY = "BURAYA_API_ANAHTARINIZI_YAZIN"
```

### 5. Google Gemini AI API
**Amaç**: AI destekli bitki bakım tavsiyeleri

1. [Google AI Studio](https://makersuite.google.com/app/apikey) sitesine gidin
2. Google hesabınızla giriş yapın
3. API anahtarı oluşturun
4. `Constants.kt` dosyasında `GEMINI_API_KEY` değerini güncelleyin

```kotlin
const val GEMINI_API_KEY = "BURAYA_API_ANAHTARINIZI_YAZIN"
```

### 6. Firebase Konfigürasyonu

1. [Firebase Console](https://console.firebase.google.com/) açın
2. Yeni proje oluşturun veya mevcut projeyi seçin
3. Android uygulaması ekleyin
4. Package name: `com.gizemir.plantapp`
5. `google-services.json` dosyasını indirin
6. Dosyayı `app/` klasörüne kopyalayın

**Firebase'de etkinleştirmeniz gereken servisler:**
- Authentication (E-posta/Şifre)
- Firestore Database
- Cloud Storage
- Cloud Messaging (FCM)

### API Anahtarları Güvenliği

⚠️ **Önemli**: Üretim ortamında API anahtarlarını kaynak kodunda saklamayın. Bunun yerine:

1. **Gradle Properties**: `gradle.properties` dosyasında saklayın
2. **Environment Variables**: Sistem değişkenleri kullanın
3. **BuildConfig**: Build sırasında enjekte edin

Örnek `gradle.properties` kullanımı:

```properties
# gradle.properties
WEATHER_API_KEY="your_api_key_here"
PERENUAL_API_KEY="your_api_key_here"
```

Build.gradle.kts'da:

```kotlin
android {
    defaultConfig {
        buildConfigField("String", "WEATHER_API_KEY", "\"${project.findProperty("WEATHER_API_KEY")}\"")
    }
}
```

## 🏗️ Mimari

Uygulama **Clean Architecture** ve **MVVM** pattern'ları kullanılarak geliştirilmiştir.

### Katman Yapısı

```
app/
├── data/                 # Veri katmanı
│   ├── local/           # Room veritabanı
│   ├── remote/          # Retrofit API servisleri
│   └── repository/      # Repository implementasyonları
├── domain/              # İş mantığı katmanı
│   ├── model/          # Domain modelleri
│   ├── repository/     # Repository arayüzleri
│   └── use_case/       # Use case'ler
└── presentation/        # UI katmanı
    ├── ui/             # Jetpack Compose ekranları
    └── viewmodel/      # ViewModel'ler
```

### Kullanılan Teknolojiler

- **UI**: Jetpack Compose + Material3
- **Dependency Injection**: Dagger Hilt
- **Database**: Room
- **Network**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Authentication**: Firebase Auth
- **Storage**: Firebase Firestore & Storage
- **Notifications**: WorkManager + FCM
- **Architecture**: MVVM + Clean Architecture

## 📋 Gerekli İzinler

Uygulama aşağıdaki izinleri kullanır:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## 🔧 Geliştirme Ortamı Kurulumu

### 1. Android Studio Ayarları

```bash
# JDK 17 kurulumu (macOS)
brew install openjdk@17

# JDK 17 kurulumu (Windows)
# Oracle JDK 17'yi indirin ve kurun
```

### 2. Gradle Wrapper

```bash
# Gradle wrapper ile build
./gradlew assembleDebug

# Test çalıştırma
./gradlew test

# Lint kontrol
./gradlew lint
```

### 3. Debug Keystore

```bash
# Debug keystore oluşturma (gerekirse)
keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000
```

## 🚀 Çalıştırma

### Debug Build

1. Android Studio'da projeyi açın
2. `app/src/main/java/com/gizemir/plantapp/core/util/Constants.kt` dosyasındaki API anahtarlarını güncelleyin
3. Firebase `google-services.json` dosyasını `app/` klasörüne ekleyin
4. Gradle sync yapın
5. Emülatör veya fiziksel cihazda çalıştırın

### Command Line Build

```bash
# Debug APK oluşturma
./gradlew assembleDebug

# Release APK oluşturma (imzalı)
./gradlew assembleRelease
```

## 📱 Desteklenen Android Versiyonlar

- **Minimum SDK**: API Level 27 (Android 8.1 Oreo)
- **Target SDK**: API Level 34 (Android 14)
- **Compile SDK**: API Level 34

## 🧪 Test

```bash
# Unit testler
./gradlew test

# Instrumentation testler
./gradlew connectedAndroidTest

# Test coverage
./gradlew jacocoTestReport
```


## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için `LICENSE` dosyasına bakın.

## 🐛 Sorun Bildirimi

Herhangi bir sorunla karşılaştığınızda lütfen [Issues](https://github.com/kullanici-adi/GreenSup/issues) sayfasından bildiriniz.

## 📞 İletişim

- **Geliştirici**: Gizemir
- **E-posta**: gizemir17.10@gmail.com
- **Proje**: GreenSup Bitki Uygulaması

## 🔄 Sürüm Geçmişi

### v1.0.0 (Mevcut)
- İlk stabil sürüm
- Bitki tanıma ve hastalık tespiti
- Hava durumu entegrasyonu
- Sosyal forum özelliği
- Kişisel bahçe yönetimi

---

**GreenSup ile bitkinize en iyi bakımı sağlayın! 🌱** 
