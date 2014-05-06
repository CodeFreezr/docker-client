# docker-client

A Docker client written in Groovy

[![Build Status](https://travis-ci.org/gesellix-docker/docker-client.svg)](https://travis-ci.org/gesellix-docker/docker-client)
[![Coverage Status](https://coveralls.io/repos/gesellix-docker/docker-client/badge.png)](https://coveralls.io/r/gesellix-docker/docker-client)


## Usage

For use in Gradle, add the bintray repository first:

```
repositories {
  maven { url 'http://dl.bintray.com/gesellix/docker-utils' }
}
```

Then, you need to add the dependency:

```
dependencies {
  compile 'de.gesellix:docker-client:2014-05-06T12-48-16'
}
```

Since Gradle uses an older Groovy release as the Docker client, you need to manually hack the classpath. You can find an example at the [gradle-docker-plugin](https://github.com/gesellix-docker/gradle-docker-plugin/blob/master/build.gradle).

The tests in `DockerClientImplSpec` should give you an idea how to use the docker-client.
