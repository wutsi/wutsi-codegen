Generate the API SDK in Kotlin from an OpenAPIV3 schemas.

## Usage
```
java -jar wutsi-codegen sdk
    -i <openapi-file-url>
    -n <api-name>
    -p <base-package>
    -o <output-dir>
    -g <github-user>

  <openapi-file-url> REQUIRED - URL of the OpenAPI file
  <api-name>         REQUIRED - Name of the API
  <base-package>     REQUIRED - Base package of the SDK
  <output-dir>       REQUIRED - Output directory to the generate files will be stored. Default = ./out.
  <github-user>      OPTIONAL - Github username.
```

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
