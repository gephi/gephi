<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
# macOS NetBeans Platform Launcher

[Native macOS NetBeans Platform launcher](https://github.com/apache/netbeans/tree/master/harness/apisupport.harness/macosx-launcher-src) for app bundle. It wraps a call to `platform/lib/nbexec`
* Requires macOS with swift to build


Manually build with the following command:
```shell
% swift build -c release
```

Binaries are stored in the `Binaries` folder.