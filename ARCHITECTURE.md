# Gephi Architecture

## High-level architecture

Gephi is based on the Java programming language and currently uses JDK 17.

The software is layered as follows:

```text
Plugins                Plugins
   │                       │
   └──────────► Gephi UI ◄─┘
                    │
Plugin ───────► Core Gephi
                    │
            NetBeans Platform
                    │
                   Java
```

The NetBeans Platform provides the foundation for many Gephi features. It is also the foundation on which the NetBeans IDE is built. NetBeans APIs therefore appear throughout the Gephi codebase, and familiarity with the platform is useful when modifying Gephi.

Core Gephi modules are separated from user-interface modules. This separation makes it possible to build a command-line tool or library using only non-UI modules, without importing unnecessary user-interface dependencies. Gephi’s toolkit uses this approach.

Plugins can build on either core modules or UI modules. Gephi’s extensible architecture does not treat plugins as a separate kind of component: a plugin is another module.

## Design principles

### Modularity

Gephi is designed to be extensible.

- The codebase is broken down into 66 modules, per the root `pom.xml`'s `<modules>` list (not counting the `application` module itself).
- Modules define dependencies between one another.
- APIs and contracts separate modules.
- A developer can locate the module responsible for a feature, work within it, and avoid needing to understand the entire codebase.
- Plugins are nothing other than modules. Adding a plugin effectively adds another module to the software—for example, a 67th module alongside the 66 existing modules.

### Multithreading

Gephi is designed around a non-blocking user interface.

- Long-running tasks can execute in the background.
- Users can continue exploring a graph while algorithms run.
- The graph structure is protected through locking so it remains consistent.
- The UI reacts to data changes.

For example, deleting a node also requires deleting its associated edges. It would be inconsistent for one thread to delete a node while another thread could still read edges belonging to that deleted node. Locking prevents this. The in-memory graph structure behaves like a database with consistency controls: callers can rely on the graph being consistent at any point in time.

The multithreaded design extends to the user interface. The software assumes that a module or plugin may modify the graph at any time and that other modules will react to those changes. Code that changes the graph does not need to explicitly refresh each affected UI component; those components update automatically. This keeps modules separate and avoids a slow interface when many changes occur concurrently.

## Technologies

### Runtime and platform

- Java, currently JDK 17
- NetBeans Platform
  - Module system
  - Docking framework, including movable windows
  - Auto-update functionality
  - User-interface components

The NetBeans Platform is open source, has a community around it, and is used as the basis for other desktop software.

### User interface and rendering

- Swing for the user interface
- OpenGL for visualization
- Java2D for rendering in the Preview tab

### Build, testing, and automation

- Maven
- JUnit
- GitHub Actions

## Main APIs

Functionality is exposed through APIs that correspond to modules:

