# Powerwall

This tool runs at home, connecting to your Powerwall and capturing metrics.
It then sends all data to Splunk over HEC.

The tool is based off [Apache Camel](https://camel.apache.org).

# Building

This project is built with Gradle, directly available in this repository as a standalone jar.

Run:
```bash
./gradlew build
```

The application is packaged as a zip file.

You can unzip and run it (Java 11+ required):

```bash
unzip build/distributions/powerwall-1.0-SNAPSHOT.unzip
./powerwall-1.0-SNAPSHOT/bin/powerwall
```

# Configuration
All parameters are passed in as environment variables.

| Name         | Description                                            |
|--------------|--------------------------------------------------------|
| TESLA_HOST   | The host name of the Tesla gateway in your system      |
| SPLUNK_URL   | The url to Splunk such as https://example.com:8088     |
| SPLUNK_TOKEN | The Splunk HEC token to authenticate incoming messages |
| SPLUNK_INDEX | The index to send data to                              |

# License
Copyright 2020-2021, Antoine Toulme

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.