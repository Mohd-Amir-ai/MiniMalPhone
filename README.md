# MiniMalPhone 📱

> An open-source, minimalist Android launcher designed to transform your smartphone into a distraction-free tool. It combines an ultra-clean monochrome design with a **dopamine-swap focus engine** that turns doom-scrolling into earned, reward-driven productivity.

---

## ✨ Philosophy

Smartphones are intentionally engineered like slot machines—vibrant colors, red notification dots, infinite feeds, and algorithmic recommendations compete for your attention.

**MiniMalPhone** flips the script:
1. **Remove Sensory Triggers**: Pure OLED `#000000` black with crisp chalk-white typography. Zero app icons.
2. **Mindful Friction**: Add a 5–10 second breathing pause before launching addictive apps to break impulsive muscle memory.
3. **Dopamine Swap**: Stop getting cheap dopamine from endless scrolling. Earn "Focus Credits" by checking off daily TODOs and deep work sessions, then spend credits if you want leisure screen time.

---

## 🚀 Key Features

* **Monochrome Text Launcher**: Default home screen replacement (`android.intent.category.HOME`) featuring your top 4–6 focus apps in clean typography.
* **Search-First App Drawer**: Instant alphabetical access with type-to-filter search.
* **App Hiding & Renaming**: Hide doom-scroll apps or rename them (e.g. *Instagram* $\rightarrow$ *"Mindless Feed"*).
* **Daily Agenda & "Rule of 3" Tasks**:
  * Swipe right for upcoming calendar events.
  * Swipe left to view today's top 3 priority tasks.
* **The Breathing Gate**: Full-screen mindful pause when attempting to open flagged distraction apps.
* **Time-Bank Focus Economy**: Complete TODOs to earn screen-time credits.
* **100% Offline & Private**: Zero trackers, zero ads, zero analytics, zero external servers.

---

## 🛠️ Architecture & Tech Stack

* **Platform**: Android (Native)
* **Language**: Kotlin
* **UI Toolkit**: Jetpack Compose (Modern declarative UI)
* **Local Persistence**: Room Database & Jetpack DataStore
* **Architecture**: Clean Architecture / MVVM
* **CI/CD**: GitHub Actions for automated cloud builds

---

## 📦 How to Test & Install

1. Download the latest `MiniMalPhone-debug-apk` from the **Actions** or **Releases** tab.
2. Install the `.apk` on your Android device.
3. When prompted, select **MiniMalPhone** as your **Default Home App**.

---

## 🤝 Contributing & License

Contributions, issues, and feature requests are welcome!
License: MIT
