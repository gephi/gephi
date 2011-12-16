# Gephi - The Open Graph Viz Platorm

[Gephi](http://gephi.org) is an open-source platform for visualizing and manipulating large graphs. It runs on Windows, Mac OS X and Linux. Localization is available in French, Spanish, Japanese and Brazilian Portuguese.

- **Fast** Powered by a built-in OpenGL engine, Gephi is pushing the envelope on very large networks. Visualize networks up to a million elements. All actions (e.g. layout, filter, drag) run in real-time.

- **Simple** Easy to install and [get started](http://gephi.org/users/quick-start/). An UI without scripts which and centered around the visualization. Like Photoshop™ for graphs.

- **Modular** Extend Gephi with [plug-ins](http://gephi.org/plugins/). The architecture is built on top of Netbeans Platform and can be extended or reused easily through well-writen APIs.

[Download Gephi](http://gephi.org/users/download/) for Windows, Mac OS X and Linux and consult the [release notes](https://wiki.gephi.org/index.php/Gephi_Releases). Example datasets can be found on our [wiki](https://wiki.gephi.org/index.php?title=Datasets).

![Gephi](http://gephi.org/wp-content/themes/gephi/images/screenshots/select-tool-mini.png)

## Install and use Gephi

Download and [Install](http://gephi.org/users/install) Gephi on your computer. 

Get started with the [Quick Start](http://gephi.org/users/quick-start/) and follow the [Tutorials](http://gephi.org/users/). Load a sample [dataset]((https://wiki.gephi.org/index.php?title=Datasets)) and start to plat with the data.

If you run into any trouble or have questions consult our [forum](http://forum.gephi.org).

## Developer Introduction

Gephi is developed in Java and use OpenGL for its visualization engine. Built on the top of Netbeans Platform, it follows a loosely-coupled, modular architecture philosophy. That permits to build large applications and make it grow in a sustainable way. Gephi is spitted into modules, which depend on each other through well-written APIs. Plugins are allowed to reuse existing APIs, create new services and even replace a default implementation by a new one.

Consult the [**Javadoc**](http://gephi.org/docs) for an overview of the APIs.

### Checkout and Build the sources

- Fork the repository and clone

        git clone git@github.com:username/gephi.git

- Run ant or [open the project in Netbeans](http://wiki.gephi.org/index.php/Build_Gephi)

        ant

### Create Plug-ins

Gephi is extensible and lets can create plug-ins to add new or modify existing features. You can create a new layout algorithm, a metric, support a new file format or database, create a filter, a tool or modify the visualization.

- [**Plugins Portal**](http://wiki.gephi.org/index.php/Plugins_portal)

- [Plugins Quick Start (5 minutes)](http://wiki.gephi.org/index.php/Plugin_Quick_Start_(5_minutes))

- Browse the [plug-ins](http://gephi.org/plugins) created by the community

## Gephi Toolkit

Gephi is not only a desktop software, it's also a Java library named [Gephi Toolkit](http://gephi.org/toolkit/).

The project packagea essential modules (Graph, Layout, Filters, IO…) in a standard Java library, which any Java project can use for getting things done. The toolkit is just a single JAR that anyone could reuse in new Java applications and achieve tasks that can be done in Gephi automatically, from a command-line program for instance.

- [Download](http://gephi.org/toolkit/)

- Build from the Gephi sources

        ant toolkit

- [Javadoc](http://gephi.org/docs/toolkit/)

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
