# Gephi - The Open Graph Viz Platform

[Gephi](http://gephi.org) is an award-winning open-source platform for visualizing and manipulating large graphs. It runs on Windows, Mac OS X and Linux. Localization is available in French, Spanish, Japanese, Russian, Brazilian Portuguese, Chinese and Czech.

- **Fast** Powered by a built-in OpenGL engine, Gephi is able to push the envelope with very large networks. Visualize networks up to a million elements. All actions (e.g. layout, filter, drag) run in real-time.

- **Simple** Easy to install and [get started](http://gephi.org/users/quick-start/). An UI that is centered around the visualization. Like Photoshop™ for graphs.

- **Modular** Extend Gephi with [plug-ins](http://gephi.org/plugins/). The architecture is built on top of Netbeans Platform and can be extended or reused easily through well-written APIs.

[Download Gephi](http://gephi.org/users/download/) for Windows, Mac OS X and Linux and consult the [release notes](https://wiki.gephi.org/index.php/Gephi_Releases). Example datasets can be found on our [wiki](https://wiki.gephi.org/index.php?title=Datasets).

![Gephi](http://gephi.org/wp-content/themes/gephi/images/screenshots/select-tool-mini.png)

## Install and use Gephi

Download and [Install](http://gephi.org/users/install) Gephi on your computer. 

Get started with the [Quick Start](http://gephi.org/users/quick-start/) and follow the [Tutorials](http://gephi.org/users/). Load a sample [dataset](https://wiki.gephi.org/index.php?title=Datasets) and start to play with the data.

If you run into any trouble or have questions consult our [forum](http://forum.gephi.org).

## Latest releases

### Stable

- Latest stable release on [gephi.org](http://gephi.org/download).

### Nightly builds

Current version is 0.9-SNAPSHOT

- [gephi-0.9-SNAPSHOT.zip](http://nexus.gephi.org/nexus/service/local/artifact/maven/content?r=snapshots&g=org.gephi&a=gephi&v=0.9-SNAPSHOT&p=zip) (Windows & Linux)

- [gephi-0.9-SNAPSHOT.dmg](http://nexus.gephi.org/nexus/service/local/artifact/maven/content?r=snapshots&g=org.gephi&a=gephi&v=0.9-SNAPSHOT&p=dmg) (Mac OS X)

- [gephi-0.9-SNAPSHOT-sources.tar.gz](http://nexus.gephi.org/nexus/service/local/artifact/maven/redirect?r=snapshots&g=org.gephi&a=gephi-parent&v=0.9-SNAPSHOT&c=sources&p=tar.gz) (Sources)

- [gephi-0.9-SNAPSHOT-javadoc.jar](http://nexus.gephi.org/nexus/service/local/artifact/maven/redirect?r=snapshots&g=org.gephi&a=gephi-parent&v=0.9-SNAPSHOT&c=javadoc&p=jar) (Javadoc)

## Developer Introduction

Gephi is developed in Java and uses OpenGL for its visualization engine. Built on the top of Netbeans Platform, it follows a loosely-coupled, modular architecture philosophy. That allows it to be used build large applications and to grow in a sustainable way. Gephi is split into modules, which depend on other modules through well-written APIs. Plugins can reuse existing APIs, create new services and even replace a default implementation with a new one.

Consult the [**Javadoc**](http://gephi.org/docs/api) for an overview of the APIs.

### Requirements

- Java JDK 6 or 7 with preferably [Oracle Java JDK](http://java.com/en/).

- [Apache Maven](http://maven.apache.org/) version 3.0.4 or later

### Checkout and Build the sources

- Fork the repository and clone

        git clone git@github.com:username/gephi.git

- Run the following command or [open the project in Netbeans](http://wiki.gephi.org/index.php/Build_Gephi)

        mvn clean install

- Once built, one can test running Gephi

		cd modules/application
		mvn nbm:cluster-app nbm:run-platform

### Create Plug-ins

Gephi is extensible and lets users create plug-ins to add new features, or to modify existing features. For example, you can create a new layout algorithm, add a metric, create a filter or a tool, support a new file format or database, or modify the visualization.

- [**Plugins Portal**](http://wiki.gephi.org/index.php/Plugins_portal)

- [Plugins Quick Start (5 minutes)](http://wiki.gephi.org/index.php/Plugin_Quick_Start_(5_minutes))

- Browse the [plugins](http://gephi.org/plugins) created by the community

- We've created a [**Plugins Bootcamp**](https://github.com/gephi/gephi-plugins-bootcamp) to learn by examples.

## Gephi Toolkit

The Gephi Toolkit project packages essential Gephi modules (Graph, Layout, Filters, IO…) in a standard Java library which any Java project can use for getting things done. It can be used on a server or command-line tool to do the same things Gephi does but automatically.

- [Download](http://gephi.org/toolkit/)

- [GitHub Project](https://github.com/gephi/gephi-toolkit)

- [Toolkit Portal](https://wiki.gephi.org/index.php/Toolkit_portal)

## License

Gephi main source code is distributed under the dual license [CDDL 1.0](http://www.opensource.org/licenses/CDDL-1.0) and [GNU General Public License v3](http://www.gnu.org/licenses/gpl.html). Read the [Legal FAQs](https://gephi.org/about/legal/faq/)  to learn more.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
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
