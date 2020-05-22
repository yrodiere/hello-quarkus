## What is this?

A simple crud application with customers, products and orders.

## Features in use

The quarkus modules/features used are:

- REST services
- Liquibase integration
- Open API
- Unit and Integrated testing with code coverage
- Hibernate/Panache with postgres database
- Bean Validation

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```
./mvnw quarkus:dev
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `hello-quarkus-1.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/hello-quarkus-1.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/hello-quarkus-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.

## Accessing openAPI

```
curl http://localhost:8080/openapi
```

## Swagger UI

http://localhost:8080/swagger-ui

## Running tests

To run unit tests only `./mvnw test`

Before you run the integration tests, the database should be available. The tests won't create a database.
To run all tests unit and integration `./mvnw clean verify`
