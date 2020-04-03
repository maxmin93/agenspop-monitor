# Agens-Alert

Real-time monitor and visualization for Agenspop (AG3 utility)
Someday, Alert function will be added on this project.

### Base project

[stat.spring.io](https://start.spring.io/#!type=gradle-project&language=kotlin&platformVersion=2.2.1.RELEASE&packaging=jar&jvmVersion=1.8&groupId=net.bitnine.ag3&artifactId=webflux-r2dbc-kotlin&name=webflux-r2dbc-kotlin&description=Demo%20project%20for%20Spring%20Boot&packageName=net.bitnine.ag3.webflux-r2dbc-kotlin&dependencies=webflux,data-r2dbc,h2,actuator)
 - gradle (kotlin)
 - kotlin
 - webflux
 - spring-data-r2dbc
 - h2
 - actuator
 
### Extra dependencies

````kotlin
implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.0.0.RELEASE")
testImplementation("io.mockk:mockk:1.9")
````

### Extra remarks
- Had to manual set the h2 version to a previous one as r2dbc-h2 can't handle (yet) the changes in 1.4.200
- Unit tests and Integration tests are including (100% coverage :))
- Upgraded to Gradle 6.0

### Reference Documentation
For further reference, please consider the following sections:

* [spring-boot-kotlin-coroutines](https://www.baeldung.com/spring-boot-kotlin-coroutines)
* [webflux-r2dbc-kotlin](https://github.com/razvn/webflux-r2dbc-kotlin)
* [Spring Data R2DBC [Experimental]](https://docs.spring.io/spring-data/r2dbc/docs/1.0.x/reference/html/#reference)
* [r2dbc.io](https://r2dbc.io/)
