Generates the code for an API server, from an OpenAPIV3 specification.

The code generated has the following characteristics:
- The server code is based on [Kotlin Programming Language](https://kotlinlang.org/).
- It's a [Springboot App](https://spring.io/projects/spring-boot).
- It uses [Apache Maven](https://maven.apache.org/) as build tool.
- It uses [Github Actions](https://github.com/features/actions) for CI/CD.
   - All Pull Requests are automatically build.
   - All merges to Master a automatically build and deployed
- It supports automatic deployment to [Heroku](https://www.heroku.com)
   - Heroku application can be automatically created
   - Logger, Database, Caching and MessageQueue can be automatically provisionned to the Heroku app.

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
