# See https://keepachangelog.com/en/1.0.0/

## [0.0.29] 2021-04-17
### Changed
- Integrate with wutsi-stream-rabbitmq 1.5.0 to support DLQ replay
### Fixed
- API interface with query string

## [0.0.28] 2021-04-10
### Changed
- Version of wutsi-stream-rabbitmq
- Simplify maven dependency configuration for wutsi
- Initialize MDC configure in the event handlers

## [0.0.27] 2021-04-10
### Added
- Add request interceptors
- slf4j configuration
### Fixed
- Default value for `Boolean`

## [0.0.26] 2021-04-09
### Fixed
- Return value of Sdk APIBuilder

## [0.0.25] 2021-04-09
### Added
- Added SDK environment enum class
### Changes
- Do not generate cache/mqueue config when flag is not present in CLI

## [0.0.24] 2021-04-03
### Changes
- pom.xml SDK artifact-id

## [0.0.23] 2021-04-03
### Added
- SDK GithubActions generator
- SDK Readme generator
- CORS Filter

## [0.0.22] 2021-04-02
### Added
- Support object
### Fixed
- Fixed order parameter in the controller
- Do not add CloudMQ to Heroku server
- API Link in README
- SDK CLI code generator

## [0.0.21] 2021-04-01
### Changed
- data classes moved from package `.model` to `.dto`
- Remove version from README
### Fixed
- Fix the hardcoded URL downloaded

## [0.0.20] 2021-03-31
### Added
- README generator
- Add swagger-ui code generator
-
### Changed
- Do not overide `application..yml` files
- Add missing repository `wutsi-stream-rabbitmq` in `pom.xml`
- Exclude from code coverage configuration classes

## [0.0.19] 2021-03-29
### Changed
- Integration of Message Queue
- Change caching configuration file from `StringCache...` to `Cache...`
### Fixed
- Update dependency of `wutsi-stream-memcached` to `v1.1.0` to fix a NPE
- FIX: RabbitMQ configuration variables (`rabbitnq...` -> `rabbitmq...`)

## [0.0.19] 2021-03-26
### Changed
- set at deployment the heroku setting environment variable `APP_PROFILE`

## [0.0.18] 2021-03-26
### Changed
- Add in pipeline TEST and PROD deployment

## [0.0.17] 2021-03-24
### Changed
- Support openapi `array`
- Add algo to load the models in the right order
- Added Jackson configuration

## [0.0.16] 2021-03-22
### Changed
- Remove validation annotation from SDK

## [0.0.15] 2021-03-22
### Fixed
- Controller parameters and default values

## [0.0.14] 2021-03-22
### Added
- Flyway for database upgrades
- Database setup in Github Workflow scripts
### Changed
- Upgrade to springboot-2.4.4 and spring-5.3.5
- Disable schema auto creation in Hibernate
- Add dependency to `spring-boot-starter-validation`
- Add dependency to `spring-boot-starter-test`

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
