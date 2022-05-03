# Gephi - The Open Graph Viz Platform

[![build](https://github.com/gephi/gephi/actions/workflows/build.yml/badge.svg)](https://github.com/gephi/gephi/actions/workflows/build.yml)
[![Downloads](https://img.shields.io/github/downloads/gephi/gephi/v0.9.5/total.svg)](https://github.com/gephi/gephi/releases/tag/v0.9.5)
[![Downloads](https://img.shields.io/github/downloads/gephi/gephi/total.svg)](https://github.com/gephi/gephi/releases/)
[![Translation progress](https://hosted.weblate.org/widgets/gephi/-/svg-badge.svg)](https://hosted.weblate.org/engage/gephi/?utm_source=widget)

[Gephi](http://gephi.org) is an award-winning open-source platform for visualizing and manipulating large graphs. It runs on Windows, Mac OS X and Linux. Localization is available in English, French, Spanish, Japanese, Russian, Brazilian Portuguese, Chinese, Czech, German and Romanian.

- **Fast** Powered by a built-in OpenGL engine, Gephi is able to push the envelope with very large networks. Visualize networks up to a million elements. All actions (e.g. layout, filter, drag) run in real-time.

- **Simple** Easy to install and [get started](https://gephi.github.io/users/quick-start). An UI that is centered around the visualization. Like Photoshop™ for graphs.

- **Modular** Extend Gephi with [plug-ins](https://gephi.org/plugins). The architecture is built on top of [Apache Netbeans Platform](https://netbeans.apache.org/tutorials/nbm-quick-start.html) and can be extended or reused easily through well-written APIs.

[Download Gephi](https://gephi.github.io/users/download) for Windows, Mac OS X and Linux and consult the [release notes](https://github.com/gephi/gephi/wiki/Releases). Example datasets can be found on our [wiki](https://github.com/gephi/gephi/wiki/Datasets).

![Gephi](https://gephi.github.io/images/screenshots/select-tool-mini.png)

## Install and use Gephi

Download and [Install](https://gephi.github.io/users/install/) Gephi on your computer. 

Get started with the [Quick Start](https://gephi.github.io/users/quick-start/) and follow the [Tutorials](https://gephi.github.io/users/). Load a sample [dataset](https://github.com/gephi/gephi/wiki/Datasets) and start to play with the data.

If you run into any trouble or have questions consult our [forum](http://forum-gephi.org/).

## Latest releases

### Stable

- Latest stable release on [gephi.org](https://gephi.org/users/download/).

### Nightly builds

Current version is 0.9.6-SNAPSHOT

- [gephi-0.9.6-SNAPSHOT-windows-x64.exe](https://oss.sonatype.org/service/local/artifact/maven/content?r=snapshots&g=org.gephi&a=gephi&v=0.9.6-SNAPSHOT&c=windows-x64&p=exe) (Windows)

- [gephi-0.9.6-SNAPSHOT-windows-x32.exe](https://oss.sonatype.org/service/local/artifact/maven/content?r=snapshots&g=org.gephi&a=gephi&v=0.9.6-SNAPSHOT&c=windows-x32&p=exe) (Windows x32)

- [gephi-0.9.6-SNAPSHOT-macos-x64.dmg](https://oss.sonatype.org/service/local/artifact/maven/content?r=snapshots&g=org.gephi&a=gephi&v=0.9.6-SNAPSHOT&c=macos-x64&p=dmg) (Mac OS X)

- [gephi-0.9.6-SNAPSHOT-linux-x64.tar.gz](https://oss.sonatype.org/service/local/artifact/maven/content?r=snapshots&g=org.gephi&a=gephi&v=0.9.6-SNAPSHOT&c=linux-x64&p=tar.gz) (Linux)

- [gephi-0.9.6-SNAPSHOT-sources.tar.gz](https://oss.sonatype.org/service/local/artifact/maven/content?r=snapshots&g=org.gephi&a=gephi&v=0.9.6-SNAPSHOT&c=sources&p=tar.gz) (Sources)

## Developer Introduction

Gephi is developed in Java and uses OpenGL for its visualization engine. Built on the top of Netbeans Platform, it follows a loosely-coupled, modular architecture philosophy. Gephi is split into modules, which depend on other modules through well-written APIs. Plugins can reuse existing APIs, create new services and even replace a default implementation with a new one.

Consult the [**Javadoc**](http://gephi.github.io/gephi/0.9.2/apidocs/index.html) for an overview of the APIs.

### Requirements

- Java JDK 11 (or later)

- [Apache Maven](http://maven.apache.org/) version 3.6.3 or later

### Checkout and Build the sources

- Fork the repository and clone

        git clone git@github.com:username/gephi.git

- Run the following command or [open the project in an IDE](https://github.com/gephi/gephi/wiki/How-to-build-Gephi)

        mvn -T 4 clean install

- Once built, one can test running Gephi

		cd modules/application
		mvn nbm:cluster-app nbm:run-platform

### Create Plug-ins

Gephi is extensible and lets developers create plug-ins to add new features, or to modify existing features. For example, you can create a new layout algorithm, add a metric, create a filter or a tool, support a new file format or database, or modify the visualization.

- [**Plugins Portal**](https://github.com/gephi/gephi/wiki/Plugins)

- [Plugins Quick Start (5 minutes)](https://github.com/gephi/gephi/wiki/Plugin-Quick-Start)

- Browse the [plugins](https://gephi.org/plugins) created by the community

- We've created a [**Plugins Bootcamp**](https://github.com/gephi/gephi-plugins-bootcamp) to learn by examples.

## Gephi Toolkit

The Gephi Toolkit project packages essential Gephi modules (Graph, Layout, Filters, IO…) in a standard Java library which any Java project can use for getting things done. It can be used on a server or command-line tool to do the same things Gephi does but automatically.

- [Download](https://gephi.org/toolkit/)

- [GitHub Project](https://github.com/gephi/gephi-toolkit)

- [Toolkit Portal](https://github.com/gephi/gephi/wiki/Toolkit)

## Localization

We use [Weblate](https://hosted.weblate.org/projects/gephi/) for localization. Follow the guidelines on the [wiki](https://github.com/gephi/gephi/wiki/Localization) for more details how to contribute.

## License

Gephi main source code is distributed under the dual license [CDDL 1.0](http://www.opensource.org/licenses/CDDL-1.0) and [GNU General Public License v3](http://www.gnu.org/licenses/gpl.html). Read the [Legal FAQs](http://gephi.github.io/legal/faq/)  to learn more.
	
Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License ("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.github.io/developers/license/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
