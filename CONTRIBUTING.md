# Contributing to Gephi

This guide covers setting up a development environment, building and running Gephi locally, finding your way around the codebase, and the pull-request and release workflows.

## Development environment

Gephi is a multi-module Maven project and currently uses JDK 17.

Two IDEs are officially supported:

- NetBeans IDE
- IntelliJ IDEA

Both recognize Gephi as a multi-module Maven project and use Maven behind the scenes for building. No IDE is required, however — everything below can also be done with the `mvn` CLI directly from the repository root.

Whichever setup you choose, the goal is the same: build, run, and debug Gephi locally, with breakpoints and fast unit-test execution. Unit tests (JUnit) should run quickly, making it possible to work in a test-driven loop without starting the full Gephi application for every iteration.

## Building and running

### First build

When starting out, or when importing Gephi into an IDE for the first time, run a complete build from the top-level Gephi parent project. This builds every module:

```bash
mvn -T 4 clean install
```

Parallel builds are supported — the `-T 4` above builds on four threads. Because module dependencies form a tree and circular dependencies are not allowed, modules at the top of the tree are built first and dependent modules are then built in parallel. The Gephi application module is always built last because it depends on every other module.

After a successful complete build, run the locally built application:

```bash
cd modules/application
mvn nbm:cluster-app nbm:run-platform
```

Two other commands are useful day to day:

```bash
# Faster iteration: skip tests
mvn -T 4 clean install -P skipTests

# Match what CI runs (build + tests + checkstyle)
mvn -T 4 --batch-mode -Djava.awt.headless=true verify -P enableCheckStyle
```

### After changing a module

Unit tests can use source changes immediately. To run the full Gephi application with a change:

1. Rebuild the module that was changed, e.g. `mvn -pl modules/<ModuleName> -am install` (the `-am` flag also rebuilds its dependencies).
2. Rebuild the application module: `cd modules/application && mvn install`.
3. Run Gephi locally: `mvn nbm:cluster-app nbm:run-platform` (from `modules/application`).

Rebuilding the changed module writes or overwrites its local JAR in the Maven local repository under `.m2`. If the module is not rebuilt, running Gephi can still use the previous local JAR rather than the new source changes. Rebuilding the application module afterwards ensures the application uses the latest local artifacts.

When uncertain, rebuild the entire repository with `mvn -T 4 clean install`. This is slower but ensures everything is current.

## Understanding the codebase

Gephi is split into 66 modules, per the root `pom.xml`'s `<modules>` list (not counting the `application` module itself). Module dependencies form a tree, and circular dependencies are not allowed — this is what allows Maven to build independent parts in parallel.

Core modules are separated from user-interface modules so that core functionality can be used by command-line tools and libraries without UI dependencies. A plugin is also a module: APIs expose functionality to other modules, SPIs are extension points, and plugins extend SPIs.

