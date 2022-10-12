# jore4-hastus

A spring boot server application which acts as a converter between jore4 and Hastus.

## Development

### Necessary tools

- Maven
- JDK11+

Uses maven to build the project, use `mvn install` to build the server. You can also run the generated .jar file locally to test the server on port 8080.

`development.sh` provides several commands to run the server in a docker container:

- `start` runs the server in port 3008
- `stop` stops the container
- `remove` removes the docker container
- `build` builds the server locally using maven
- `test` runs all tests

## API structure

### GET

`/` Hello world JSON response

## Technical Documentation

jore4-hastus is a Spring Boot application written in Kotlin, which implements a REST API for converting Hastus CSV into Jore4 data and the reverse, Jore4 data into CSV files for Hastus.

### Package Structure

- `fi.hsl.jore.hastus.api` package contains the API endpoint definitions
- `fi.hsl.jore.hastus.config` package contains the server configuration

Tests are in the `fi.hsl.jore.hastus.test` package.

## Developer Guide

### Coding Conventions

Code should be written using [standard Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

Also:

- Additionally, minimize the use of mutable variables, using `val` whenever possible.
