# AGENT.MD — Persistent Project Guidelines for AI Coding Agents

> **CRITICAL DIRECTIVE**: This document serves as the permanent reference and rule set for any AI coding agent working on this Android Kotlin project. Always read and strictly adhere to these instructions before making build, Gradle, signing, versioning, packaging, or APK-related changes.

---

## 1. Project Build Principles

- **Native Architecture**: This is a native Android project written in Kotlin and Jetpack Compose.
- **Source Code is Truth**: The source code in this repository is the primary project source. GitHub is strictly an optional source-control/backup/distribution system and is **NOT** a prerequisite for building the APK.
- **Release Preference**: Always prefer a proper Android **RELEASE** build when producing an APK for actual installation or distribution.
- **Never Default to Debug**: Never use a DEBUG APK as the final production APK unless the user explicitly requests a debug build.

---

## 2. Release APK

For production APK generation:
- **Variant**: Build the `release` variant.
- **Dependency Cleanliness**: Do not unnecessarily include debug-only dependencies or debug tooling.
- **Safe Optimization**: Use release optimizations where it is safe and verified.
- **Preserve Functionality**: Keep the APK installable and functional. Never remove required dependencies, resources, or code paths just to reduce APK size.
- **Preserve Architecture**: Do not make arbitrary architecture or dependency changes only for size reduction without checking the project first.
- **R8 / Minification**:
  - If R8 / minification is already configured, preserve it.
  - If R8 is not configured, inspect the project before enabling it. Enable it only when verified safe without breaking runtime reflection, serialization, or Compose layouts.

---

## 3. APK Size Optimization

When requested to reduce APK size, inspect the project first:
- Gradle dependencies & duplicate libraries
- Unused dependencies
- Large images & assets (use modern formats like WebP where appropriate)
- Unused resources & fonts
- Native libraries (`.so` packaging filters)
- Debug-only dependencies
- Build & Compose packaging configuration

**Rules**:
- Do **NOT** delete assets, libraries, features, or resources merely because they appear large. Verify that they are unused or unnecessary first.
- Never sacrifice application functionality or user experience just to achieve a smaller APK size.

---

## 4. Application ID (`applicationId`)

- **Identity Preservation**: The existing `applicationId` in `app/build.gradle.kts` is critical.
- **Do NOT Change**: Never change the existing `applicationId` unless the user explicitly requests a new application identity.
- **Update Compatibility**: Changing the `applicationId` causes Android to treat the new APK as a completely different application rather than an update.
- **Warning Requirement**: Before changing package or application identity, warn the user about the update implications.

---

## 5. Signing Key — Critical

- **Key Stability**: Production APKs must use a stable signing key. The signing key used for the first public release must be preserved for every future update.
- **Strict Prohibitions**:
  - **NEVER** generate a new signing key for every version.
  - **NEVER** replace an existing production keystore without explicit user instruction.
  - **NEVER** commit private keystore files or passwords into GitHub or public source control.
  - **NEVER** put signing passwords directly inside source code.
- **Recommended Practice**:
  - Keep the production keystore in a secure location.
  - Store passwords and secrets securely using `key.properties`, environment variables, or secure CI secret injection.
  - Ensure sensitive signing files are listed in `.gitignore`.
- **Existing Config**: If an existing production signing configuration is found, preserve it. If no signing configuration exists, explain that the user needs to create and safely preserve one before publishing the first production version.

---

## 6. Versioning

Every production update must increase `versionCode`.

```kotlin
// Version 1:
versionName = "1.0"
versionCode = 1

// Version 2:
versionName = "1.1"
versionCode = 2

// Version 3:
versionName = "1.2"
versionCode = 3
```

- **Monotonic Increase**: `versionCode` must strictly increase and never decrease or reset to `1`.
- **User-Facing Label**: `versionName` is the user-facing version string.
- **Update Order**: `versionCode` is the Android update-order integer.
- **Inspection First**: Before creating an update APK, inspect existing version values in `app/build.gradle.kts` and increment appropriately.

---

## 7. Android Update Compatibility

For seamless APK updates without uninstalling the old version, all four criteria must be satisfied:

$$\text{Same } \mathbf{applicationId} + \text{Same } \mathbf{signing\ key} + \text{Higher } \mathbf{versionCode} + \mathbf{Compatible\ package\ config}$$

