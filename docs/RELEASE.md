# Release signing (Google Play)

1. Create the upload key **once** on your own computer and keep it safe (back it up; losing it means you cannot update the app):

       keytool -genkeypair -v -keystore animalbiotech-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000

2. In the GitHub repository: Settings > Secrets and variables > Actions, add:
   `KEYSTORE_BASE64` (output of `base64 -w0 animalbiotech-upload.jks`), `KEYSTORE_PASSWORD`, `KEY_ALIAS` (upload), `KEY_PASSWORD`.
3. Push any commit. The workflow builds a signed `app-release.apk` and `app-release.aab` and stores them as the
   `release-outputs` artifact of the run. Upload the `.aab` to Google Play (enrol in Play App Signing).
4. Without these secrets the release build is unsigned and is only used to check that R8 shrinking succeeds.

The debug APK published under the `latest` release uses a fixed debug key (`app/debug.keystore`) so updates install over earlier builds.
