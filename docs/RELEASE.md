# Release signing

The release APK is signed in GitHub Actions from four repository secrets. **You create the keystore and add the secrets yourself; never paste them into chat, an issue or a commit.**

## 1. Create the keystore (once, on your own computer)

    keytool -genkeypair -v -storetype PKCS12 -keystore animalbiotech-release.jks -alias release \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=R. Ramesh, OU=Zoology, O=GASC Nagercoil, C=IN"

`keytool` asks for the keystore password (use the same for the key; PKCS12 uses one password). Check it: `keytool -list -v -keystore animalbiotech-release.jks`.

## 2. Add the secrets (Settings > Secrets and variables > Actions, or with the GitHub CLI on your computer)

    base64 -w0 animalbiotech-release.jks | gh secret set KEYSTORE_BASE64 -R rameshgascngl-create/Animal-Biotechnology-and-Bioinformatics
    gh secret set KEYSTORE_PASSWORD -R rameshgascngl-create/Animal-Biotechnology-and-Bioinformatics     # prompts, not echoed
    gh secret set KEY_PASSWORD      -R rameshgascngl-create/Animal-Biotechnology-and-Bioinformatics     # same value as the keystore password
    gh secret set KEY_ALIAS         -R rameshgascngl-create/Animal-Biotechnology-and-Bioinformatics --body release

(On macOS use `base64 -i animalbiotech-release.jks | tr -d '\n'`.)

## 3. What changes in CI
The *Release candidate* workflow (.github/workflows/release-candidate.yml) decodes `KEYSTORE_BASE64` to a temporary file, sets `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, and builds `assembleRelease` (R8 and resource shrinking on). `app/build.gradle.kts` signs with the release key when `KEYSTORE_PATH` is set and otherwise with the shared debug key. Without the secrets the published file is named `...-NOT-FOR-DISTRIBUTION-debug-signed.apk` and the release notes say so.

**A release-signed APK cannot be installed over a debug-signed one** (different keys): uninstall first, which deletes progress and bookmarks. The upgrade gate in CI therefore tests debug-signed builds against each other; the key change is a one-time cost when you move to the release key.

## 4. Keystore backup (read this)
The keystore and its passwords are the only proof that you are the author of the app. If you lose either, you can never publish an update that installs over the existing app (users would have to uninstall and lose their data), and Google Play can only help if you enrolled in Play App Signing. Keep at least two copies of the `.jks` file on different devices or media (for example an encrypted USB drive and a password manager attachment), and keep the passwords in a password manager, never in the repository. GitHub secrets cannot be read back once saved.

## Debug builds
The APK under the `latest` release is debug-signed with `app/debug.keystore` so that every CI build updates the previous one in place.
