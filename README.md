---
 AI Contribution Weight: [ 90% ] AI Generated / [10%] Human Refactored
---
## Project Portal

A centralized campus-wide ecosystem designed to showcase, share and manage academic research, course capstones and
innovative student designs.

## Current implementation status

The checked-in application is currently a Java 17 console demo (`com.bu.Application`), not yet the Spring Boot API
described in [spec.md](spec.md). It prints a greeting and exits; there is no HTTP server or API available on port 8080.
The Dockerfile and Compose configuration provide a Java 17 container build and launch, but do not turn the demo into a
long-running web service. Do not treat this version as a production API deployment.

## Prerequisites

- Java 17 (Azul Zulu is configured as the Gradle toolchain) for local builds.
- Docker with the Docker Compose v2 plugin for container builds and runs.

## Build and test locally

From the repository root, build the JAR and run the test suite with the Gradle wrapper:

```powershell
.\gradlew.bat clean build
```

On macOS/Linux, run `./gradlew clean build`. To run only the tests, use `.\gradlew.bat test` (or `./gradlew test`).

Run the built console application:

```powershell
java -cp build\libs\project-portal-1.0.0.jar com.bu.Application
```

On macOS/Linux, use `java -cp build/libs/project-portal-1.0.0.jar com.bu.Application`.

## Build and run the Docker container

Build the image:

```shell
docker compose build
```

Run the demo once and print its output in the terminal:

```shell
docker compose run --rm --no-deps artifact-indexer
```

The container exits after the demo finishes. The Compose service maps port 8080 for the future API, but the current
application does not listen on that port.

## Deploy on a Docker host

For the current console demo, deploy by checking out the repository on a host with Docker Compose v2, then run:

```shell
docker compose build
docker compose run --rm --no-deps artifact-indexer
```

This builds the image on the target host and runs the demo once; it does not publish an image to a registry or deploy
a persistent API. The GitHub Actions workflow builds and scans the image but does not publish it. A production API
deployment requires implementing the Spring Boot web service from `spec.md` and selecting a hosting target and image
registry.

## Dependency lockfile

Commit `gradle.lockfile` to Git. Gradle dependency locking is enabled in `build.gradle`; the lockfile records the
resolved dependency versions so builds and Trivy dependency scans use a reproducible dependency graph. Regenerate it
after dependency changes with `.\gradlew.bat dependencies --write-locks` on Windows or
`./gradlew dependencies --write-locks` on macOS/Linux, then include the updated file in the same commit.