**Core graph structure is developed in a separate repository.** `modules/GraphAPI` here is only a thin NetBeans-module wrapper around the `graphstore` library (a Maven dependency, see `graphstore.version` in the root `pom.xml`). The actual graph/node/edge data structure lives in [github.com/gephi/graphstore](https://github.com/gephi/graphstore) — changes to the core graph structure itself (storage, indexing, node/edge/column implementation) should be made there, not in this repo.

### Repository layout

The repository ([github.com/gephi/gephi](https://github.com/gephi/gephi)) is organized as follows:

```text
.github/
└── workflows/       # GitHub Actions workflows
modules/
├── application/     # Gephi application module (built last, depends on all others)
└── ...              # Source code for the other 66 modules
src/                  # Extra files, including the macOS launcher
pom.xml               # Parent POM containing shared configuration
```

The root `pom.xml` is the parent POM for the multi-module Maven project. Shared dependency versions and build configuration are kept there whenever possible; each module inherits this configuration by declaring the root POM as its parent. Configuration may be placed in a module's own POM when it is highly specific, but this is uncommon.

The application module contains mostly application configuration, including branding, version, and installer configuration. It depends on all other modules.

### Module structure

A module follows Maven conventions:

```text
ModuleName/
├── src/
│   ├── main/
│   │   ├── java/          # Sources
│   │   ├── nbm/
│   │   └── resources/     # Bundles, localization files, other files, icons, and images
│   └── test/
│       └── java/          # Test sources
└── pom.xml                # Dependencies, public packages, and extra configuration
```

Each module's `pom.xml` declares its dependencies and public packages. Dependencies are standard Maven dependencies. Public packages define what other modules can access; implementation packages remain hidden.

### Module names

Most modules follow these roles:

| Module pattern | Role | Count among the 66 modules |
|---|---|---:|
| `ImportAPI` | Defines APIs and SPIs and implements APIs | 17 |
| `ImportPlugin` | SPI implementations | 11 |
| `ImportPluginUI` | UI-only SPI implementations | 6 |
| `DesktopImport` | Remaining UI code | 17 |

Together, 51 of the 66 modules follow this convention. The same pattern is used in other areas, including Layout, Preview, and Statistics. The remaining modules cover other functionality, such as the welcome screen (`WelcomeScreen`) and settings migration between versions (`SettingsUpgrader`).

### Package names

Use the existing package conventions:

| Role | Package convention |
|---|---|
| API | `org.gephi.NAME.api` |
| API implementation | `org.gephi.NAME` |
| SPI | `org.gephi.NAME.spi` |
| SPI implementation | `org.gephi.NAME.plugin` |
| SPI implementation, UI only | `org.gephi.ui.NAME.plugin` |
| UI | `org.gephi.desktop.NAME` |

### APIs, SPIs, and public packages

APIs and SPIs are plain Java interfaces.

- APIs offer functionality to other modules.
- SPIs are meant to be extended.
- Plugins always extend an SPI.
- APIs and SPIs should be clearly documented.
- API and SPI changes are recorded in the Javadoc overview page (see [API changelog](#api-changelog) below).
- Most APIs aim to remain backward compatible; some may be marked under development while their contracts are refined.

Only packages declared public in a module's `pom.xml` are accessible to other modules. API and implementation code may be in the same module, but implementation packages must remain hidden so consumers do not depend on details that may later be replaced.

### API changelog

Every public API/SPI change is recorded in `src/main/javadoc/overview.html`, under the `<h2>API Changes</h2>` section. This file is the source used to compile release notes, so new entries must follow its existing formatting convention exactly:

- Entries are grouped by release under an `<h3>0.11.0</h3>`-style heading (the current in-progress version). Add a new `<h3>` only when starting the next version's entries; otherwise append to the existing one for the version you're targeting.
- Within a version, entries are grouped by API under an `<h4>` heading using the API's name (e.g. `<h4>Graph API</h4>`, `<h4>Import API</h4>` — match the name used in ARCHITECTURE.md's [Main APIs](ARCHITECTURE.md#main-apis) table). Add a new `<h4>` (with its own `<ul>`) if that API has no entries yet in this version.
- Each change is one `<li>` in that API's `<ul>`, written as a short, self-contained sentence describing what changed and why it matters to a consumer, with identifiers wrapped in `<code>` tags (e.g. `Addition of <code>newWorkspace()</code> in <code>ProjectController</code>...`).
- Don't use the older `(Month DD YYYY)`-prefixed, undifferentiated-by-API list style — that's the legacy format used only in the `<h3>Archive</h3>` section at the bottom, kept for history and not a pattern to extend.
- If an API's stability changes (e.g. goes from under development to stable, or gets deprecated), update its `<span class="unstable">`/`<span class="deprecated">` marking in the `<h2>API List</h2>` section at the bottom of the same file.

### Adding an extension

Implement the appropriate SPI and register the implementation with the NetBeans service-provider annotation:

```java
@ServiceProvider(service = Renderer.class)
public class NodeRenderer implements Renderer {
}
```

Lookup discovers registered implementations at runtime:

```java
for (Renderer renderer : Lookup.getDefault().lookupAll(Renderer.class)) {
}
```

Without the service-provider annotation, `lookupAll` will not discover the implementation.

The available extension categories are:

| SPI | Extension point |
|---|---|
| Import SPI | File, database, and wizard importers |
| Layout SPI | Layout algorithms |
| Statistics SPI | Other algorithms |
| Tools SPI | Tools in the menu bar |
| Project SPI | Persistence providers |
| Export SPI | File exporters for graphs and graphics |
| Filters SPI | Filters |
| Preview SPI | Preview builders and renderers |
| Generator SPI | Generators, similar to importers |
| Data Laboratory SPI | Manipulators |
| Appearance SPI | Transformers for ranking and partitioning |
| Visualization SPI | Renderers for the future visualization engine |

Some SPIs have UI components. Keep those implementations in a plugin UI module rather than a core plugin module. For example, the CSV spreadsheet-import wizard implements a UI SPI and belongs in the import plugin UI module. Remaining UI such as the post-import report panel belongs in a desktop module.

### Controllers and models

Controllers are singleton services obtained through Lookup:

```java
ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
```

Use controllers as the entry point for executing module functionality and altering models. Controllers contain setters and retrieve models.

Models contain state and data and expose getters. There is one model of each relevant kind per workspace. Models also supply state to persistence providers for serialization into and restoration from `.gephi` project files.

## Branch and pull-request workflow

### Day-to-day development

The normal flow uses the master branch and feature branches:

```text
feature branch ──► pull request ──► build and test ──► merge
master commit ────────────────────► build and test
```

A commit to master triggers the GitHub Actions **build and test** workflow. It builds Gephi and runs all tests. A successful run indicates that a regression has most likely not been introduced.

A feature is developed on a feature branch and submitted through a pull request. Pull requests follow the same build-and-test path; when the tests pass, the pull request can be merged.

### PR format

Opening a PR on GitHub auto-populates it from [`.github/pull_request_template.md`](.github/pull_request_template.md) — use that template, don't replace it with free-form text.

- **Title**: `ISSUE_NUMBER DESCRIPTIVE_SUMMARY`, e.g. `#1299 Fix issue with edge weight`. Omit the issue number if none exists.
- Keep PRs small; split large changes rather than bundling unrelated work.
- Fill in the **Description** section and link the GitHub issue if one exists.
- Check the **Checklist**: confirm you've merged with master beforehand.
- Check **Added tests?** honestly — if no, state why they aren't needed.
- Check **Added to documentation?** for each box that applies: `README.md`, the [API changelog](CONTRIBUTING.md#api-changelog) (`src/main/javadoc/overview.html`), the separate [gephi-documentation](https://github.com/gephi/gephi-documentation) repo, and inline code documentation.

## Releases

Releasing Gephi is almost entirely automated through GitHub Actions. The only manual steps are:

1. Change the version in the `pom.xml` files, including moving from a snapshot version to a non-snapshot version when creating a final release.
2. Create the release on GitHub.

Everything else in the release process is automated.

### Release branches

A release branch, such as `0.9.7`, uses the **release** GitHub Actions workflow, which:

- Builds Gephi.
- Runs all tests.
- Produces release artifacts.
- Deploys the artifacts to Maven Central.
- Updates the NetBeans AutoUpdate site.

The build-and-test portion overlaps with the normal master and pull-request workflow; artifact production and AutoUpdate-site updates are release-specific.

### Development and final releases

Development and final releases use the same release process. The Maven version determines which type is produced:

- A version ending in `-SNAPSHOT` is a development release. Development releases can be produced repeatedly; their incremented build identifiers provide the latest binaries.
- A version without `-SNAPSHOT` is a final release. A final release cannot be overwritten — after publishing `0.9.7`, another final release must use a new version such as `0.9.8`.

A commit to a release branch whose POM files do not contain `-SNAPSHOT` triggers publication of the final version.

### Artifacts and AutoUpdate

The release workflow publishes all Gephi artifacts to Maven Central. These include:

- Each module's JAR.
- Each module's NBM, the NetBeans module file format.
- Application binaries for Linux, Windows, macOS, and the other supported OS and architecture combinations.

**Publishing is a single merged bundle, not five independent uploads.** Each matrix job stages its artifacts locally instead of publishing them directly; a separate `publish-central` job then downloads every job's staged output, merges it into one bundle, and uploads that as a single unit. This isn't an arbitrary design choice — Maven Central's Publisher Portal requires one atomic upload per component version, so the matrix jobs can't each publish on their own.

The AutoUpdate site is also updated. Gephi queries an XML file hosted on the project's GitHub Pages site, which lists all required modules and their current versions. The release workflow updates the XML and module paths so an older Gephi installation can detect a newer version and prompt the user to update without reinstalling the application. Commits that update the AutoUpdate site are produced by GitHub Actions.

The AutoUpdate URLs themselves are injected into `DesktopBranding`'s `layer.xml` via Maven resource filtering at build time — see the exclusion comment in `modules/DesktopBranding/pom.xml`, added after a `maven-resources-plugin` regression once silently overwrote the filtered file with unfiltered placeholder URLs, with no build failure. If you touch that module's resource or build configuration, double-check the built `layer.xml` still contains real URLs.

### Matrix build

The release uses a matrix build with one job for each supported operating-system and architecture combination — currently five: Linux x64, Linux aarch64, Windows x64, macOS x64, and macOS aarch64. Additional combinations can be added as operating systems and architectures develop.

Release builds run without Maven's `-T 4` parallel-build flag used elsewhere in this guide — parallel builds proved unreliable for release deploys, so the release workflow builds single-threaded.

### Embedded JRE

Each OS and architecture artifact embeds a JRE. The release workflow downloads the corresponding current JRE from trusted sources and packages it into the binary. For example, a 64-bit Windows artifact receives the appropriate Windows JRE. Users therefore do not need to install Java before running Gephi.

### macOS

The macOS workflow performs additional security steps:

- Code signing
- App notarization

App notarization sends the final binary to Apple for verification. The workflow waits up to 15 minutes for the result (configurable via the `gephi.apple.notarization.timeout` property). When verification succeeds, macOS Gatekeeper allows Gephi to run without warning that it may be spyware. These steps are automated in the release process.

### Windows

The Windows workflow creates an installer using Inno Setup, a popular open-source installer platform. The repository contains an installer-configuration file that can be modified. The release workflow runs Inno Setup and produces the final installer.

The Windows binary is code-signed via eSigner, a cloud HSM service authenticated with a username, password, and TOTP secret, rather than a locally-held PFX certificate — a change made when the certificate-based approach became impractical to maintain in CI.

### Lessons from past releases

A few things learned from recent releases (0.11.0 through 0.11.2) are worth keeping in mind:

- **Sanity-check the flattened POM before tagging.** 0.11.0 needed an emergency same-day patch (0.11.1) because the flattened POM produced for release was missing required descriptors — `groupId`, license, SCM, and developer metadata — that only get validated at actual release/publish time, not during a normal `mvn install`. If you change anything around POM structure or the `application` module's packaging, test it against a release-shaped build first.
- **Budget for a stabilization patch about a week out.** 0.11.2 shipped a week after 0.11.0/0.11.1 with over a dozen crash fixes, each referencing a Sentry issue that only surfaced once the release reached a wide user base. Watch Sentry closely in the days following a release rather than treating post-release crash reports as exceptional.
- **Release scripts run on Linux, macOS, and Windows runners — keep shell code portable.** A GNU-only `grep -oP` in a release-summary step worked on Linux but silently broke on macOS's BSD `grep`; it was replaced with `sed`. Avoid GNU-only flags in anything under `.github/workflows/release/`.
- **The workflow branch triggers are hardcoded, not pattern-based.** `release.yml` only runs on an explicit `branches:` allow-list (currently just `0.11.3`), while `build.yml` excludes those same release branches via `branches-ignore:` so they aren't built twice. Both lists need updating by hand — add the new branch to `release.yml` and to `build.yml`'s ignore list when starting a release, and prune old entries once a branch is no longer active.

## Compatibility expectations

Gephi uses semantic versioning for API changes:

- **Patch (`0.11.x`)**: minimal API changes; a method may be added.
- **Minor (`0.x.x`)**: API additions and occasional compatibility breaks when necessary, especially for APIs under development.
- **Major (`x.x.x`)**: APIs may be rewritten from scratch with major changes.

The intended direction is to:

- keep each feature's API clean, stable, and documented;
- allow implementations to be replaced;
- add new features through plugins;
- keep core functionality usable without the UI;
- keep modules reusable by other projects through Maven Central.

## Further reading

### NetBeans Platform

- NetBeans APIs Documentation
- Source code
- FAQ
- Documentation
- Tutorials
- API versus SPI: <https://netbeans.apache.org/wiki/DevFaqApiSpi.asciidoc>
- Lookup: <https://netbeans.apache.org/wiki/DevFaqLookup.asciidoc>

### Conventions and examples

- Code Style
- Localization
- Documentation
- Coding via examples
- Plugins Bootcamp
- Toolkit Demos

### API design

- [*Practical API Design: Confessions of a Java Framework Architect*](https://wiki.apidesign.org/wiki/TheAPIBook)
