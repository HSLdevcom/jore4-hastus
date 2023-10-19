# jore4-hastus

A spring boot server application which acts as a converter between jore4 and Hastus.

## Development

### Necessary tools

- Maven
- JDK17+

Uses maven to build the project, use `mvn install` to build the server. You can also run the generated .jar file locally to test the server on port 8080.

`development.sh` provides several commands to run the server in a docker container:

- `start` runs the server in port 3008
- `stop` stops the container
- `remove` removes the docker container
- `build` builds the server locally using maven
- `test` runs all tests

## API structure

### Export feature

`POST export/routes` Send a JSON body containing the route labels, priority and date for which to produce a CSV file for Hastus.

Export endpoint expects a JSON format:

```json
{
    uniqueLabels: [string...],
    priority: int,
    observationDate: "yyyy-mm-dd"
}
```

Request parameters are used to filter routes when they are retrieved (via GraphQL) from the Jore4 network & routes database.
During the export, as a side effect, a `journey_pattern_ref` entry is created for each route in the Jore4 timetables database.
These entries are later used to connect Hastus timetables to their source routes in the import function.

The returned CSV file follows the HSL's Hastus CSV specification.

### Import feature

`POST import` Send a text body in CSV format containing a Hastus schedule to be imported as a vehicle schedule frame to the Jore4 timetables database.
The trip records in the CSV data are matched against the journey pattern references (`journey_pattern_ref`) in the Jore4 timetables database.
For each trip record in the CSV, a route match must be found in the existing journey pattern references.

The matching criteria between Hastus trips and journey pattern references are:
- route labels must be the same
- route direction must be the same
- the stop point labels must match and be in the same order
- the timing point labels must match and be in the same order

As a result of a successful import event, a vehicle schedule frame is created in the Jore4 timetables database.
The identifier of the resulting vehicle schedule frame is returned in the JSON response:

```json
{
  "vehicleScheduleFrameId": "238d0bbc-6be7-4070-9a3b-73fbaae57e01"
}
```

In the event of an error, e.g. when no journey pattern reference is found for a Hastus trip, the following error response is returned:

```json
{
    "reason": "descriptive error message"
}
```

## Technical Documentation

jore4-hastus is a Spring Boot application written in Kotlin, which implements a REST API for converting Hastus CSV into Jore4 data and the reverse, Jore4 data into CSV files for Hastus.

### Package Structure

- `fi.hsl.jore.hastus.api` package contains the API endpoint definitions
- `fi.hsl.jore.hastus.config` package contains the server configuration
- `fi.hsl.jore.hastus.data` package contains the hastus data format and mappers
- `fi.hsl.jore.hastus.graphql` package contains graphql service and queries
- `fi.hsl.jore.hastus.util` package contains the utilities, currently CSV writer

Tests are in the `fi.hsl.jore.hastus.test` package.

## Developer Guide

### Coding Conventions

Code should be written using [standard Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

Also:

- ktlint is run automatically during build and will fail the build if any warnings are found
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
