Generate the API SDK in Kotlin from an OpenAPIV3 schemas, that usesSdkCodeGeneratorTest [feign](https://github.com/OpenFeign/feign) as HTTP client binder.

This generator generates:
- Data classes for each of the API schemas, generated in the directory `<output-dir>/src/main/kotlin`, in the package `<base-package>.model`
- The API service class, generated in the directory `<output-dir>/src/main/kotlin`, in the package `<base-package>`
- The `pom.xml` generated in the directory `<output-dir>`

### Usage
```
java -jar wutsi-codegen sdk
    -i <openapi-file-url>
    -o <output-dir>
    -p <base-package>
    -a <maven-artifact-name>
    -g <maven-group>
    -n <api-name>
```
