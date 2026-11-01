# Saikou Neo extension template

Saikou Neo is an anime app for Android TV and Fire TV. It picks the anime and
episode itself, and a source tells it where to play them. This template is a
working source with no servers yet. Copy it, fill in your code, and press a
button to get a link and short code you can add in the app.

## Make your source

### 1. Copy the template

Select **Use this template**, then **Create a new repository**.

### 2. Name it

Open `gradle.properties` and change the three lines at the top:

```properties
source.name=My source
source.id=io.github.yourname.mysource
source.version=1.0.0
```

The name is what people see in Saikou Neo. The id is unique to your source:
lowercase words with dots, usually starting with your GitHub name.

### 3. Write your code

All of it goes in
[`StarterSource.kt`](source/src/main/kotlin/com/example/source/StarterSource.kt),
which has two functions:

- `servers(episode)` gets the anime's titles, ids and episode number. Find the
  episode on your site and return the servers that have it.
- `stream(server)` gets one of those servers back. Return the video links for
  it.

The comments in the file show what to return. To check your code, change the
sample episode in
[`StarterSourceTest.kt`](source/src/test/kotlin/com/example/source/StarterSourceTest.kt)
to one your site has, then run:

```bash
./gradlew :source:testDebugUnitTest
```

### 4. Add a signing key

You only do this once. The release signs your APK with a key kept in your
repo's secrets, so every version of your source comes from the same key.

Make the key with `keytool`, which comes with Java. It asks for a password.
The key's name goes into every APK, so keep it generic. Keep the file and
password somewhere safe, because you need the same key for every update.

```bash
keytool -genkeypair -keystore release.jks -alias source -keyalg RSA -keysize 4096 -validity 10000 -dname "CN=Source"
```

Then, in your repo on GitHub, go to **Settings > Secrets and variables >
Actions** and add two secrets:

| Name | Value |
| --- | --- |
| `KEYSTORE_PASSWORD` | The password you chose. |
| `KEYSTORE_BASE64` | The key file as text. Make it with the command below and paste the output. |

On macOS or Linux:

```bash
base64 < release.jks
```

On Windows, in PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.jks"))
```

Never commit `release.jks`. The template's `.gitignore` already leaves it out.

### 5. Release it

Go to **Actions > Release > Run workflow**. It runs your tests, builds a signed
APK and puts it on a new release with a `source.json` list. The release notes
give a short v.gd code for that list. In Saikou Neo, open **Settings > Sources >
Add a source** and type the code, or the link:

```text
https://github.com/<you>/<repo>/releases/latest/download/source.json
```

The code and the link stay the same for every release and always lead to your
newest version. The code is all lowercase, so it's easy to type on a TV. To
choose it yourself, add a variable named `SHORT_CODE` under **Settings > Secrets
and variables > Actions**, such as `mysource`. It needs 5 to 30 letters, numbers
or underscores, and nobody else can be using it on v.gd.

## Update your source

Raise `source.version` in `gradle.properties`, such as from `1.0.0` to `1.1.0`,
and run the Release workflow again. Each version can only be released once.
People get the update by adding the code or link again.

## Keep your code private

A TV can't download from a private repo, so a private source releases into a
separate public repo instead. Several private sources can share one, and its
`sources.json` lists them all.

1. Make the public repo, with a README so it isn't empty. An organization works
   well for this.
2. Make a GitHub App there with **Repository permissions > Contents: Read and
   write** and no webhook. Install it on the public repo and generate a private
   key.
3. In your private repo, under **Settings > Secrets and variables > Actions**,
   add the variables `RELEASES_OWNER` (the public repo's owner), `RELEASES_REPO`
   (its name) and `RELEASES_CLIENT_ID` (the app's client ID), and the secret
   `RELEASES_APP_KEY` (the private key file's text).

Release as before. The APK goes on a release in the public repo, and the
source's entry in its `sources.json` is updated. The code in the release notes
is for that list.

The app's bot is the only author the public repo shows. Releases and APKs are
named by a short hash of the source's id, so the public repo's tags and file
names don't name the source. The list still has the source's name and id,
because Saikou Neo shows them.

## Good to know

- Saikou Neo provides the extension API and Kotlin when it loads your source,
  so the APK only holds your own code.
- To use a library, such as an HTTP client, add it to `dependencies` in
  [`source/build.gradle.kts`](source/build.gradle.kts) with `implementation`.
  It goes into your APK.
- Renaming `StarterSource` or its package is optional. If you do, update
  `source.class` in `gradle.properties` to match.
- R8 is off. If you turn it on, add a keep rule for the class in
  `source.class`.
- To build a signed APK on your own computer, set `SIGNING_STORE_FILE` to the
  key file's path and `SIGNING_STORE_PASSWORD` to its password, then run
  `./gradlew :source:assembleRelease`.
- Only publish sources you have the right to share.
