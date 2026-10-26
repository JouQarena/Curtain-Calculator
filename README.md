# 🪟 حاسبة الستائر | Curtain Calculator

[![Build APK](https://github.com/JouQarena/Curtain-Calculator/actions/workflows/build-apk.yml/badge.svg)](https://github.com/JouQarena/Curtain-Calculator/actions/workflows/build-apk.yml)
![Platform](https://img.shields.io/badge/platform-Android-green)
![Kotlin](https://img.shields.io/badge/kotlin-1.9-blue?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=android)
![Min SDK](https://img.shields.io/badge/minSdk-24-orange)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

A simple, offline-first Android app (in Arabic 🇪🇬) that helps curtain makers and homeowners take accurate curtain measurements — from window/rod sizing to calculating the ideal number of curtain rings, presses per meter, and spacing, with a built-in interactive tutorial and daily calculation history.

تطبيق أندرويد بسيط باللغة العربية يساعد في حساب مقاسات الستائر بدقة — من قياس الشباك والماسورة إلى حساب العدد المثالي للحلقات وعدد الكبسات في كل متر والمسافة بين الحلقات، مع دليل استخدام تفاعلي وسجل حسابات مقسّم حسب اليوم.

---

## ✨ Features | المميزات

### 1️⃣ Window Calculator | حاسبة قياس الشباك 🪟
Enter the **window width**, the **rod (ماسورة) width**, and an optional **rod deduction** (defaults to `10` cm, customizable to `0` or any positive decimal value) to get the exact distance to leave on each side.

أدخل **عرض الشباك** و**عرض الماسورة** وقيمة **الخصم من الماسورة** (القيمة الافتراضية `10` سم وقابلة للتعديل لأي رقم موجب أو كسر عشري) لتحصل على المسافة الصحيحة من كل جانب.

> **Formula | المعادلة:**
> ```
> المسافة من كل جانب = (عرض الشباك − (عرض الماسورة − الخصم)) ÷ 2
> ```

---

### 2️⃣ Rings & Presses Calculator | حاسبة عدد الحلقات والكبسات ⭕
Enter the **curtain width** in centimeters to get all valid combinations of ring counts, presses per meter, and ideal spacing:

أدخل **عرض الستارة (سم)** لتحصل على كل الاحتمالات المناسبة لعدد الكبسات في المتر وعدد الحلقات والمسافة بينها:

- Subtracts `10 cm` allowance from the total width (**effective width** / المقاس الفعلي).
- Tests all **odd divisors** (`3, 5, 7, 9, …`) and keeps results where spacing falls between **15 and 18 cm** (inclusive).
- Calculates the **actual ring count** (`odd divisor + 1`) and the **number of presses per meter** (`عدد الكبسات في كل متر`).

> **Formulas | المعادلات:**
> ```
> المقاس الفعلي = عرض الستارة − 10
> المسافة بين الحلقات = المقاس الفعلي ÷ رقم القسمة الفردي   (الشرط: 15 ≤ المسافة ≤ 18 سم)
> عدد الحلقات الفعلي = رقم القسمة الفردي + 1
> عدد الكبسات في كل متر = عدد الحلقات الفعلي ÷ (عرض الستارة بالسنتيمتر ÷ 100)
> ```

---

### 3️⃣ Quick Keyboard Calculation | الحساب السريع من الكيبورد ⌨️
- Calculate results either by tapping the **«احسب»** button **or** by pressing the **Checkmark / Done (✓)** action directly on the keyboard (which calculates immediately and dismisses the keyboard).
- إمكانية إظهار الناتج فوراً عند الضغط على علامة **صح (✓)** في لوحة المفاتيح دون الحاجة للضغط على زر «احسب»، مع بقاء زر «احسب» متاحاً على الشاشة.

---

### 4️⃣ Daily Calculation History Folders | سجل الحسابات بالفولدرات اليومية 📂
- A floating action button at the **bottom-left** of the screen opens the **Calculation History (سجل الحسابات)** page.
- Every calculation (Window or Rings) is saved automatically on the device.
- Calculations are organized by **date (day)** into **full-width rectangular folders** (`مستطيلات بعرض الشاشة`).
- Tap any day's folder to view all calculations performed on that day.
- **Flexible deletion:** delete a **single calculation** inside a folder, or delete an **entire day's folder** at once.

---

### 5️⃣ Interactive In-App Guide | الدليل التفاعلي داخل التطبيق 💡
- Built-in step-by-step interactive spotlight tutorial (`TutorialGuide.kt`) with visual illustrations and arrows pointing to each field, button, result card, and history button.
- Can be dismissed temporarily (`×`) or marked as completed (`✓`).

---

### General | عام
- 🌙 **Material 3 + Dynamic colors** — follows system light/dark theme (Android 12+)
- ↔️ **Full RTL Arabic UI** — واجهة عربية كاملة من اليمين لليسار
- 📴 **100% offline** — no internet, no permissions, no tracking
- ✅ Input validation with clear Arabic error messages & unit tests

---

## 📱 Screenshots | صور التطبيق

> Replace these placeholders with real screenshots from your device.

| Window Calculator | Rings Calculator | Calculation History |
|---|---|---|
| <img src="screenshots/window.png" width="240" alt="Window calculator"> | <img src="screenshots/rings.png" width="240" alt="Rings calculator"> | <img src="screenshots/history.png" width="240" alt="Calculation history"> |

_To add screenshots: create a `screenshots/` folder in the repo root, drop your PNGs in, and the table above will render automatically._

---

## 🛠️ Tech Stack | التقنيات المستخدمة

| Technology | Details |
|---|---|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | Single-activity, composable screens + local persistence (`SharedPreferences` JSON) |
| **Min SDK** | 24 (Android 7.0) |
| **Target / Compile SDK** | 34 |
| **Java / JVM** | 17 |
| **Gradle** | 8.5 |
| **Testing** | JUnit 4 (`CalculatorLogicTest`) |
| **CI** | GitHub Actions (auto-builds debug APK on every push) |

---

## 📂 Project Structure | هيكل المشروع

```
Curtain-Calculator/
├── .github/workflows/
│   └── build-apk.yml                         # CI: builds debug APK on push
├── app/
│   ├── build.gradle.kts                      # App-level Gradle config
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/curtaincalculator/
│       │   │   ├── MainActivity.kt           # Calculators UI, keyboard actions, history & logic
│       │   │   ├── TutorialGuide.kt          # Interactive spotlight tutorial & illustrations
│       │   │   └── CurtainApp.kt             # Application class
│       │   └── res/                          # Adaptive icons, drawables, strings, themes
│       └── test/java/com/example/curtaincalculator/
│           └── CalculatorLogicTest.kt        # Unit tests for calculator formulas & history
├── artwork/                                  # App icon SVG & PNG source artwork
├── build.gradle.kts                          # Root Gradle config
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

### Run unit tests | تشغيل الاختبارات

```bash
./gradlew testDebugUnitTest
```

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

- [x] Customizable rod deduction
- [x] Interactive first-time user guide
- [x] Presses per meter calculation (`عدد الكبسات في كل متر`)
- [x] Direct calculation from keyboard Done (✓) button
- [x] Calculation history organized into daily folders with single/folder deletion
- [ ] Export / share calculation results via WhatsApp or PDF
- [ ] Fabric meter estimation + cost calculation
- [ ] English language toggle

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
