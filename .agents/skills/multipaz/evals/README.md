# Multipaz Skill Evals

Use `scenarios.md` as a manual behavioral regression checklist for the skill.
These are not executable tests or proof that an implementation compiles.

## Fixtures

Run each scenario against a project that carries real Multipaz version evidence.
Pick the fixture that matches the scenario:

1. **Consumer project.** Use an existing Kotlin Multiplatform, Android, or iOS
   project with declared released `org.multipaz:*` dependencies. Use it for
   scenarios that inspect a third-party project's version catalog, dependency
   style, source sets, and platform wiring. Do not clone a project solely to run
   an evaluation.
2. **Current Multipaz checkout.** Use the current
   https://github.com/openwallet-foundation/multipaz checkout only for scenarios
   specifically about upstream modules, samples, or Android NFC wiring.
3. **Your own target project.** Any Kotlin Multiplatform, Android, or iOS project
   with a discoverable Multipaz version in a version catalog, Gradle file, or
   lockfile.

Do not run a scenario against a project with no Multipaz dependency and then
record "nothing is configured" as a pass, except for the scenarios that test
exactly that (scenario 1 tests inspection of an unconfigured or partially
configured project).

## Rules

- Every scenario must stay grounded in the target project's declared artifacts and
  files; consult upstream source only when the scenario requires it.
- NFC scenarios must preserve the Android-only support boundary.
- Treat any response that generates iOS NFC credential presentation code as a failure.
- Lack of a local upstream source is not a failure for a released-artifact
  integration. Lack of a declared Multipaz version or required platform-wiring
  evidence must be reported rather than guessed.
