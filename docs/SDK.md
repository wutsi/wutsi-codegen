Generate the API SDK in Kotlin from an OpenAPIV3 schemas.

## Usage
```
java wutsi-codegen-<version>.jar sdk [options]
 -github_project <github-project>   The github project name
 -github_user <github-user>         Your github username
 -in <openapi-file-url>             (REQUIRED) URL of the OpenAPIV3 file that describe the API
 -jdk <jdk-version>                 Version of the JDK of the project. Default: 1.8
 -name <api-name>                   (REQUIRED) Name of the API. Ex: like
 -out <output-dir>                  (REQUIRED) Output directory. Where to store the generated files
 -package <base-package>            (REQUIRED) Base package of the api. Ex: com.foo.bar```

## Output
The SDK generator will generate the following files:

- The Maven `<output-dir>/pom.xml`

- The API class, based on [feign](https://github.com/OpenFeign/feign):
  - The API classname in the package `<base-package>.<api-name>API`, that exposes a function for each endpoint
  - Model classes for API entities in the package `<base-package>.model`

- The Model classes to represent the entities of the API
  - They are located in the directory `<output-dir>/src/main/kotlin`
  - Their package name `<base-package>.model`

## Dependencies
- [feign](https://github.com/OpenFeign/feign) as HTTP client binder.
