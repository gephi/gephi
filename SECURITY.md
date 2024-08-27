# Security Policy

## Introduction

Before diving into the details, it's important to note that Gephi is a **desktop software**.
- It only interacts with local files. It **does NOT connect to the web** or to any networks.
- Gephi **does NOT have any servers** and **does NOT host any data**.

All of the data you load into Gephi only resides in your computer's memory and hard drive. As a result, Gephi is much less exposed to security problems compared to web software for instance. Please take that in account while evaluating the policy below.

## Supported Versions

We monitor security vulnerabilities in the dependencies we use, as well as within the core codebase. In case of a major vulnerability, we would release a new patch version of Gephi to address it.

| Version | Supported          |
| ------- | ------------------ |
| 0.10.1   | :white_check_mark: |
| 0.9.7   | :x:                |

## Code security

Gephi's codebase is entirely open-source and [available on GitHub](https://github.com/gephi/gephi/). The project had always restricted commit permissions on its `main` branch and stable contributors. No external organisations are responsible for auditing the codebase.

Gephi has numerous dependencies, listed in the projects' `pom.xml` files. It only depends on other open-source projects. There are no dependencies on closed sources. If you're having a hard-time locating the source code of some of our dependencies, don't hesitate to reach out.

In addition, we have enabled **dependabot** to get informed about security vulnerabilities in our dependencies.

## Contributors

Gephi had contributions from many individuals in many countries but the vast majority of the code was written by less than 5 people. We have CLA agreements with all major contributors. The core contributors are from France and Spain.

## Release versions

The artifacts produced via the Gephi repository are secured. Users can always trust us with the release binaries they download from [gephi.or](https://gephi.org) or [https://github.com/gephi/gephi/releases](https://github.com/gephi/gephi/releases). 

**These measures are in place to ensure Gephi artifacts are safe and can't be compromised:**
- Only the members of the core Gephi team can approve contributions and trigger releases.
- The release process is [completely automated](https://github.com/gephi/gephi/actions/workflows/release.yml) via GitHub Actions and doesn't require any interactions with a developer's local computer.
- Binaries are directly uploaded from GitHub Actions to [Maven Central](https://central.sonatype.com/artifact/org.gephi/gephi/overview). Only our project can push artifacts to the `org.gephi` groupId. As you may know, once a file is released on Maven Central, it can't be altered.
- Digital signatures for the release binaries are also available on Maven Central.
- The Mac OS app goes through a thorough [notarisation process](https://developer.apple.com/documentation/security/notarizing-macos-software-before-distribution) before being released. This means the package is sent to Apple's servers for verification and only if approved it can be released.
- The Windows installer is also codesigned using an official certificate.

## Vulnerabilities

Here is a history of security/data vulnerabilities:

| Name | Type | Severity | Description | Reported | Fixed |
| ---- | ---- | -------- | ----------- | -------- | ----- |
| PII in Crash Reports | Data privacy | Low | In the application logs it's possible to identify the username. The crash reports are voluntary, but it would be better to anonymize them. | October 2021 | ✅ Yes, in version 0.9.3 [#2340](https://github.com/gephi/gephi/issues/2340) |
| Log4j vulnerability | Dependency vulnerability | N/A | Gephi doesn't depend on Log4j, we weren't affected. | December 2021 | N/A |

## Reporting a Vulnerability

In case you find a vulnerability or want to get in touch regarding security, reach out to us at contact [at] gephi.org. Alternatively, you can [directly report via GitHub](https://github.com/gephi/gephi/security/advisories/new).

Based on your analysis, we'll evaluate if there are any risks for our users. In case we decide to release a patched version, you'll be informed.

We don't offer any rewards. If you provide a PR or a patch with your report, we'll be happy to include you in the release notes (if you desire so).

## Gephi Plugins

[Plugins](https://gephi.org/plugins/#/) are extensions users can install within Gephi to extend its functionalities. Plugins are made available within Gephi via an approval process [detailed here](https://github.com/gephi/gephi-plugins). 

**These measures are in place to avoid malicious code from Plugins:**
- Plugin contributors have to make PRs to the repository we control. Plugins artifacts can only be built from our repository. You can [inspect the codebase](https://github.com/gephi/gephi-plugins/tree/master-forge).
- Plugin artifacts are hosted on GitHub on [our page](https://github.com/gephi/gephi-plugins/tree/gh-pages/plugins) branch. There is full visibility. These are the plugins files downloaded from Gephi.
- Plugin packages (i.e. NBM files) are signed.
- We only accept open-source dependencies in plugins.

That all said, be aware that Gephi Plugins can pose a risk:
- Plugins can be packaged and distributed as NBMs files. Only the plugins available via Gephi are officially approved. If you manually install (e.g. via a NBM file you have received), you do that at your own risk.
- Some Plugins may require network access. We recommend you to review the source code of the plugins you install.