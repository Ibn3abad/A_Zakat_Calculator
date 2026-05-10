- [Startseite](https://ibn3abad.github.io/A_Zakat_Calculator/)

  - [terms-conditions](https://ibn3abad.github.io/A_Zakat_Calculator/terms-conditions)

  - [privacy-policy](https://ibn3abad.github.io/A_Zakat_Calculator/privacy-policy)

# **Zakat Calculator**

#  Accurate, Private, Simple.

**Welcome to your reliable companion for Zakat calculation.** **Fulfilling your Zakat obligation should be simple, accurate, and secure. Our app provides you with an easy-to-use tool to calculate your Zakat obligations according to Islamic principles.**

**Your Privacy Matters.** **We believe that your financial situation is a private matter. That is why our app processes all calculations exclusively on your device. No financial data is sent to an external server.**

## Key Features

- **Precise Calculations:** **User-friendly interface for an accurate assessment.**

- **Privacy-First:** **No data collection on your financial assets.**

- **Completely Free:** **No hidden costs, simple and straightforward.**

## 📱 Download

[Google Play](https://play.google.com/store/apps/details?id=com.ibn3abad.zakat_calculator&pli=1)

## 🛠️ Tech Stack

- **Language:** Kotlin
- **Platform:** Android
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** SQLite (local storage)

## 🔗 Related Projects

- **[Zakat Calculator Backend](https://github.com/Ibn3abad/A_Zakat_Calculator_Backend)** - Supabase backend and API for data sync and authentication
- **[Website](https://ibn3abad.github.io/A_Zakat_Calculator/)** - Web version and documentation

## 📋 Project Structure

```
A_Zakat_Calculator/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/          # Kotlin source code
│   │   │   ├── res/             # Resources (layouts, drawables, etc.)
│   │   │   └── AndroidManifest.xml
│   │   └── test/                # Unit tests
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest version recommended)
- Android SDK 21+
- Kotlin 1.8+
- Gradle 7.0+

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Ibn3abad/A_Zakat_Calculator.git
   cd A_Zakat_Calculator
   ```

2. **Open in Android Studio:**
   - File → Open → Select the project directory
   - Android Studio will automatically download dependencies

3. **Build and Run:**
   - Connect an Android device or start an emulator
   - Click "Run" or press `Shift + F10`

## 🧮 Zakat Calculation Logic

The app calculates Zakat based on:
- **Nisab threshold:** The minimum amount of wealth required to pay Zakat
- **Zakat percentage:** 2.5% of qualifying wealth
- **Lunar year:** Based on Islamic calendar
- **Types of wealth:** Cash, gold, silver, investments, business inventory, etc.

## 🔐 Privacy & Security

- ✅ All calculations performed locally on device
- ✅ No data transmission to external servers
- ✅ No tracking or analytics
- ✅ No ads or in-app purchases
- ✅ GDPR compliant

## 🤝 Integration with Backend

For features like calculation history and cloud sync, connect to the [Zakat Calculator Backend](https://github.com/Ibn3abad/A_Zakat_Calculator_Backend).

**Backend Repository:** https://github.com/Ibn3abad/A_Zakat_Calculator_Backend

## 🧪 Testing

Run unit tests with:
```bash
./gradlew test
```

Run instrumented tests with:
```bash
./gradlew connectedAndroidTest
```

## 📚 Documentation

- [Setup Guide](./docs/SETUP.md) *(if available)*
- [Architecture](./docs/ARCHITECTURE.md) *(if available)*
- [API Integration](./docs/API.md) *(if available)*

## 🐛 Bug Reports & Feature Requests

Found a bug or have suggestions? Please open an [issue](https://github.com/Ibn3abad/A_Zakat_Calculator/issues).

## 📄 License

This project is licensed under the Other license. See LICENSE file for details.

## 👤 Author

**Ibn3abad** - [GitHub Profile](https://github.com/Ibn3abad)

## 🔗 Quick Links

- **Main Repository:** https://github.com/Ibn3abad/A_Zakat_Calculator
- **Backend Repository:** https://github.com/Ibn3abad/A_Zakat_Calculator_Backend
- **Website:** https://ibn3abad.github.io/A_Zakat_Calculator/
- **Google Play:** https://play.google.com/store/apps/details?id=com.ibn3abad.zakat_calculator

---

**Your Privacy Matters.** Accurate, Private, Simple.
