package net.bitnine.ag3.agensalert

import net.bitnine.ag3.agensalert.config.MonitorProperties
import net.bitnine.ag3.agensalert.config.ProductProperties
import net.bitnine.ag3.agensalert.gremlin.AgenspopClient
import net.bitnine.ag3.agensalert.gremlin.AgenspopService
import net.bitnine.ag3.agensalert.storage.H2SheduleService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicInteger


@SpringBootApplication
@EnableConfigurationProperties(MonitorProperties::class, ProductProperties::class)
class AgensAlertApplication

fun main(args: Array<String>) {
    val ctx = runApplication<AgensAlertApplication>(*args)

    val monitorProperties = ctx.getBean(MonitorProperties::class.java)
    val productProperties = ctx.getBean(ProductProperties::class.java)

    if( productProperties.debug ){
        println("\nproperties:")
        println("=======================================")
        println(monitorProperties)
        println(productProperties)
        println("=======================================")
    }

    //////////////////////////////////////////////
    //
    //  ** NOTE: 서비스가 가능한 상태인지 확인한다
    //
    //  Configuration 클래스나 H2ScheduleService 의 Postconstruct 에서 작동 불가
    //  - DB 가 생성되기 전에 먼저 실행되어 버림
    //      IllegalStateException: Lifecycle method annotation requires a no-arg method
    //  - AgenspopClient 와 DB의 모든 테이블 확인이 끝나면, canScheduled 상태 변경
    //      ==> 실시간 데이터 캡쳐 작업 시작
    //
    //////////////////////////////////////////////

    println("\ncheck agenspop client and embedded storage before scheduleTasks start..")

    val client = ctx.getBean(AgenspopClient::class.java)
    val check1 = client.findDatasources()
//            .subscribe({
//                println("  1) check agenspop client.. OK (${it.keys})")
//            },{
//                println("  1) check agenspop client.. fail (msg=${it ?: "null"})")
//            })

    val db = ctx.getBean(DatabaseClient::class.java)
    val check2 = db.execute("""select count(*) as cnt from event_qry""").fetch().first()
//            .subscribe({
//                println("  2-1) check event_qry table.. OK (size=${it.get(it.keys.first())})")
//            },{
//                println("  2-1) check event_qry table.. fail (msg=${it})")
//            })
    val check3 = db.execute("""select count(*) as cnt from event_row""").fetch().first()
//            .subscribe({
//                println("  2-2) check event_row table.. OK (size=${it.get(it.keys.first())})")
//            },{
//                println("  2-2) check event_row table.. fail (msg=${it})")
//            })
    val check4 = db.execute("""select count(*) as cnt from event_agg""").fetch().first()
//            .subscribe({
//                println("  2-3) check event_agg table.. OK (size=${it.get(it.keys.first())})")
//            },{
//                println("  2-3) check event_agg table.. fail (msg=${it})")
//            })


    val scheduler = ctx.getBean(H2SheduleService::class.java)
    Flux.concat(check1, check2, check3, check4)
            .map {
                if(it is MutableMap) return@map it.toMap();
                return@map it;
            }
            .collectList()
            .subscribe({
                var checked = 0
                for ( (idx, r) in it.withIndex()){
                    print("  - check[$idx]: ${r.values}")
                    if( r.isNullOrEmpty().not() ){
                        print(" --> OK\n")
                        checked += 1
                    }
                    else{
                        print(" --> fail\n")
                    }
                }

                if( checked == it.size ){
                    println("  ==> All services and tables are nomal. Do activate scheduler")
                    scheduler.setActivate(true)
                }
            },{
                println("  ... check ERROR")
            },{
                println("  ... check completed!")
            })

}

// ** Reference
// https://www.baeldung.com/spring-boot-kotlin-coroutines
// https://github.com/razvn/webflux-r2dbc-kotlin
// https://r2dbc.io/

// **NOTE
// DB handling 이 non-blocking 방식의 reactive 라는 것이지
// 그때문에 WebFlux 를 써야 되는건 아니지 않나?
// - 잦은 IO를 처리해야 하는 것은 실시간 그래프를 위한 WebSocket API 이고,
// - HTTP API 로는 전체 time-scrolling 에 대한 그래프 데이터셋의 dump 뿐

// **NOTE : 웹챗인데 이것은 참고만 할 것
// https://github.com/swsms/webchat
//
// - WebMVC 와 WebFlux 는 하나의 Application 에서 혼용이 안된다
// - 로그인 등의 복잡한 로직은 WebMVC 방식이 편하지만, 당장은 데모 목적이라 추가 안하기로
// - 새로 추가된 그래프만 WebSocket 으로 전달하는, 단순기능으로 간다
// - 급한 것은 Angular 쪽에 WebSocket 클라이언트와 Time-scrolling Chart