- **No Uninstall Default**: The agent must **NEVER** tell the user to uninstall the old app as the default solution to a normal update issue.
- **Signature Mismatches**: If Android reports a signature mismatch, investigate the signing configuration first.
- **Package Conflicts**: If Android reports package conflicts, investigate `applicationId` and signing before recommending uninstallation.

---

## 8. Data Preservation

- **Preserve User Data**: Normal APK updates must preserve existing application data.
- **Persistent Structures**: Do not change database identifiers, table names, storage paths, SharedPreferences/DataStore keys, or Room schemas unnecessarily.
- **Migrations**: If database schema changes are required, implement proper database migrations instead of deleting user databases.
- **No Silent Clearing**: Never add code that clears application data on startup or during an update unless explicitly requested.

---

## 9. Build Commands

When building a standard release APK, prefer the project's Gradle wrapper.

- **Linux / macOS / Cloud**:
  ```bash
  ./gradlew assembleRelease
  ```
- **Windows**:
  ```bat
  gradlew.bat assembleRelease
  ```

**Inspection Rule**:
- Before running commands, inspect whether the project uses `gradlew`, `gradlew.bat`, Kotlin DSL (`.kts`), Groovy DSL, product flavors, or custom build variants.
- Inspect the actual output directory (e.g. `app/build/outputs/apk/release/`) rather than assuming a fixed path.

---

## 10. Mobile-Only Building

- The user may develop and build the project from a mobile phone (e.g., Termux, cloud environments, mobile web IDEs) without a desktop PC.
- Do not assume GitHub Actions or a PC is mandatory.
- If the user specifies a mobile-only workflow, provide clean, mobile-optimized CLI instructions.

---

## 11. GitHub & CI/CD

- GitHub is optional for building.
- If GitHub is used:
  - Store source code cleanly.
  - CI/CD workflows may automate release builds.
  - Secrets and keystore passwords must never be committed.
  - Do not create unnecessary GitHub files or workflows unless requested.

---

## 12. Release Build Checklist

Before classifying a build as a production release, verify:
1. `applicationId` has not unexpectedly changed.
2. `versionCode` is greater than the previous production version.
3. `versionName` is correct and descriptive.
4. Release variant is being built.
5. Production signing configuration is correct and verified.
6. Existing production keystore has not been replaced.
7. No signing passwords or secrets are exposed in source control.
8. R8 / minification configuration is verified safe (if enabled).
9. APK size is reasonable and optimized.
10. Required resources, assets, and features are intact.
11. The generated APK file actually exists at the target path.
12. The APK is installable and updates over prior installations.

---

## 13. Update Testing Workflow

When testing the update flow:
1. Install **Version A**.
2. Launch and verify core features.
3. Install **Version B** directly over **Version A** (do not uninstall Version A).
4. Verify Android performs an update in-place.
5. Verify existing application data, preferences, and Room DB records remain intact.
6. Verify important features still work as expected.

If the update fails, investigate `applicationId`, signing certificate, `versionCode`, `minSdk`/`targetSdk`, and ABI splits before suggesting destructive actions.

---

## 14. Important Agent Behavior

Before modifying build, signing, or version configuration:
1. Inspect the existing project files.
2. Identify the current configuration.
3. Preserve existing working configuration.
4. Make the smallest necessary change.
5. Build and test.
6. Report exactly what was changed.

**Strict Prohibitions**:
- Never rewrite the entire Gradle configuration unnecessarily.
- Never replace a working keystore.
- Never change `applicationId` casually.
- Never reset `versionCode`.
- Never delete user data to solve an update problem without explicit permission.
- Never expose signing secrets.

---

## 15. Production Release Commands & Intent Mapping

- When the user says **"Build release APK"**:
  - Inspect the project.
  - Use release configuration.
  - Preserve `applicationId`.
  - Preserve production signing configuration.
  - Build the APK.
  - Verify output and report the generated APK path.

- When the user says **"Make update APK"**:
  - Inspect currently configured version information.
  - Increment `versionCode` (and update `versionName` appropriately).
  - Preserve `applicationId` and signing key.
  - Build release APK.
  - Ensure the output is structured for direct update over the existing install.

---

## 16. First Priority: Stability

- **Stability Over Excess**: Project stability and correctness are always more important than unsolicited optimization.
- **No Unsolicited Infrastructure**: Do not add extra build systems, third-party analytics, unnecessary dependencies, or unrequested services.
- **Simplicity**: Keep the build system simple, reproducible, and maintainable.
