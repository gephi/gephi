# Gephi - The Open Graph Viz Platform

[![Build Status](https://travis-ci.org/gephi/gephi.svg?branch=master)](https://travis-ci.org/gephi/gephi)

[Gephi](http://gephi.org) is an award-winning open-source platform for visualizing and manipulating large graphs. It runs on Windows, Mac OS X and Linux. Localization is available in French, Spanish, Japanese, Russian, Brazilian Portuguese, Chinese and Czech.

- **Fast** Powered by a built-in OpenGL engine, Gephi is able to push the envelope with very large networks. Visualize networks up to a million elements. All actions (e.g. layout, filter, drag) run in real-time.

- **Simple** Easy to install and [get started](https://gephi.github.io/users/quick-start). An UI that is centered around the visualization. Like Photoshop™ for graphs.

- **Modular** Extend Gephi with [plug-ins](https://marketplace.gephi.org). The architecture is built on top of Netbeans Platform and can be extended or reused easily through well-written APIs.

[Download Gephi](https://gephi.github.io/users/download) for Windows, Mac OS X and Linux and consult the [release notes](https://github.com/gephi/gephi/wiki/Releases). Example datasets can be found on our [wiki](https://github.com/gephi/gephi/wiki/Datasets).

![Gephi](https://gephi.github.io/images/screenshots/select-tool-mini.png)

## Install and use Gephi

Download and [Install](https://gephi.github.io/users/install/) Gephi on your computer. 

Get started with the [Quick Start](https://gephi.github.io/users/quick-start/) and follow the [Tutorials](https://gephi.github.io/users/). Load a sample [dataset](https://github.com/gephi/gephi/wiki/Datasets) and start to play with the data.

If you run into any trouble or have questions consult our [forum](http://forum.gephi.org).

## Latest releases

### Stable

- Latest stable release on [gephi.org](https://gephi.github.io/users/download/).

### Nightly builds

Current version is 0.9.2-SNAPSHOT

- [gephi-0.9.2-SNAPSHOT-windows.exe](http://oss.sonatype.org/service/local/artifact/maven/content?r=snapshots&g=org.gephi&a=gephi&v=0.9.2-SNAPSHOT&c=windows&p=exe) (Windows)

- [gephi-0.9.2-SNAPSHOT-macos.dmg](http://oss.sonatype.org/service/local/artifact/maven/content?r=snapshots&g=org.gephi&a=gephi&v=0.9.2-SNAPSHOT&c=macos&p=dmg) (Mac OS X)

- [gephi-0.9.2-SNAPSHOT-linux.tar.gz](https://oss.sonatype.org/service/local/artifact/maven/content?r=snapshots&g=org.gephi&a=gephi&v=0.9.2-SNAPSHOT&c=linux&p=tar.gz) (Linux)

- [gephi-0.9.2-SNAPSHOT-sources.tar.gz](https://oss.sonatype.org/service/local/artifact/maven/content?r=snapshots&g=org.gephi&a=gephi&v=0.9.2-SNAPSHOT&c=sources&p=tar.gz) (Sources)

## Developer Introduction

Gephi is developed in Java and uses OpenGL for its visualization engine. Built on the top of Netbeans Platform, it follows a loosely-coupled, modular architecture philosophy. Gephi is split into modules, which depend on other modules through well-written APIs. Plugins can reuse existing APIs, create new services and even replace a default implementation with a new one.

Consult the [**Javadoc**](http://gephi.github.io/gephi/0.9.1/apidocs/index.html) for an overview of the APIs.

### Requirements

- Java JDK 7 or 8 with preferably [Oracle Java JDK](http://java.com/en/).

- [Apache Maven](http://maven.apache.org/) version 3.2.2 or later

### Checkout and Build the sources

- Fork the repository and clone

        git clone git@github.com:username/gephi.git

- Run the following command or [open the project in Netbeans](https://github.com/gephi/gephi/wiki/How-to-build-Gephi)

        mvn clean install

- Once built, one can test running Gephi

		cd modules/application
		mvn nbm:cluster-app nbm:run-platform

### Create Plug-ins

Gephi is extensible and lets developers create plug-ins to add new features, or to modify existing features. For example, you can create a new layout algorithm, add a metric, create a filter or a tool, support a new file format or database, or modify the visualization.

- [**Plugins Portal**](https://github.com/gephi/gephi/wiki/Plugins)

- [Plugins Quick Start (5 minutes)](https://github.com/gephi/gephi/wiki/Plugin-Quick-Start)

- Browse the [plugins](https://marketplace.gephi.org/) created by the community

- We've created a [**Plugins Bootcamp**](https://github.com/gephi/gephi-plugins-bootcamp) to learn by examples.

## Gephi Toolkit

The Gephi Toolkit project packages essential Gephi modules (Graph, Layout, Filters, IO…) in a standard Java library which any Java project can use for getting things done. It can be used on a server or command-line tool to do the same things Gephi does but automatically.

- [Download](http://gephi.github.io/toolkit/)

- [GitHub Project](https://github.com/gephi/gephi-toolkit)

- [Toolkit Portal](https://github.com/gephi/gephi/wiki/Toolkit)

## License

Gephi main source code is distributed under the dual license [CDDL 1.0](http://www.opensource.org/licenses/CDDL-1.0) and [GNU General Public License v3](http://www.gnu.org/licenses/gpl.html). Read the [Legal FAQs](http://gephi.github.io/legal/faq/)  to learn more.
	
Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
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
