nio-fs-provider
===============


- Latest Release on Maven Central: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/no.maddin.niofs/nio-fs/badge.svg?style=plastic)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22no.maddin.niofs%22)

- Latest Snapshot on Sonatype OSSRH: [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/no.maddin.niofs/nio-fs?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/no/maddin/niofs/nio-fs/)

- Build status: [![Github](https://github.com/maddingo/nio-fs-provider/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/maddingo/nio-fs-provider/actions/workflows/maven.yml?query=branch%3Amaster+)

- Snyk Status: [![Known Vulnerabilities](https://snyk.io/test/github/maddingo/nio-fs-provider/61f838dea1f59aff09699575f7dc95989a3836f3/badge.svg)](https://snyk.io/test/github/maddingo/nio-fs-provider/61f838dea1f59aff09699575f7dc95989a3836f3)

- Quality Gate Status: [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=maddingo_nio-fs-provider&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=maddingo_nio-fs-provider)

- Vulnerabilities: [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=maddingo_nio-fs-provider&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=maddingo_nio-fs-provider)

- Reliability Rating: [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=maddingo_nio-fs-provider&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=maddingo_nio-fs-provider)

- Maintainability Rating: [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=maddingo_nio-fs-provider&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=maddingo_nio-fs-provider)
  
- Documentation: http://maddingo.github.io/nio-fs-provider

FileSystemProviders for java.nio introduced in Java 7.

It allows File system operation agnostic to the underlying implementation,
much like Apache VFS, but now in standard Java.

__NB:__ The project has moved in Maven Central.
This library was previously published with groupId `no.uis.nio` in Maven Central. Version 1.1.7 is the last version under this groupId.

Newer versions are published with groupId `no.maddin.niofs`.

-----------
[![](https://codescene.io/projects/3651/status.svg) Get more details at **codescene.io**.](https://codescene.io/projects/3651/jobs/latest-successful/results)

------------
# Deployment
local deployment
```bash
mvn clean deploy -P local
```

Sonatype OSSRH deployment
```bash
mvn clean deploy -P sonatype-oss
```
- https://central.sonatype.org/publish/publish-maven/#distribution-management-and-authentication
- https://central.sonatype.org/publish/generate-token/
