# See https://keepachangelog.com/en/1.0.0/

## [0.0.14] 2021-03-22
### Added
- Flyway for database upgrades
### Changed
- Upgrade to springboot-2.4.4 and spring-5.3.5

## [0.0.13] 2021-03-21
### Changed
- Run maven in quiet mode with Github Workflow
- Add cache configuration in `pom.xml`, `application.yml`
- Add `@EnableCaching` in `Application` launcher class
- Add spring configuration class for caching

## [0.0.12] 2021-03-19
### Added
- Database configuration in `pom.xml`, `application.yml`

## [0.0.11] 2021-03-17
### Added
- Deployment of API server to Heroku
- Add CLI option for registering services
### Changed
- Server jar no longer deployed to github Maven repo

## [0.0.10] 2021-03-17
### Added
- Add `settings.xml` for maven generator

## [0.0.9] 2021-03-17
### Added
- Add `.gitignore`

## [0.0.8] 2021-03-16
### Added
- Add server `application.yml` configurations

## [0.0.7] 2021-03-15
### Fixed
- Generate .editorconfig
- Generate github workflows script

## [0.0.6] 2021-03-14
### Fixed
- Add javax.validation dependency in pom.xml
- Fix constructor of delegates

## [0.0.5] 2021-03-14
### Added
- Command Line interface for generating Server and SDK

## [0.0.4] 2021-03-12
### Added
- Server code generators

### Removed
- `groupId` and `artifactId` from the context

## [0.0.3] 2021-03-11
### Fixed
- add the request parameter in the API methods

## [0.0.2] 2021-03-11
### Changed
- Package structure. Code moved from `com.wutsi.codegen.sdk.kotlin` to `com.wutsi.codegen.kotlin.sdk`

## [0.0.1] 2021-03-11
### Added
- SDK code generator