| API | Responsibility |
|---|---|
| Project API | Project management and workspaces |
| Graph API | Graph structure and access to graph data — the actual data structure implementation lives in the separate [graphstore](https://github.com/gephi/graphstore) repository; `modules/GraphAPI` here is a thin NetBeans-module wrapper around it (see note below) |
| Import API | Importing files and databases, including File Open workflows |
| Layout API | Layout algorithms |
| Filters API | Filters |
| Statistics API | Algorithms such as community detection and PageRank |
| Export API | Exporting files |
| Preview API | Rendering preview images |
| Appearance API | Partitioning, ranking, and transforming nodes and edges with colors and sizes |
| Tools API | Tools associated with the visualization, such as shortest path |
| Data Laboratory API | Manipulation of attributes and Data Laboratory operations |
| Visualization API | Functionality offered by the OpenGL engine |

APIs can depend on one another. For example, the Graph API depends on the Project API because graphs are associated with workspaces. Dependencies are declared as standard Maven dependencies in each module’s `pom.xml`. A new module dependency is added to that module’s POM.

**Core graph structure lives in a separate repository.** `modules/GraphAPI` in this repo only wraps
the `graphstore` library (pulled in as a Maven dependency, see the `graphstore.version` property in
the root `pom.xml`) as a NetBeans module and exposes `org.gephi.graph.api`/`org.gephi.graph.impl`.
The actual graph/node/edge data structure — the core of what Gephi manipulates — is implemented in
[github.com/gephi/graphstore](https://github.com/gephi/graphstore), a separate repository. Any
change to the core graph data structure itself (storage, indexing, node/edge/column implementation)
belongs there, not here; changes here should be limited to the NetBeans-integration layer
(`GraphControllerImpl`, `GraphPersistenceProvider`) and consumers of the API.

### API properties

Gephi APIs are plain Java interfaces.

They use the NetBeans module system’s public-package mechanism. A module marks selected packages as public in its `pom.xml`; only those packages are accessible to other modules. The public-package list may also be empty.

For example, the Import API module contains:

- A public API package, such as `org.gephi.io.importer.api`.
- An implementation package, such as `org.gephi.io.importer.impl`.
- Other module-internal code.

The API and its implementation can live in the same module, but only the API package is public. A module that depends on the Import API cannot access its implementation. This hides implementation details, preserves the API as a contract, and allows an implementation to be replaced later without consumers depending on its internals.

This public-package approach predates the module system introduced in Java 9 but provides a similar form of encapsulation through the NetBeans Platform.

Most APIs have reached a level of stability where backward compatibility is a goal. APIs may also be marked as under development while their entry points and contracts are being refined. The aim is for them eventually to become stable enough that plugins can rely on them over time.

The main APIs are documented through Javadoc. Complete documentation for all main APIs is the goal. Documentation quality varies in some places, but in most cases it provides a usable entry point. API and SPI changes are tracked in `src/main/javadoc/overview.html` (the main Javadoc overview page), providing a change log for developers migrating modules or plugins, including new methods and other changes. This file is also the source used to compile release notes, so entries need to follow its existing formatting convention — see [CONTRIBUTING.md](CONTRIBUTING.md#api-changelog) for the exact structure.

The architecture was heavily inspired by [*Practical API Design: Confessions of a Java Framework Architect*](https://wiki.apidesign.org/wiki/TheAPIBook), written by the architect of the NetBeans Platform. It is recommended as an introduction to API design.

## APIs and SPIs

SPI means **Service Provider Interface**.

APIs and SPIs share implementation properties: both are Java interfaces, both use public packages, both are intended to be backward compatible, and both should be clearly documented. Their roles differ, and APIs and SPIs are never mixed.

- APIs offer functionality to other modules.
- SPIs are meant to be extended.
- Plugins always extend an SPI.

For example, the Import API module can contain:

```text
Import API module
├── API: org.gephi.io.importer.api
├── implementation: org.gephi.io.importer.impl
└── SPI: org.gephi.io.importer.spi
    └── Importer
        ├── ImporterGEXF
        ├── ImporterGML
        ├── ImporterXXX
        └── implementation supplied by a plugin
```

An importer SPI defines the operations an importer must provide, such as execution. Core modules and plugins can implement it for GEXF, GML, or other formats.

### Available SPIs

| SPI | Extension point                                                       |
|---|-----------------------------------------------------------------------|
| Import SPI | File, database, and wizard importers                                  |
| Layout SPI | Layout algorithms                                                     |
| Statistics SPI | Other algorithms                                                      |
| Tools SPI | Tools in the menu bar                                                 |
| Project SPI | Persistence providers                                                 |
| Export SPI | File exporters for graphs and graphics                                |
| Filters SPI | Filters                                                               |
| Preview SPI | Preview builders and renderers                                        |
| Generator SPI | Generators, similar to importers                                      |
| Data Laboratory SPI | Manipulators                                                          |
| Appearance SPI | Transformers for ranking and partitioning                             |
| Visualization SPI | Renderers for the new `VisualizationEngine` module (work in progress) |

The Project SPI’s persistence providers make `.gephi` project files extensible. Any module can add its own data to a `.gephi` file by implementing a persistence provider.

The visualization engine revamp is underway: a new `VisualizationEngine` module now exists alongside the legacy `VisualizationImpl` module (the application currently depends on both), with its own `org.gephi.viz.engine.spi.Renderer` SPI. The Visualization SPI's functionality is still being expanded as that migration progresses.

The available SPIs define the kinds of plugins that can be created: a plugin can exist when it implements an SPI.

Resources supplied with the architecture material:

- API versus SPI: <https://netbeans.apache.org/wiki/main/netbeansdevelopperfaq/DevFaqApiSpi/>

## Lookup

Lookup is the most important NetBeans Platform utility in the Gephi architecture. It appears throughout the codebase and supports the API/implementation and SPI/implementation separations.

### Finding a singleton service

Controllers are obtained without accessing their implementations directly:

```java
ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
```

For example, code that needs to create, save, or otherwise manipulate projects obtains the `ProjectController` service through Lookup. This resembles dependency injection. The service is a singleton, and obtaining it through Lookup preserves the separation between the public API and its implementation.

### Finding every implementation of an SPI

Lookup can return every available implementation of an SPI:

```java
for (Renderer renderer : Lookup.getDefault().lookupAll(Renderer.class)) {
}
```

The returned collection can contain implementations from core modules and plugins. Implementations added to the runtime classpath are included without callers needing to know their packages.

This mechanism powers lists and trees of available extensions in the interface. A layout selector, for example, discovers every layout algorithm through `lookupAll`. The same pattern is used for exporters, renderers, filters, and other extension lists.

### Registering an implementation

SPI implementations must be registered with the service-provider annotation:

```java
@ServiceProvider(service = Renderer.class)
public class NodeRenderer implements Renderer {
}
```

The annotation states that the class supplies an implementation of the `Renderer` SPI. Lookup searches the classpath for these registrations. Implementing the interface without the annotation means that `lookupAll` will not discover the implementation.

A new layout, filter, renderer, or other extension is therefore added by:

1. Implementing the relevant SPI.
2. Adding the service-provider annotation.

Resources supplied with the architecture material:

- Lookup: <https://netbeans.apache.org/wiki/main/netbeansdevelopperfaq/DevFaqLookup/>

## API direction and compatibility

The architecture aims to preserve the following properties:

- Each feature has a clean, stable, documented API.
- Developers can discover an extension point through Javadoc without first reading deeply into the implementation.
- Implementations can be replaced without changing, or without substantially changing, the API.
- New features can be added through plugins.
- Core functionality can be used by command-line tools without UI modules.
- Modules can be reused in other projects.
- Gephi modules are published on Maven Central and can be selected individually, such as using only the graph structure or using the graph and layout modules in another library, cloud application, or third-party application.
- Core and UI modules remain separated, even when features require user input and UI components.

Gephi has rewritten modules internally multiple times while keeping their APIs unchanged or changing them only minimally. This remains an architectural goal.

The NetBeans Platform also makes it possible to replace a default implementation. A plugin could provide a new implementation—for example, a replacement `ProjectController`—and give it a higher priority so that Lookup selects it instead of the default. This is uncommon in practice, but it demonstrates that the platform supports replacing as well as extending functionality.

### Semantic versioning

Gephi uses semantic versioning to communicate API changes:

- **Patch (`0.11.x`)**: minimal API changes. A new method may be added to an API, but changes are otherwise limited.
- **Minor (`0.x.x`)**: API additions and occasional compatibility breaks when necessary, particularly for APIs marked under development.
- **Major (`x.x.x`)**: APIs may be rewritten from scratch with major changes, such as between `1.0` and `2.0`.

## Anatomy of a module

All modules follow the Maven-style structure:

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

The module’s `pom.xml` is particularly important. It contains configuration such as dependencies and public packages. Creating a new Gephi module produces this Maven-convention structure.

## Module and package conventions

Most modules follow four naming roles. Using import as the example:

| Module pattern | Role | Count among the 66 modules |
|---|---|---:|
| `ImportAPI` | Defines APIs and SPIs and implements APIs | 17 |
| `ImportPlugin` | SPI implementations | 11 |
| `ImportPluginUI` | UI-only SPI implementations | 6 |
| `DesktopImport` | Remaining UI code | 17 |

Together, 51 of the 66 modules follow this convention.

### API modules

An API module contains the API, its SPIs, and the API implementation. There are one or two exceptions. For example, the Filters implementation can still be found in a separate filters implementation module, though the intention is to place both APIs and implementations in the API module.

### Plugin modules

A plugin module contains SPI implementations. For import, this is where implementations such as the GEXF importer are found.

API and plugin modules are core Gephi modules and contain no user-interface code. They are the modules needed by a command-line application that imports the file formats supported by Gephi.

### Plugin UI modules

Some SPIs include UI components. An import or export implementation may need a panel that lets users configure it. UI implementations of those SPIs live in a plugin UI module so that core and UI modules remain separate.

For example, most graph formats do not need a configuration panel when imported. Spreadsheet import is an exception: it uses a full CSV import wizard, detects the content, and requires Swing UI code implementing the Importer UI SPI. That code belongs in the import plugin UI module.

### Desktop modules

Desktop modules contain remaining UI code. For import, the report panel shown after an import belongs in the desktop import module. That report displays the number of nodes, errors, and warnings.

The same convention applies across domains. Examples include:

- Layout API and Layout Plugin
- Preview API and Preview Plugin
- Statistics API, Statistics Plugin, Statistics Plugin UI, and Desktop Statistics

Modules that do not follow the four-role convention cover other functionality, such as the welcome screen (`WelcomeScreen`) and settings migration between versions (`SettingsUpgrader`).

### Package names

| Role | Package convention |
|---|---|
| API | `org.gephi.NAME.api` |
| API implementation | `org.gephi.NAME` |
| SPI | `org.gephi.NAME.spi` |
| SPI implementation | `org.gephi.NAME.plugin` |
| SPI implementation, UI only | `org.gephi.ui.NAME.plugin` |
| UI | `org.gephi.desktop.NAME` |

These conventions should be followed when creating modules or changing implementations so the codebase remains organized.

## Controllers and models

Gephi uses a simplified Model–Controller pattern rather than a full Model–View–Controller pattern; there is no architectural View component.

### Controllers

Controllers include `ProjectController`, `ImportController`, and `GraphController`.

- A controller is a singleton service found through Lookup.
- It is the entry point for interacting with and executing a module’s functionality.
- It is the entry point for any model alteration and contains setters.
- It retrieves the model.

### Models

- Models contain all state and data and expose getters.
- There is one model of each relevant kind per workspace.
- Models are the source for persistence providers.

A project can contain multiple independent workspaces. Each workspace has its own layout model, filter model, and other models. Configuration set in Workspace 1 changes when switching to Workspace 2, and the Workspace 1 configuration is restored when switching back.

```text
Project
├── Workspace 1
│   ├── Layout Model
│   ├── Filter Model
│   └── ...
└── Workspace 2
    ├── Layout Model
    ├── Filter Model
    └── ...
```

Models also provide the state serialized into `.gephi` project files. When saving, Gephi discovers all persistence-provider implementations. A layout persistence provider, for example, writes layout state into the file and restores it when the file is read.

In summary:

- Models provide the getters and contain the data.
- Controllers provide the setters and alter state and data.
- There is no View in the current architectural pattern.

## Repository structure

The repository is organized as follows:

```text
.github/
└── workflows/       # GitHub Actions workflows
modules/
├── application/     # Gephi application module (built last, depends on all others)
└── ...              # Source code for the other 66 modules
src/                  # Extra files, including the macOS launcher
pom.xml               # Parent POM containing shared configuration
```

### Application module

The application module is different from the other modules. In NetBeans Platform terminology, the repository contains modules and an application. Gephi has one application module, the Gephi application, which depends on all the other modules and is built last.

The application module contains mostly configuration rather than code, including branding-screen configuration, versions, and installer-related configuration. Another application using only a subset of Gephi modules would use a separate application module with only those dependencies.

### Root files

The root `src` folder contains extra files such as the macOS launcher and is not generally relevant to day-to-day development.

The root `pom.xml` is the parent POM for the multi-module Maven repository. Shared configuration is kept there whenever possible, including dependency versions and build configuration. Every module declares this POM as its parent and inherits its configuration. Module-specific configuration may live in a module’s own POM, but this is uncommon.