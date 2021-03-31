[![](https://github.com/foo/test-service/actions/workflows/master.yml/badge.svg)](https://github.com/foo/test-service/actions/workflows/master.yml)
[![](https://github.com/foo/test-service/actions/workflows/pull_request.yml/badge.svg)](https://github.com/foo/test-service/actions/workflows/pull_request.yml)

[![JDK](https://img.shields.io/badge/jdk-1.8-brightgreen.svg)](https://jdk.java.net/11/)
[![](https://img.shields.io/badge/maven-3.6-brightgreen.svg)](https://maven.apache.org/download.cgi)
![](https://img.shields.io/badge/language-kotlin-blue.svg)
[![](https://img.shields.io/badge/version-0.0.21-blue.svg)](https://jdk.java.net/11/)

This api used for managing likes on links

# Installation Prerequisites

## Configure Github
- Generate a Github token for accessing packages from GibHub
  - Goto [https://github.com/settings/tokens](https://github.com/settings/tokens)
  - Click on `Generate New Token`
  - Give a value to your token
  - Select the permissions `read:packages`
  - Generate the token
- Set your GitHub environment variables on your machine:
  - `GITHUB_TOKEN = your-token-value`
  - `GITHUB_USER = your-github-user-name`

## Maven Setup
- Download Instance [Maven 3.6+](https://maven.apache.org/download.cgi)
- Add into `~/m2/settings.xml`
```
    <settings>
        ...
        <servers>
            ...
            <server>
              <id>wutsi-spring-memcached</id>
              <username>${env.GITUB_USER}</username>
              <password>${env.GITHUB_TOKEN}</password>
            </server>
        </servers>
    </settings>
```

## Usage
- Install
```
$ git clone git@github.com:foo/test-service.git
```

- Build
```
$ cd test-service
$ mvn clean install
```

- Launch the API
```
$ mvn spring-boot:run
```

That's it... the API is up and running! Start sending requests :-)

# Links
- [API](docs/api/)
