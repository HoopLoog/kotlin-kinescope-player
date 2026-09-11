# Publishing to Maven Central

Canonical coordinates:

```text
io.kinescope:kotlin-kinescope-player:0.1.6
io.kinescope:kotlin-kinescope-shorts:0.1.6
```

Host apps normally depend only on **`kotlin-kinescope-player`** (it pulls Shorts in transitively).

Namespace `io.kinescope` must be **Verified** on [Central Portal](https://central.sonatype.com/).

Modules call `publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)`. Without that, plugin **0.30.0** defaults to legacy OSSRH and fails with `Cannot get stagingProfiles … (402)`.

## One-time setup (secrets stay on your machine / CI)

### 1. User token

Central Portal → account → **Generate User Token**.  
Put into **`~/.gradle/gradle.properties`** (not the repo):

```properties
mavenCentralUsername=<token username>
mavenCentralPassword=<token password>
```

(Equivalent to the `<server>` username/password you were given; do **not** commit them.)

### 2. GPG signing

Central requires signed artifacts:

```bash
gpg --full-generate-key
gpg --list-secret-keys --keyid-format LONG
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
```

In the same `~/.gradle/gradle.properties`:

```properties
signing.keyId=<LAST_8_OF_KEY_ID>
signing.password=<gpg passphrase>
signing.secretKeyRingFile=<path-to-secring.gpg>
```

Export a ring if needed:

```bash
gpg --export-secret-keys -o %USERPROFILE%\.gnupg\secring.gpg <KEY_ID>
```

On modern GPG, also see [Central GPG docs](https://central.sonatype.org/publish/requirements/gpg/) / vanniktech docs for `signing.inMemoryKey` alternatives in CI.

## Publish from this repo

```bash
./gradlew :kotlin-kinescope-shorts:publishToMavenCentral :kotlin-kinescope-player:publishToMavenCentral
```

Or publish both:

```bash
./gradlew publishToMavenCentral
```

Then open [Central Portal → Deployments](https://central.sonatype.com/publishing/deployments).  
If `mavenCentralAutomaticPublishing` is not enabled, click **Publish** on the deployment.

Check that the version resolves:

```groovy
implementation 'io.kinescope:kotlin-kinescope-player:0.1.6'
```

## Git workflow

1. Commit publish config + docs to your **fork** (no secrets).  
2. Open a PR into **upstream**.  
3. After merge, run publish with **org** Central token + GPG (locally or CI on upstream).  
4. JitPack remains **unsupported** for new releases; legacy pins may still resolve — see [installation.md](installation.md).

## Project flags (committed)

See root `gradle.properties`: `GROUP`, `VERSION_NAME`, `mavenCentralPublishing`, `signAllPublications`, POM metadata.  
Optional: `mavenCentralAutomaticPublishing=true` to auto-release after Portal validation.
