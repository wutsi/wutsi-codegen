Generate the API SDK in Kotlin from an OpenAPIV3 schemas

This generator generates:
- ``pom.xml``: Maven pom file with all the dependencies
- Data classes for each of the API schemas, generated into the directory `<output-dir>`, in the package `<base-package>.model`
- The API service class, generated into the directory `<output-dir>`, in the package `<base-package>`

### Usage
```
java -jar wutsi-codegen sdk -i <openapi-file-url> -o <output-dir> -p <base-package> -a <api-name>
```

### Dependencies
- [feign](https://github.com/OpenFeign/feign): HTTP client binder
- [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization)
