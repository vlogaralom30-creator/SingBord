# SingBord Keyboard ⌨️

> **Ultra-Fast, Zero-Gap 2D Flat Minimalist Keyboard for Android**  
> Engineered for pure typing precision, speed, and privacy.

---

## 🌟 Overview

**SingBord** is an open-source, lightweight, high-performance Android keyboard built from the ground up using **Kotlin** and **Jetpack Compose**. Unlike bloated commercial keyboards loaded with cloud telemetry, predictive tracking, and heavy 3D rendering engines, SingBord returns to what matters most: **speed, tactile accuracy, and 100% offline security**.

Its signature **2D Flat Zero-Gap Layout** separates keys using razor-sharp hairline dividers instead of traditional spaced bubbles. This maximizes touch contact targets, prevents finger slippage, and eliminates visual clutter.

---

## 🚀 Key Features

### 1. 📐 Zero-Gap 2D Flat Architecture
- Keys sit flush against each other with zero wasted spacing.
- Ultra-crisp customizable divider lines (adjustable from 0.5dp up to 3.0dp).
- Flat modern color contrast ensuring maximum legibility in light and dark conditions.

### 2. ⚡ Real-Time Instant Response
- Built directly on Android's `InputMethodService` combined with Compose `ViewCompositionStrategy`.
- Zero latency keystroke delivery via direct `InputConnection.commitText()`.
- Instantaneous settings synchronization via live `OnSharedPreferenceChangeListener`.

### 3. 🎯 Steady Typing Bar (Anti-Jumping Preview)
- Fixed-height 26dp character preview banner.
- Eliminates screen flickering and input-box jumping during fast keystrokes.
- Clear visual confirmation of pressed letters without blocking text fields.

### 4. 🔒 100% Offline & Private by Design
- **Zero Internet Permission** (`android.permission.INTERNET` is NOT requested).
- **Zero Keylogging**: Your passwords, banking details, and private chats never leave your device's memory.
- No analytics, no crashlytics trackers, no ad SDKs.

### 5. 🛠️ Deep Customization
- **Line Thickness**: Fine-tune divider borders to your liking.
- **Keyboard Height**: Compact, Standard, or Tall sizes to fit any screen size or hand ergonomie.
- **Dedicated Number Row**: Toggle a 0-9 numerical row on top for rapid number entry.
- **Haptic & Audio Feedback**: Tactile vibration and crisp key click feedback with zero delay.
- **Auto-Capitalization**: Automatic capitalization for sentences and beginnings of text fields.

---

## 🏗️ Technical Architecture

| Component | Technology | Role |
|---|---|---|
| **Language** | Kotlin 2.2.10 | Modern, type-safe, expressive Android development |
| **UI Framework** | Jetpack Compose (BOM 2024.09.00) | Declarative reactive UI rendering for IME and Settings |
| **System Service** | `InputMethodService` | Low-level Android IME pipeline interfacing with apps |
| **Persistence** | `SharedPreferences` (Real-time listener) | Instant hot-reload of user preferences |
| **Build Toolchain** | AGP 9.1.1 + Gradle 9.0 | Next-generation incremental compilation pipeline |
| **Target SDK** | Android 16 (API Level 36) | Fully compliant with Google Play 2026+ requirements |

---

## 📱 Keyboard Activation Guide

1. **Install APK**: Download the latest release APK from GitHub Releases or build from source.
2. **Enable in Settings**:
   - Open SingBord app → Tap **"1. Enable SingBord"**.
   - Turn on SingBord in Android's *Manage Keyboards* list.
3. **Select Active Keyboard**:
   - Tap **"2. Select Active Keyboard"**.
   - Choose **"SingBord Keyboard"** in the input method selector dialog.
4. **Test & Customize**:
   - Tap the *Interactive Test Typing* field to try out your new keyboard.
   - Adjust line thickness, number row, and haptics in real time!

---

## 🛠️ Building From Source

```bash
# Clone the repository
git clone https://github.com/vlogaralom30-creator/SingBord.git
cd SingBord

# Grant execution permissions
chmod +x gradlew

# Build Debug APK
./gradlew assembleDebug --no-daemon

# The output APK will be generated at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📜 Documentation Index

- [CHANGELOG.md](./CHANGELOG.md) — Detailed version history, milestone levels, and upcoming roadmap.
- [DEV_JOURNEY.md](./DEV_JOURNEY.md) — Behind-the-scenes engineering chronicle, bugs encountered, and sleepless nights.
- [PUBLISHING_AND_MONETIZATION.md](./PUBLISHING_AND_MONETIZATION.md) — Strategic publishing plan (Google Play, F-Droid, Galaxy Store) and revenue generation guide.
- [PRIVACY_POLICY.md](./PRIVACY_POLICY.md) — Complete 100% offline privacy pledge and compliance details.

---

## 👨‍💻 Author & Brand

- **Developed by**: Naxxivo Tech Lab
- **Lead Developer**: @vlogaralom30-creator
- **License**: Apache 2.0 Open Source
