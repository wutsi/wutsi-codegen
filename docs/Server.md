Generates the code for an API server, from an OpenAPIV3 specification.

The code generated includes:
- The API documentation browser (using Swagger UI)
- The [Maven](https://maven.apache.org/) build configuration files
- The [Github Actions](https://github.com/features/actions) scripts for building and deploy the API to [Heroku](https://www.heroku.com)
- The code shell for all the endpoint. The code is based on [Kotlin](https://kotlinlang.org/) and [Springboot](https://spring.io/projects/spring-boot)

## Usage
```
java wutsi-codegen-<version>.jar server [options]
 -github_project <github-project>   The github project name
 -github_user <github-user>         Your github username
 -heroku <heroku-app>               Heroku application name. This will trigger the deployment when merging to `master` branch.
                                    IMPORTANT: The github secret HEROKU_API_KEY must be configured.
 -in <openapi-file-url>             (REQUIRED) URL of the OpenAPIV3 file that describe the API
 -jdk <jdk-version>                 Version of the JDK of the project. Default: 1.8
 -name <api-name>                   (REQUIRED) Name of the API. Ex: like
 -out <output-dir>                  (REQUIRED) Output directory. Where to store the generated files
 -package <base-package>            (REQUIRED) Base package of the api. Ex: com.foo.bar
 -service_cache                     Attach a cache to the API
 -service_database                  Attach a database to the API
 -service_logger                    Attach a logger to the API
 -service_mqueue                    Attach a queue to the API
```
