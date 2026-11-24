# Google Play store readiness

Baseline source inspection **verified at `f5294a395fc8f7fdaa37580a9e1fa218a7794fe1`**. This covers repository declarations and app storage calls. Play Console acceptance, the installed release build and phone behaviour are **not verified**.

| Submission gate | Current result | Owner action |
|---|---|---|
| Target API | **targetSdk 36 on branch completion/api36, not verified on a phone.** The branch also declares compile SDK 36, minimum API 26, version code 9 and package `com.gasczoology.animalbiotech`. New phone-app submissions and updates require API 36 or higher from 31 August 2026. | Complete the supported toolchain upgrade and Android 16 runtime checks before treating this as store-ready. The current workflows still use Gradle 8.9, the RC check still expects API 35, and Tier 3 uses an API 35 emulator. In Play Console, use a version code higher than any already uploaded for this package. |
| Download size | **Not verified.** The current Play policy measures compressed delivery size. The base module limit is 500 MB and the total for all modules and install-time asset packs is 4 GB. A mobile-data warning appears above 200 MB. | Check the signed candidate in App Bundle Explorer for its compressed size by device. The local APK or AAB file size alone cannot establish the Play result. |
| Public privacy policy | The HTML is present at `docs/privacy.html`. About also contains an offline policy. The live URL and Play acceptance are **not verified**. | Set up Pages below and enter the resulting public URL in Play Console. |
| Signed bundle | The Gradle release build is configured to use owner-supplied signing material and otherwise falls back to the shared debug key. An upload-key-signed AAB is **not verified**. | Build and sign locally using the registered upload key after the supported toolchain and runtime checks are complete, then validate through Play Console. |

[Google Play target API policy](https://support.google.com/googleplay/android-developer/answer/11926878) · [Google Play compressed size limits](https://support.google.com/googleplay/android-developer/answer/9859372) · [App Gradle configuration](../../app/build.gradle.kts) · [Android 16 source and toolchain audit](android16-behaviour.md)

## Publish the privacy page yourself

1. Open the repository’s **Settings → Pages**. Under **Build and deployment**, select **Deploy from a branch**.
2. Select **main** and **/docs**, then **Save**. The checked main commit `fb2a8e50cc9f90f386011ff55662f0d8527073fa` already contains `docs/privacy.html`. The project site can expose other published content under that folder, so review the folder before enabling it.
3. Wait for the Pages deployment to succeed. Copy the URL shown by GitHub and open its `privacy.html` path in a private browser window. The expected default URL is listed below. Confirm HTTPS, access without signing in and the correct policy text. A Pages source of `/docs` puts the page at `/privacy.html` within the project site.
4. In Play Console → **App content → Privacy policy**, enter the live URL. Review the policy contact and confirm the in-app About policy or link meets Play’s in-app disclosure requirements for the exact submitted build.

[GitHub Pages source settings](https://docs.github.com/en/pages/getting-started-with-github-pages/configuring-a-publishing-source-for-your-github-pages-site) · [Expected privacy page address, not verified](https://rameshgascngl-create.github.io/Animal-Biotechnology-and-Bioinformatics/privacy.html) · [Play user data and privacy policy](https://support.google.com/googleplay/android-developer/answer/10144311)

## Build the upload-key-signed AAB yourself

1. Check **Play Console → App integrity** for the registered upload certificate. For a first release, enrol in Play App Signing and create and securely back up a separate upload key locally. For an existing app, use the registered upload key or complete Play’s upload-key reset before using another key.
2. On your computer, use Android Studio **Build → Generate Signed App Bundle / APK → Android App Bundle → release** with the upload keystore. The Gradle equivalent is `gradle :app:bundleRelease` after supplying `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD` privately to your local build environment. The output is under `app/build/outputs/bundle/release/`.
3. Inspect the bundle signer locally, for example with `keytool -printcert -jarfile app/build/outputs/bundle/release/app-release.aab`. Compare the SHA-256 certificate fingerprint to the **upload** certificate in Play Console. Upload only the signed candidate to your chosen testing track when ready, and check the bundle’s target SDK, version code and compressed size.
4. Keep private signing keys, keystores, passwords, recovery material and Play credentials under your control. The assistant must never receive or hold them. A public certificate fingerprint can be shared for comparison. Keep the private material out of chat, commits and downloadable logs or artifacts.

[Android app signing](https://developer.android.com/studio/publish/app-signing) · [Upload and inspect an Android App Bundle](https://developer.android.com/studio/publish/upload-bundle)

## Data safety form for the current backup option A

| Form question | Proposed answer | Repository evidence and limit |
|---|---|---|
| Does the app collect or share any of the listed user data types? | **No**, for the inspected source and current design. | The source manifest declares no permissions, including no internet permission. The production dependency declarations contain Android UI and platform libraries and no ads or analytics SDK. The inspected storage path keeps study progress and preferences in app-private `animal_biotech` SharedPreferences. The final merged manifest, effective dependencies and runtime network behaviour are **not verified**. |
| Which data types, purposes and transmission safeguards apply? | No collected or shared types for this design. Transmission fields are inapplicable if Play does not surface them. | Local bookmarks, quiz results, reading position, flashcard progress and settings are local processing. This is not a claim about encryption in transit. |
| Does the app create accounts or offer account deletion? | No app account creation. Account deletion is inapplicable. Users can clear local study records via Android app storage settings or uninstall. | Inspected source and privacy wording describe the local storage design. The behaviour of the submitted phone build is **not verified**. |
| Do study records enter cloud backup or device transfer? | Option A configures the records to stay on-device. | The source uses `allowBackup=false` and `fullBackupContent=false`, plus explicit shared-preference exclusions for cloud backup and device transfer. Actual OEM and Android backup, restore and migration behaviour are **not verified**. If option B is approved, revisit the policy and declarations before submitting. |

[Source manifest](../../app/src/main/AndroidManifest.xml) · [Backup rules](../../app/src/main/res/xml/data_extraction_rules.xml) · [Local storage](../../app/src/main/java/com/gasczoology/animalbiotech/Data.kt) · [Play Data safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469) · [Android backup and transfer behaviour](https://developer.android.com/identity/data/autobackup)
