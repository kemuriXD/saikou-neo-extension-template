# Saikou Neo extension template

A starting point for a Saikou Neo source. Saikou Neo is an anime app for
Android TV and Fire TV that ships with no video sources. A source is a small
APK the app loads to play an episode. The app picks the anime and episode from
its own catalogue, and the source returns servers and streams for it.

This template builds a source that has no servers yet. Copy it, fill in a few
settings, and push a tag to get a signed APK on a GitHub release, ready to add
in Saikou Neo.

## Make your own

1. Select **Use this template** on GitHub to make your own repo.
2. In `gradle.properties`, set your source's id, name, class and version.
3. Rename `StarterSource` and its package to match `source.id` and
   `source.class`, then write `servers()` and `stream()`.
4. Make a signing key and add it to your repo's secrets (see below).
5. Push a tag named after the version, such as `v1.0.0`.

The release workflow runs the tests, builds a signed APK and attaches it to a
release as `source.apk`. Add its link in Saikou Neo under
**Settings > Sources > Add a source**:

```text
https://github.com/<you>/<repo>/releases/download/v1.0.0/source.apk
```

The link to your newest release stays the same each time:

```text
https://github.com/<you>/<repo>/releases/latest/download/source.apk
```

## Settings

All of them are in `gradle.properties` and go into the APK's manifest.

| Setting | What it is |
| --- | --- |
| `source.id` | The package name. A new version with the same id replaces the old one in the app. |
| `source.name` | The name shown in Settings > Sources. |
| `source.class` | The class that implements `SourceExtension`. It needs a constructor with no arguments. |
| `source.versionCode` | A whole number. Raise it for every release. |
| `source.versionName` | Shown in the app. The release tag is `v` followed by this. |
| `source.apiVersion` | The extension API version you build against. |

## The extension API

The source builds against the Saikou Neo extension API from Maven Central,
`io.github.kemurixd:saikou-neo-extension-api`. Its version is the API version.
The app provides the API and the Kotlin standard library when it loads the
source, so both are `compileOnly` and the APK stays a few kilobytes.

The app loads sources built for its own API version, or an older one that's
still compatible. When a new API version comes out, raise `source.apiVersion`
only if you need something it adds.

R8 is off. If you turn it on, add a keep rule for your `source.class` and its
constructor. The API's own keep rules don't apply to a `compileOnly`
dependency.

## Signing

Every release is signed with your own key. Keep the key and its passwords
private and backed up: the key is never committed, and the release workflow
reads it from repository secrets.

Make a key with `keytool`, which comes with any JDK:

```bash
keytool -genkeypair -v -keystore release.jks -alias source -keyalg RSA -keysize 4096 -validity 10000
```

Then add four secrets under **Settings > Secrets and variables > Actions** in
your repo, or with the GitHub CLI:

```bash
base64 -w 0 release.jks | gh secret set KEYSTORE_BASE64
gh secret set KEYSTORE_PASSWORD
gh secret set KEY_ALIAS --body source
gh secret set KEY_PASSWORD
```

On macOS, use `base64 -i release.jks` instead. To build a signed APK on your
own computer, set `SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`,
`SIGNING_KEY_ALIAS` and `SIGNING_KEY_PASSWORD` and run
`./gradlew :source:assembleRelease`. Without them, the release APK is unsigned
and the app can't use it.

## Build and test

```bash
./gradlew :source:testDebugUnitTest
```

The test calls the source with a sample episode. Change it to an anime and
episode your site has.
