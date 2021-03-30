Generates the code for an API server, from an OpenAPIV3 specification.

The code generated includes:
- The API documentation browser (using Swagger UI)
- The [Maven](https://maven.apache.org/) build configuration files
- The [Github Actions](https://github.com/features/actions) scripts for building and deploy the API to [Heroku](https://www.heroku.com)
- The code shell for all the endpoint. The code is based on [Kotlin](https://kotlinlang.org/) and [Springboot](https://spring.io/projects/spring-boot)

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
