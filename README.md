




Codahale Extra
============

<a href="https://raw.githubusercontent.com/ArpNetworking/metrics-codahale-extra/master/LICENSE">
    <img src="https://img.shields.io/hexpm/l/plug.svg"
         alt="License: Apache 2">
</a>
<a href="https://build.arpnetworking.com/job/ArpNetworking/job/metrics-codahale-extra/job/master/">
    <img src="https://build.arpnetworking.com/job/ArpNetworking/job/metrics-codahale-extra/job/master/badge/icon"
         alt="Build Status">
</a>
<a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.arpnetworking.metrics.extras%22%20a%3A%22codahale-extra%22">
    <img src="https://img.shields.io/maven-central/v/com.arpnetworking.metrics.extras/codahale-extra.svg"
         alt="Maven Artifact">
</a>

Extension for clients migrating from Codahale metrics that allows migration to our client library while retaining publication to Codahale.


Instrumenting Your Codahale Application
---------------------------------------

### Add Dependency

Determine the latest version of the Codahale extra in [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.arpnetworking.metrics.extras%22%20a%3A%22codahale-extra%22).

#### Maven

Add a dependency to your pom:

```xml
<dependency>
    <groupId>com.arpnetworking.metrics.extras</groupId>
    <artifactId>codahale-extra</artifactId>
    <version>VERSION</version>
</dependency>
```

The Maven Central repository is included by default.

#### Gradle

Add a dependency to your build.gradle:

    compile group: 'com.arpnetworking.metrics.extras', name: 'codahale-extra', version: 'VERSION'

Add at least one of the Maven Central Repository and/or Local Repository into build.gradle:

    mavenCentral()
    mavenLocal()

#### Play

Add a dependency to your project/Build.scala:

```scala
val appDependencies = Seq(
    ...
    "com.arpnetworking.metrics.extras" % "codahale-extra" % "VERSION"
    ...
)
```

The Maven Central repository is included by default.  Alternatively, if using the local version add the local repository into project/plugins.sbt:

    resolvers += Resolver.mavenLocal

### Publishing via Codahale

This extra package contains classes that can be used to supplement Codahale output.  The first way this can be done is to use the codahale-extra package and record all
metrics against a MetricsRgistry in the com.arpnetworking.metrics.codahale package.  This will allow existing code written against the Codahale metrics interfaces to work
with ArpNetworking metrics, with the caveat that the use of static methods to create metrics is not supported.  This is the preferred method of using this library.

```java
final MetricsFactory metricsFactory = ...
final MetricsRegistry registry = new com.arpnetworking.metrics.codahale.MetricsFactory(metricsFactory);
Counter counter = registry.newCounter("foo");
counter.inc();
```

The other way to use this library is as a drop-in replacement.  Through the use of shading we have created the codahale-replace library that serves as a full
replacement for Codahale metrics.  This is recommended for times when it is not possible to modify existing code that uses Codahale metrics.  To use this method,
remove the metrics-core.jar from the classpath of the target application and add codahale-replace.jar in its place.  All use of Codahale metrics will instead be
sent to Arpnetworking metrics (including all static references).

To configure the metrics library when employing a drop-in replacement you should set these environment variables:

* METRICS_CODAHALE_EXTRA_CLUSTER - The name of the cluster that the running instance belong to.
* METRICS_CODAHALE_EXTRA_SERVICE - The name of the service that the running instance is reporting for.
* METRICS_CODAHALE_EXTRA_DIRECTORY - The directory to write the query log file to.

All the environment variables have defaults but it is highly recommended that the values be set to more appropriate values.  The defaults are:

* METRICS_CODAHALE_EXTRA_CLUSTER - CodahaleCluster
* METRICS_CODAHALE_EXTRA_SERVICE - CodahaleService
* METRICS_CODAHALE_EXTRA_DIRECTORY - /tmp

#### Differences

Codahale metrics provides some interfaces that ArpNetworking metrics does not.  For instance, there is no Meter in ArpNetworking metrics,
and counters act differently in Codahale than here.  This section will detail the differences.

##### Counter

Both Codahale and ArpNetworking metrics contain counters that can be incremented by 1 or any arbitrary number.  The difference is in
ArpNetworking metrics' separation of units of work and tracking individual samples.  This impedance mismatch is solved by storing each
call to Codahale's counter inc as a separate sample in ArpNetworking metrics.  This means that loops where inc() is called multiple times
will translate to multiple samples of '1'.  Normally this will not be a problem and the expected value of the Codahale metric will be in
the 'sum' statistic's value.  Note, however, that using Codahale metrics can lead to unintuitive sample distributions.

##### Timer

Timers in Codahale and ArpNetworking metrics are very similar.  Their use is functionally equivalent.  The only difference is in the
reporting of values.  Since ArpNetworking records individual samples, you will have access to statistically correct percentiles, min, max
and counts.

##### Meter

Meters exist in Codahale to record rates.  ArpNetworking metrics does not contain meters.  All meters are converted into counters.
Counters in ArpNetworking metrics allow for the computation of the rates that Codahale metrics produces due to the retention of samples.

##### Histograms

Histograms exist in Codahale to record percentiles.  ArpNetworking metrics retains samples and uses histograms to store the samples.  As a
result, histograms are converted to counters and provide the same statistics as Codahale histograms provide.

### Building

Prerequisites:
* [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

Building:

    metrics-codahale-extra> ./mvnw package

To use the local version you must first install it locally:

    metrics-codahale-extra> ./mvnw install

You can determine the version of the local build from the pom file.  Using the local version is intended only for testing or development.

You may also need to add the local repository to your build in order to pick-up the local version:

* Maven - Included by default.
* Gradle - Add *mavenLocal()* to *build.gradle* in the *repositories* block.
* SBT - Add *resolvers += Resolver.mavenLocal* into *project/plugins.sbt*.

License
-------

Published under Apache Software License 2.0, see LICENSE

&copy; Groupon Inc., 2014
