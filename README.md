# Agens-Alert

Real-time monitor and visualization for Agenspop (AG3 utility)
Someday, Alert function will be added on this project.

## Parent project : mixed 

Backend
 - spring boot & webflux
 - kotlin & co-routine
 - spring-data-r2dbc
 - r2dbc-h2 (embedded db: file)
 - webclient (connect to agenspop)

Frontend
 - Angular 8 & bootstrap 4
 - amcharts4 (date axis)
 - cytoscape.js 3
 - lodash-es 4
 - UI Template: [ArchitectUI - Angular 7 Bootstrap 4](https://github.com/DashboardPack/architectui-angular-theme-free)

 
## Build & Run

Build
````bash
mvn clean install -DskipTests
````

<img height="460px" src="">

Run
```bash
cd backend/target
java -jar agens-alert-0.7.jar --spring.config.name=alert-config

## or

cd backend
mvn spring-boot:run
```

<img height="460px" src="">

## Backend APIs

Query with Date range


Query with Time range


## Frontend Views

Monitor Queries (List)

<img height="460px" src="">


Monitor View (about one query)

<img height="460px" src="">


Monitor Realtime (about one query)

<img height="460px" src="">



### Reference Documents

For further reference, please consider the following sections:

* [spring-boot-kotlin-coroutines](https://www.baeldung.com/spring-boot-kotlin-coroutines)
* [webflux-r2dbc-kotlin](https://github.com/razvn/webflux-r2dbc-kotlin)
* [Spring Data R2DBC [Experimental]](https://docs.spring.io/spring-data/r2dbc/docs/1.0.x/reference/html/#reference)
* [r2dbc.io](https://r2dbc.io/)
