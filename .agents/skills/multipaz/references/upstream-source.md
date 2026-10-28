# Current API and Source Discovery

This skill deliberately has no source pin. Multipaz KDoc is built from the
current repository, so it is useful for public-API discovery but is not
version-specific proof for a project using an older released artifact:

- [Multipaz KDoc](https://developer.multipaz.org/kdocs/index.html)
- [Multipaz source repository](https://github.com/openwallet-foundation/multipaz)

## Default evidence order

1. Read the target's resolved Multipaz version and dependency declarations.
2. Inspect its Gradle files, source sets, app entry points, and existing tests.
3. Use local Gradle/IDE artifact sources and compilation to verify the APIs used
   for a released dependency. Use KDoc to discover symbols or clarify concepts.
4. Do not clone or request Multipaz source by default.
5. Consult upstream source or a version-matched sample only for unreleased APIs,
   Android NFC service/manifest wiring, unavailable artifact sources, or a
   compilation/runtime discrepancy. If that evidence is essential but absent,
   report the limitation instead of guessing.

## Maintenance policy

Keep this skill limited to stable workflows and guardrails. Do not add public
API signatures or copyable SDK code. A new Multipaz release does not require a
skill update unless these stable workflows or platform-support boundaries change.
