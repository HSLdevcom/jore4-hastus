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

## Docker reference

The application uses spring boot which allows overwriting configuration properties as described
[here](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables).
The docker container is also able to
[read secrets](https://github.com/HSLdevcom/jore4-tools#read-secretssh) and expose
them as environment variables.

The following configuration properties are to be defined for each environment:

| Config property | Environment variable   | Secret name | Example                             | Description                                                                                         |
| --------------- | ---------------------- | ----------- | ----------------------------------- | --------------------------------------------------------------------------------------------------- |
| -               | SECRET_STORE_BASE_PATH | -           | /mnt/secrets-store                  | Directory containing the docker secrets                                                             |
| hasura.url      | HASURA_URL             | hasura-url  | http://jore4-hasura:8080/v1/graphql | Hasura microservice base url                                                                        |
| hasura.secret   | _don't use_            | _don't use_ | hasura                              | Hasura admin secret used only for generating graphql schema. _Don't use it for the running service_ |

More properties can be found from `/profiles/prod/config.properties`
