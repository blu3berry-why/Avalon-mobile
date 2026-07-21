# Dev-environment gotchas

Traps that cost time in this repo and are not visible from the code.

## Gradle cannot run inside the agent sandbox

**Symptom**

```
FAILURE: Build failed with an exception.
* What went wrong:
Gradle could not start your build.
> Could not create service of type FileLockContentionHandler using
  BasicGlobalScopeServices.createFileLockContentionHandler().
   > java.net.SocketException: Operation not permitted
```

**Root cause — it is a socket restriction, not a filesystem one.**

`~/.gradle` is already on the sandbox write-allowlist, so the obvious fix (adding
more filesystem paths) is a no-op. Gradle's `FileLockContentionHandler` binds a
local TCP socket to coordinate file locks between processes, and the sandbox's
network policy denies the bind.

Two consequences worth knowing:

- `./gradlew --version` **succeeds** inside the sandbox — it never starts the
  build services, so it does not bind the socket. It is not a valid smoke test
  for "gradle works here".
- Any real task (`compileKotlinDesktop`, `assemble`, `desktopTest`, …) fails
  before configuration starts.

**Workaround**

Run gradle with the sandbox disabled. There is no filesystem allowlist entry
that fixes this; the network policy is the binding constraint.

## Reading gradle build output

`build/test-results/**` lives under the project directory and is readable, but
the run that produces it needs the same sandbox exemption as the build itself.
Test counts are in the `<testsuite tests=... failures=...>` attributes of
`core/*/build/test-results/desktopTest/*.xml`.

## Warnings that are pre-existing, not caused by your change

These appear on every `:core:data` build and are not signal:

- `KLIB resolver: The same 'unique_name=... ' found in more than one library`
  — androidx vs `org.jetbrains.compose` / `org.jetbrains.androidx` duplicates of
  `annotation`, `collection`, `lifecycle-*`.
- `Cannot infer a bundle ID from packages of source files and exported
  dependencies, use the bundle name instead: CoreData` — iOS framework linking.
