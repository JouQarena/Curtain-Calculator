# 🪟 حاسبة الستائر | Curtain Calculator

[![Build APK](https://github.com/JouQarena/Curtain-Calculator/actions/workflows/build-apk.yml/badge.svg)](https://github.com/JouQarena/Curtain-Calculator/actions/workflows/build-apk.yml)
![Platform](https://img.shields.io/badge/platform-Android-green)
![Kotlin](https://img.shields.io/badge/kotlin-1.9-blue?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=android)
![Min SDK](https://img.shields.io/badge/minSdk-24-orange)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

A simple, offline-first Android app (in Arabic 🇪🇬) that helps curtain makers and homeowners take accurate curtain measurements — from window/rod sizing to calculating the ideal number of curtain rings and their spacing.

تطبيق أندرويد بسيط باللغة العربية يساعد في حساب مقاسات الستائر بدقة — من قياس الشباك والماسورة إلى حساب العدد المثالي للحلقات والمسافة بينها.

---

## ✨ Features | المميزات

### 1️⃣ Window Calculator | حاسبة قياس الشباك 🪟
Enter the **window width** and the **rod (ماسورة) width** to get the exact distance to leave on each side.

أدخل **عرض الشباك** و**عرض الماسورة** لتحصل على المسافة الصحيحة من كل جانب.

> **Formula | المعادلة:**
> ```
> المسافة من كل جانب = (عرض الشباك − (عرض الماسورة − 10)) ÷ 2
> ```

### 2️⃣ Rings Calculator | حاسبة عدد الحلقات ⭕
Enter the **curtain width** to get all possible combinations of ring counts with ideal spacing.

أدخل **عرض الستارة** لتحصل على كل الاحتمالات المناسبة لعدد الحلقات والمسافة بينها.

- Subtracts 10 cm allowance from the total width (effective width)
  - يخصم 10 سم من العرض الكلي (المقاس الفعلي)
- Tests all **odd** ring counts (3, 5, 7, 9, …)
  - يجرّب كل الأعداد **الفردية** للحلقات
- Keeps only results where spacing is between **15–18 cm**
  - يعرض فقط النتائج التي تقع فيها المسافة بين **15 و 18 سم**

> **Formula | المعادلة:**
> ```
> المقاس الفعلي = عرض الستارة − 10
> المسافة بين الحلقات = المقاس الفعلي ÷ عدد الحلقات (فردي)
> الشرط: 15 ≤ المسافة ≤ 18 سم
> ```

### General | عام
- 🌙 **Material 3 + Dynamic colors** — follows system light/dark theme (Android 12+)
- ↔️ **Full RTL Arabic UI** — واجهة عربية كاملة من اليمين لليسار
- 📴 **100% offline** — no internet, no permissions, no tracking
- ✅ Input validation with clear Arabic error messages

---

## 📱 Screenshots | صور التطبيق

> Replace these placeholders with real screenshots from your device.

| Window Calculator | Rings Calculator |
|---|---|
| <img src="screenshots/window.png" width="250" alt="Window calculator"> | <img src="screenshots/rings.png" width="250" alt="Rings calculator"> |

_To add screenshots: create a `screenshots/` folder in the repo root, drop your PNGs in, and the table above will render automatically._

---

## 🛠️ Tech Stack | التقنيات المستخدمة

| Technology | Details |
|---|---|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | Single-activity, composable screens |
| **Min SDK** | 24 (Android 7.0) |
| **Target / Compile SDK** | 34 |
| **Java / JVM** | 17 |
| **Gradle** | 8.5 |
| **CI** | GitHub Actions (auto-builds debug APK on every push) |

---

## 📂 Project Structure | هيكل المشروع

```
Curtain-Calculator/
├── .github/workflows/
│   └── build-apk.yml            # CI: builds debug APK on push
├── app/
│   ├── build.gradle.kts         # App-level Gradle config
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/curtaincalculator/
│       │   ├── MainActivity.kt  # All UI + calculation logic
│       │   └── CurtainApp.kt    # Application class
│       └── res/                 # Drawables, strings, themes
├── build.gradle.kts             # Root Gradle config
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 🚀 Getting Started | التشغيل

### Prerequisites | المتطلبات
- [Android Studio](https://developer.android.com/studio) (Ladybug or newer recommended)
- JDK 17
- An Android device or emulator running Android 7.0+

### Run the app | تشغيل التطبيق

1. **Clone the repo:**
   ```bash
   git clone https://github.com/JouQarena/Curtain-Calculator.git
   ```
2. **Open** the project in Android Studio.
3. Let Gradle sync, then press ▶️ **Run**.

### Build the APK manually | بناء ملف APK يدويًا

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Download APK from CI | تحميل APK من GitHub Actions
Every push to `main` automatically builds a debug APK:
1. Go to the [**Actions**](https://github.com/JouQarena/Curtain-Calculator/actions) tab.
2. Open the latest successful run of **Build Curtain Calculator APK**.
3. Download the **Curtain-Calculator-APK** artifact (available for 30 days).

---

## 🗺️ Roadmap | أفكار مستقبلية

- [ ] Save calculation history
- [ ] Fabric meter estimation + cost calculation
- [ ] English language toggle
- [ ] Signed release APK / Play Store listing
- [ ] App icon redesign

Contributions and ideas are welcome — feel free to open an issue or pull request! 💡

---

## 🤝 Contributing | المساهمة

1. Fork the repo
2. Create a branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m "Add my feature"`
4. Push and open a Pull Request

---

## 📄 License | الرخصة

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details (or replace with your preferred license).

---

## 👤 Author | المطوّر

**JouQarena** — [GitHub](https://github.com/JouQarena)

Made with ❤️ in Egypt 🇪🇬
