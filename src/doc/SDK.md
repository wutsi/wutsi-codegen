Generate the API SDK in Kotlin from an OpenAPIV3 schemas.

## Usage
```
java -jar wutsi-codegen sdk
    -i <openapi-file-url>
    -n <api-name>
    -p <base-package>
    -o <output-dir>

  <openapi-file-url> REQUIRED - URL of the OpenAPI file
  <api-name>         REQUIRED - Name of the API
  <base-package>     REQUIRED - Base package of the SDK
  <output-dir>       OPTIONAL - Output directory to the generate files will be stored. Default = ./out.
```

## Output
The SDK generator will generate the following files.
- The Maven descriptor `pom.xml`.
  - It's located in the directory `<output-dir>`
  - It has the following information:
    - The `version` = `info.version` in the OpenAPI file
    - The `artifactId` = `<api-name>-sdk` provided in the command line
    - The `groupId` = `<base-package>` provided in the command line
- The API class, based on [feign](https://github.com/OpenFeign/feign):
  - It's located in the directory `<output-dir>/src/main/kotlin`
  - The classname is `<base-package>.<api-name>API`
  - It exposes a function for each endpoint of the API
- The Model classes to represent the entities of the API
  - They are located in the directory `<output-dir>/src/main/kotlin`
  - Their package name `<base-package>.model`

## Dependencies
- [feign](https://github.com/OpenFeign/feign) as HTTP client binder.
- [javax.validation](https://mvnrepository.com/artifact/javax.validation/validation-api).
