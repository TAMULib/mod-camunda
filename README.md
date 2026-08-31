# mod-camunda

Copyright (C) 2018-2025 The Open Library Foundation.

This software is distributed under the terms of the Apache License, Version 2.0.
See the file ["LICENSE"](LICENSE) for more information.


# Table of Contents

1. [Docker deployment](#docker-deployment)
  1. [Publish docker image](#publish-docker-image)
2. [Manual Build and Run](#manual-build-and-run)
3. [Camunda Module Dependencies](#camunda-module-dependencies)
4. [Workflow Project Structure](#workflow-project-structure)
5. [Camunda APIs](#camunda-apis)
6. [Kafka Message Broker](#kafka-message-broker)
7. [Issue Tracker](#issue-tracker)


## Docker deployment

```
cd ..
git clone https://github.com/TAMULib/mod-workflow.git
cd mod-workflow
mvn clean install

cd mod-camunda
docker build -t folio/mod-camunda .
docker run -d -p 8081:8081 folio/mod-camunda
```


### Publish docker image

```
docker login [docker repo]
docker build -t [docker repo]/folio/mod-camunda:[version] .
docker push [docker repo]/folio/mod-camunda:[version]
```


## Manual Build and Run

The `mod-camunda` project may also be built and run directly on a local machine.
There are external services that are still needed and it is recommended that a remote gateway and a remote Kafka are used.
This example describes the local build and run process using an already configured local Postgresql server with already configured remote gateway, remote Kafka servers, and a remote (or local) `mod-workflow`.
This example uses environment variables to configure the `mod-camunda` service, however these configurations may be directly applied to the `application.yml`.

```shell
DB_USERNAME=folio \
DB_PASSWORD=folio \
DB_PORT=5432 \
SERVER_PORT=9000 \
GATEWAY_URL=http://remote_gateway:8000 \
SPRING_JPA_HIBERNATE_DDLAUTO="update" \
CAMUNDA_BPM_DATABASE_SCHEMAUPDATE=true \
TENANT_INITIALIZEDEFAULTENANT=true \
TENANT_RECREATEDEFAULTTENANT=true \
SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_CHECKSERVERIDENTITY=false \
TENANT_DEFAULTTENANT=diku \
KAFKA_HOST=https://remote_kafka \
KAFKA_PORT=9092 \
APPLICATION_KAFKA_LISTENER_EVENTS_TOPIC_PATTERN='folio.(.*\.)?camunda_local.events' \
APPLICATION_KAFKA_LISTENER_EVENTS_GROUPID=folio-mod-camunda_local-events-group \
mvn clean spring-boot:run
```

The above example explicitly defines the Postgresql database settings.
The `SPRING_JPA_HIBERNATE_DDLAUTO`, `CAMUNDA_BPM_DATABASE_SCHEMAUPDATE`, `TENANT_INITIALIZEDEFAULTENANT`, and `TENANT_RECREATEDEFAULTTENANT` environment variables are defined this way so that the database is deleted and re-created on each run. If this is not desired, then they should be set to `false`.
Note that the `SPRING_JPA_HIBERNATE_DDLAUTO` could be set to `None`, but `update` is still safe when preserving the database between runs.

The `SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_CHECKSERVERIDENTITY` is only needed on a local machine because there might not be a valid mail server certificate for the local machine.
This probably should not be set to `false` on a production system.

The `TENANT_DEFAULTTENANT` is added to show that the tenant can be changed, but the default of `diku` is likely fine.

The `APPLICATION_KAFKA_LISTENER_EVENTS_TOPIC_PATTERN` and `APPLICATION_KAFKA_LISTENER_EVENTS_GROUPID` are provided to separate the normal Kafka messages from the local instance.
This helps ensure isolation between different systems.
It is recommended to use a unique topic pattern and group id for each local instance (as in for each developer machine running this locally).

The `GATEWAY_URL`, `KAFKA_HOST`, and `KAFKA_PORT` are standard server configuration variables for the remote systems.
The values assigned to these are for example purposes and likely will not work without being changed to a proper address.

The `SERVER_PORT` is provided because the `mod-workflow` might be run on the same local machine.
They both default to port `8081` and that could be changed to something like `9000` to prevent conflicts.

The `GATEWAY_URL` may be changed to point directly to a local `mod-workflow` to bypass the gateway.
This can help make development easier in certain situations.
In such a case, this could be set to the local `mod-workflow` instance, such as `GATEWAY_URL=http://localhost:9001`.


## Camunda Module Dependencies

This module depends on spring-module-core and brings in Camunda BPM to enable workflow capabilities.
Camunda is an open-source BPM platform that is embedded in this module.
The [pom.xml](pom.xml) described these versions and dependencies.

- `camunda-bpm-spring-boot-starter`
  - Adds the Camunda engine.
  - [https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/](https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/)
  - [https://github.com/camunda/camunda-bpm-spring-boot-starter](https://github.com/camunda/camunda-bpm-spring-boot-starter)
  - The Camunda engine requires a database schema to be configured on startup.
    - Details on the process engine database schema configuration can be found in the [spring boot integration configuration](https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/configuration/).
- `camunda-bpm-spring-boot-starter-webapp`
  - Enables Web Applications such as Camunda Cockpit and Tasklist.
  - [https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/webapps/](https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/webapps/)
- `camunda-bpm-spring-boot-starter-rest`
  - Enables the Camunda REST API.
  - [https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/rest-api/](https://docs.camunda.org/manual/develop/user-guide/spring-boot-integration/rest-api/)
  - [https://docs.camunda.org/manual/latest/reference/rest/](https://docs.camunda.org/manual/latest/reference/rest/)
  - The Camunda REST API uses Jersey so we use spring boot's common application properties to configure the path to be /camunda in the application.yml file.
    - `spring.jersey.application-path=camunda`


## Workflow Project Structure

Business Process Models and Decision Models are built using the [Camunda Modeler](https://camunda.com/products/modeler/) which implements **BPMN 2.0** and **DMN 1.1** specifications.

The UI module [ui-workflow](https://github.com/folio-org/ui-workflow) provides a way to facilitate building, activating, and running individual workflows.

Any Java code that is executed in the context of a process is usually written in a Java Delegate.
These classes are stored in `src/main/java/org/folio/rest/camunda/delegate`.


## Accessing the Application

1. Start `mod-camunda` either through the Docker interface or through the manual `mvn clean spring-boot:run` command.
2. Navigate to the Camunda Portal, such as `https://localhost:9000/ui/welcome/default/#/welcome` for a local execution.
3. Log in as admin user: `admin`, password: `admin` (which can be changed using environment variables or by tweaking the `application.yml` file).


## Camunda APIs
The [module descriptor template](descriptors/ModuleDescriptor-template.json) describes the available end points provided by `mod-camunda`.
This template is used to build the module descriptor when building the package or starting the local server.
This end point described by this template can be found via appropriate FOLIO interfaces, such what [Stripes UI](https://github.com/folio-org/stripes-ui) may provide.

The following are example end points:
- Process/Decision Deployment
  - [https://docs.camunda.org/manual/latest/reference/rest/deployment/](https://docs.camunda.org/manual/latest/reference/rest/deployment/)
  - `GET`
    - `/camunda/deployment`
    - `/camunda/deployment/{id}`
  - `POST`
    - `/camunda/deployment/create`
  - `DELETE`
    - `/camunda/deployment/{id}`
- Process Definition
  - [https://docs.camunda.org/manual/latest/reference/rest/process-definition/](https://docs.camunda.org/manual/latest/reference/rest/process-definition/)
  - `GET`
    - `/camunda/process-definition`
    - `/camunda/process-definition/{id}`
  - `POST`
    - `/camunda/process-definition/{id}/start`
    - `/camunda/process-definition/key/{key}/tenant-id/{tenant-id}/start`
- Decision Definition
  - [https://docs.camunda.org/manual/latest/reference/rest/decision-definition/](https://docs.camunda.org/manual/latest/reference/rest/decision-definition/)
  - `GET`
    - `/camunda/decision-definition`
    - `/camunda/decision-definition/{id}`
- Tasks
  - [https://docs.camunda.org/manual/latest/reference/rest/task/](https://docs.camunda.org/manual/latest/reference/rest/task/)
  - `GET`
    - `/camunda/task`
    - `/camunda/task/{id}`
  - `POST`
    - `/camunda/task/{id}/claim`
    - `/camunda/task/{id}/unclaim`
    - `/camunda/task/{id}/complete`
- Message Events
  - [https://docs.camunda.org/manual/latest/reference/rest/message/](https://docs.camunda.org/manual/latest/reference/rest/message/)
  - `POST`
    - `/camunda/message`


## Kafka Message Broker

The message broker being used is the standard FOLIO message broker called [Apache Kafka](https://kafka.apache.org/), which is called Kafka for brevity.


### Workflow Module Triggers

The trigger entity from `mod-workflow` is used to select which request-response events from Okapi are to be published to the `${ENV:folio}.workflow.events` topic that mod-camunda can subscribe to. In order to create the Triggers we have to provide the correct permissions to the `diku_admin`. The vagrant will create an example trigger for when a user is created.


### Common Environment Variables:

The most notable environment variables for deployment are described in the [Module Descriptor](descriptors/ModuleDescriptor-template.json) template.

The following is a summary of many of them.

| Name                                 |       Default value         | Description
|:-------------------------------------|:----------------------------|:-------------------------------------------------------
| DB_CHARSET                           | UTF-8                       | Database charset.
| DB_DATABASE                          | okapi_modules               | Postgres database name.
| DB_SCHEMA                            | diku_mod_camunda            | Postgres database schema name.
| DB_HOST                              | postgres                    | Postgres host name.
| DB_MAXPOOLSIZE                       | 5                           | Database max pool size.
| DB_PASSWORD                          | folio_admin                 | Postgres user password (be sure to change this).
| DB_PORT                              | 5432                        | Postgres port.
| DB_QUERYTIMEOUT                      | 60000                       | Database query timeout.
| DB_USERNAME                          | folio_admin                 | Postgres user name.
| FOLIO_ENV_DEFAULTS_LOGLEVEL_EXPOSE   | true                        | Set the `logLevel` variable expose value.
| FOLIO_ENV_DEFAULTS_LOGLEVEL_NAME     | logLevel                    | Set the `logLevel` variable name.
| FOLIO_ENV_DEFAULTS_LOGLEVEL_TYPE     | literal                     | Set the `logLevel` variable type.
| FOLIO_ENV_DEFAULTS_LOGLEVEL_VALUE    | INFO                        | Set the `logLevel` variable value.
| FOLIO_ENV_DEFAULTS_GATEWAYURL_VALUE  | `http://localhost:9130`     | Set to the same value as `OKAPI_URL`, something like `https://kong:8000`.
| FOLIO_LOGIN_PATH                     | `/authn/login-with-expiry`  | Set the log in path.
| JAVA_OPTIONS                         | `-XX:MaxRAMPercentage=75.0` | Java options.
| KAFKA_HOST                           | kafka                       | Kafka broker host name.
| KAFKA_PORT                           | 9092                        | Kafka broker port.
| KAFKA_SECURITY_PROTOCOL              | PLAINTEXT                   | Kafka security protocol used to communicate with brokers (SSL or PLAINTEXT).
| KAFKA_SSL_KEYSTORE_LOCATION          | -                           | The location of the Kafka key store file. This is optional for client and can be used for two-way authentication for client.
| KAFKA_SSL_KEYSTORE_PASSWORD          | -                           | The store password for the Kafka key store file. This is optional for client and only needed if `ssl.keystore.location` is configured.
| KAFKA_SSL_TRUSTSTORE_LOCATION        | -                           | The location of the Kafka trust store file.
| KAFKA_SSL_TRUSTSTORE_PASSWORD        | -                           | The password for the Kafka trust store file. If a password is not set, trust store file configured will still be used, but integrity checking is disabled.
| OKAPI_AUTH_ACCESSCOOKIENAME          | `folioAccessToken`          | The OKAPI cookie name for the Access token; This is generally never changed.
| OKAPI_AUTH_REFRESHCOOKIENAME         | `folioRefreshToken`         | The OKAPI cookie name for the Refresh token; This is generally never changed.
| OKAPI_AUTH_TOKENHEADERNAME           | `x-okapi-token`             | The OKAPI header token name; This is generally never changed.
| OKAPI_URL                            | `http://localhost:9130`     | Gateway URL, such as `https://kong:8000`.
| CAMUNDA_BPM_ADMINUSER_EMAIL          | `admin@localhost`           | The e-mail address of the Camunda administration user.
| CAMUNDA_BPM_ADMINUSER_ID             | admin                       | The account name of the Camunda administration user.
| CAMUNDA_BPM_ADMINUSER_PASSWORD       | admin                       | The password of the Camunda administration user.
| CAMUNDA_BPM_DATABASE_SCHEMAUPDATE    | true                        | If Camunda should auto-update the BPM database schema (may result in dropping existing data).
| CAMUNDA_BPM_METRICS                  | false                       | Enable or disable Camunda metrics by default.
| SERVER_PORT                          | 8081                        | The port the `mod-camunda` service listens on.
| SERVER_SERVLET_CONTEXTPATH           | `/`                         | The context path, or base path, to host at.
| SPRING_FLYWAY_ENABLED                | false                       | Database migration support via Spring Flyway.
| SPRING_JPA_HIBERNATE_DDLAUTO         | update                      | Auto-configure database on startup.
| TENANT_DEFAULTTENANT                 | diku                        | The name of the default tenant to use.
| TENANT_FORCETENANT                   | false                       | Forcibly add or overwrite the tenant name using the default tenant.
| TENANT_INITIALIZEDEFAULTENANT        | true                        | Perform initial auto-creation of tenant in the database (schema, tables, etc..).
| TENANT_RECREATEDEFAULTTENANT         | false                       | When `TENANT_INITIALIZEDEFAULTTENANT` is true and the database already exists, then drop and re-create the database on start.

The `folio.env.defaults`, which maps to `FOLIO_ENV_DEFAULTS`, supports a set of named objects with a structure similar to the **JSON** below that is also shown in the table above:
```json
"logLevel": {
  "name": "logLevel",
  "type": "literal",
  "expose": true,
  "value": "DEBUG"
}
```

The key name must match the `name` value.

The environment variable is not stored as **JSON**, however, but is instead stored using the following format:
```
  FOLIO_ENV_DEFAULTS_[row number]_[field name]
```
Where `[row number]` is replaced, with a number like `0`, `1`, etc...
Where `[field name]` is replaced with a field name like `name`, `type`, etc...

The `folio.env.defaults` items have the following fields:
- `expose`
- `name`
- `type`
- `value`

The `expose` may either be `true` or `false` and represents whether or not to expose the variable to a **Script Task**.
The `name` may only be word characters without leading numbers that represents a variable name to use.
The `type` is a specific list of known types that determine how the `value` is handled and validated.
The `value` is the value to initially store, which is allowed to change over time and can also be **NULL**.

The `folio.env.defaults` is intended to be dynamically generated through environment variables as needed for any given environment.
The following types are provided:
- `literal`
- `secure`
- `url`
- `url_path`

The `literal` is simple a raw string value that is not processed in any way.
The `secure` is a string value that gets stored into memory in an encrypted form once loaded.
The `url` is a string that gets verified to be a valid **URL** on start.
The `url_path` is a string that gets verified to be a valid **URL** path on start and requires a leading slash (`/`) and no trailing slash.


#### Secure FOLIO Env Defaults

The `secure` type for `folio.env.defaults` is designed around **Java** security classes.
This does not expose the secure variables to **Camunda**.

Each delegate must be configured to fetch and handle these secure variables as needed.
These will generally be reserved by each delegate and might even be required for the delegate to operate as intended.
Each reserved variable can be overriden using the usual **Camunda** functionality.


### Permissions

The permissions provided by this module are described in the [Module Descriptor](https://github.com/folio-org/mod-camunda/blob/master/descriptors/ModuleDescriptor-template.json) under `permissionSets`.

The permissions defined here are specific to this module and are usually not directly applied to any specific user.
Instead, permissions available for assignment to users (or accounts) are found in the [ui-workflow Module Permission Sets](https://github.com/folio-org/ui-workflow/blob/main/package.json).
These [ui-workflow](https://github.com/folio-org/ui-workflow) permissions are automatically exposed via the appropriate [Stripes UI](https://github.com/folio-org/stripes-ui) administration interface.


### Issue tracker

See project [FOLIO](https://issues.folio.org/browse/FOLIO) and the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker/).
