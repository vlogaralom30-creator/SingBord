# SingBord Changelog & Version Evolution 📋

All notable changes, milestones, and feature evolutions of the **SingBord** project are documented in this file.

The project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## 🏆 Current Project Status: **Level 1.0 — Production-Ready Stable (v1.0.0)**

| Dimension | Current Rating | Notes |
|---|---|---|
| **Stability** | ⭐⭐⭐⭐⭐ (5/5) | Zero ANRs, rock-solid typing engine, no memory leaks |
| **Typing Smoothness** | ⭐⭐⭐⭐⭐ (5/5) | Constant-height preview eliminates all layout jumps |
| **Settings Sync** | ⭐⭐⭐⭐⭐ (5/5) | Live hot-reload via SharedPreference listeners |
| **CI/CD Reliability** | ⭐⭐⭐⭐⭐ (5/5) | Fully automated GitHub Actions APK generation |
| **Privacy Compliance** | ⭐⭐⭐⭐⭐ (5/5) | 100% offline, zero network permissions declared |

---

## 🔄 Version History

### [v1.0.0] - 2026-09-15 (Current Stable Public Release)
> **Milestone**: Full Production Readiness, Flawless Stability, and Brand Refresh.

#### ✨ Features & Upgrades
- **Fixed-Height Anti-Jumping Bar**: Replaced dynamic 32dp popup badge with a permanent 26dp character preview banner. Screen input fields no longer flicker or bounce up and down during rapid typing!
- **Real-Time Settings Hot-Reload**: Integrated `SharedPreferences.OnSharedPreferenceChangeListener` inside `SingBordInputMethodService`. Changing line thickness, keyboard size, or number row in settings now immediately updates the open keyboard in real time without needing an app restart.
- **Enhanced Adaptive App Icon**: Introduced a sleek, high-contrast 2D minimalist vector keycap emblem with deep cobalt gradient background.
- **In-App Privacy & Terms Dialogs**: Transparent legal documentation embedded directly inside the main settings screen for instant user reassurance.
- **Dynamic Activation Observer**: Added `LifecycleEventObserver` listening to `Lifecycle.Event.ON_RESUME` in `MainActivity.kt`. Returning from system keyboard picker instantly reflects "Selected ✓" status.

#### 🐛 Bug Fixes & Stability
- Completely eliminated keystroke jitter and layout height re-calculation penalties.
- Fixed input connection unbinding on certain custom Android OEM skins (MIUI / HyperOS / ColorOS).

---

### [v0.9.5] - 2026-09-11 (Build & CI Stabilization)
> **Milestone**: AGP 9.1.1, Kotlin 2.2.10, and GitHub Actions CI Resolution.

#### ✨ Features
- Added automated CI build pipeline in `.github/workflows/android.yml`.
- Automatic Android SDK 36 license agreement and self-healing keystore generation.

#### 🐛 Bug Fixes
- **AGP 9.1.1 Source Sets Conflict**: Resolved `Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin` by adding `android.disallowKotlinSourceSets=false` to `gradle.properties`.
- **KSP Version Alignment**: Resolved `ApplicationManager.getApplication() is null` by synchronizing `googleDevtoolsKsp` to `2.2.10-2.0.2` matching Kotlin 2.2.10.
- **CI Keystore Fallback**: Handled missing or corrupted keystores on GitHub runners via automatic `keytool` generation.

---

### [v0.8.0] - 2026-09-08 (The 2D Zero-Gap Overhaul)
> **Milestone**: Introduction of the Signature 2D Flat Layout.

#### ✨ Features
- **Zero-Gap Keyboard Matrix**: Redesigned keyboard from conventional bubble keys to a sleek, line-separated grid.
- **Adjustable Line Thickness**: Added custom slider supporting 0.5dp to 3.0dp border widths.
- **Keyboard Height Scaling**: Added Compact (220dp), Standard (260dp), and Tall (300dp) options.
- **Dedicated Number Row**: Toggleable top number row for quick alphanumeric entry.
- **Haptic & Sound Integration**: Direct Android `AudioManager` and `Vibrator` feedback on key touch.

---

### [v0.5.0] - 2026-08-25 (Jetpack Compose IME Architecture)
> **Milestone**: Transition from Legacy XML Views to Modern Jetpack Compose IME.

#### ✨ Features
- Integrated Compose with Android `InputMethodService` using `ViewCompositionStrategy.DisposeOnLifecycleDestroyed`.
- Added shift key states (Lowercase, Uppercase, Caps Lock toggle).
- Added Symbol mode (`?123`) and alternate punctuation layout.
- Added long-press backspace continuous deletion support.

---

### [v0.1.0] - 2026-08-10 (Initial Prototype)
> **Milestone**: Proof of Concept.
- Basic Android IME service registered with system settings.
- Initial QWERTY layout rendering basic text input.

---

## 🗺️ Future Roadmap & Upcoming Levels

### [Level 1.1 — Localization & Expressions] (Target: v1.1.0)
- [ ] **Bangla Layout Integration**: Native Phonetic & Probhat layouts for Bengali typists.
- [ ] **Emoji Picker Panel**: Lightweight, offline Unicode emoji selector without extra library bloat.
- [ ] **Color Theme Presets**: Pure AMOLED Black, Warm Sand, Modern Pastel, and Classic High Contrast.

### [Level 1.2 — Ergonomics & Gestures] (Target: v1.2.0)
- [ ] **One-Handed Floating Mode**: Left/Right hand dockable mode for large screen tablets and phablets.
- [ ] **Spacebar Swipe Cursor Control**: Slide left/right on spacebar to accurately position cursor in text fields.
- [ ] **Clipboard History Vault**: 100% local, encrypted temporary clipboard manager.
