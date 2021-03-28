Generate the API server code from an OpenAPIV3 specification.
The code generated is a Kotlin springboot application

## Usage
```
java -jar wutsi-codegen-<version>.jar server
    -i <openapi-file-url>
    -n <api-name>
    -p <base-package>
    -o <output-dir>
    -g <github-user>
    -heroku <heroku-app>
    -service_cache
    -service_logger
    -service_mqueue
    -service_database

  -i <openapi-file-url> REQUIRED - URL of the OpenAPI file
  -a <api-name>         REQUIRED - Name of the API
  -p <base-package>     REQUIRED - Base package of the SDK
  -o <output-dir>       REQUIRED - Output directory to the generate files will be stored. Default = ./out.
  -g <github-user>      OPTIONAL - Github username.
  -heroku <heroku-app>  OPTIONAL - Heroku application name. This will trigger the deployment when merging to `master` branch.
                        IMPORTANT: The github secret HEROKU_API_KEY must be configured.
  -service_cache        OPTIONAL - Attach a cache to the API
  -service_logger       OPTIONAL - Attach a logger to the API
  -service_database     OPTIONAL - Attach a database to the API
  -service_mqueue       OPTIONAL - Attach a message queue to the API
```

## Output
The Server generator will generate the following files.
- `<output-dir>/.editorconfig`: [EditorConfig](https://editorconfig.org/) file

- `<output-dir>/.gitignore`

- The Maven files:
  - `<output-dir>/settings.xml`. **IMPORTANT**: This file is **never** overwritten!
  - `<output-dir>/pom.xml`. **IMPORTANT**: This file is **never** overwritten!

- The code in the directory `<output-dir>/src/main/kotlin`:
  - Controller classes for each endpoint in the package `<base-package>.endpoint`
  - Delegate classes for each endpoint in the package `<base-package>.delegate`
  - Model classes for API entities in the package `<base-package>.model`
  - Spring configuration files in the package `<base-package>.config`

- The configurations files in `<output-dir>/src/main/resource`: `application.yml`, `application-test.yml` and `application-prod.yml`
  **IMPORTANT**: The configuration files are **never** overwritten!

- The GithubActions files:
  - `<output-dir>/.github/workflows/pull_request.yml`: Build script for each PR
  - `<output-dir>/.github/workflows/master.yml`: Build script on master deployment

- The Heroku files:
  - `<output-dir>/Procfile`: Startup command
  - `<output-dir>/system.properties`: System properties

- SpringCache configuration files:
  - `<base-package>/config.SpringCacheLocalConfiguration`: for local environment
  - `<base-package>/config.SpringCacheRemoteConfiguration`: for remote environment

## Dependencies
- SpringBoot v2.4.4
- SpringBoot Actuator
- Junit5
- Kotlin Mockito
