package net.bitnine.ag3.agensalert.config

import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.*
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executor


@Component
class ScheduleTasks(@Autowired val monitorProperties: MonitorProperties) {

    private val logger = LoggerFactory.getLogger(ScheduleTasks::class.java)

    /**
     * This @Schedule annotation run every 5 seconds in this case. It can also
     * take a cron like syntax.
     * See https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html
     */
    @Scheduled(cron="0 5 16 * * ?")    //  매 1분마다 (cron 에서는 initialDelay 안됨)
    // (fixedRate = 5000, initialDelay = 2000)    // 2초 후, 5초 간격 (5초 마다는 fixedDelay)
    fun reportCurrMinute(){
        val currDateTime = LocalDateTime.now()
        val truncatedDateTime = currDateTime.plusSeconds(2).truncatedTo(ChronoUnit.MINUTES)
        logger.info("** each 2 minutes : now ${DateTimeFormatter.ISO_LOCAL_TIME.format(currDateTime)} => ${DateTimeFormatter.ISO_LOCAL_TIME.format(truncatedDateTime)}")
    }

    @Scheduled(cron="\${agens.monitor.cron-expression}")    //  매 10초마다
    fun reportCurrSecond(){
        val currDateTime = LocalDateTime.now()
        val truncatedDateTime = currDateTime.plusNanos(300).truncatedTo(ChronoUnit.SECONDS)
        logger.info("every 10 seconds : now ${DateTimeFormatter.ISO_LOCAL_TIME.format(currDateTime)} => ${DateTimeFormatter.ISO_LOCAL_TIME.format(truncatedDateTime)}")
    }
}

// https://github.com/ruslanys/sample-spring-boot-netty/blob/master/src/main/kotlin/me/ruslanys/config/ExecutorsConfig.kt

@Configuration
@EnableScheduling
@EnableAsync
class ExecutorsConfig : SchedulingConfigurer, AsyncConfigurer {

    companion object {
        private const val SCHEDULER_POOL_SIZE = 5
        private const val ASYNC_POOL_SIZE = 10
    }

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        val taskScheduler = ThreadPoolTaskScheduler()
        taskScheduler.poolSize = SCHEDULER_POOL_SIZE
        taskScheduler.initialize()
        taskScheduler.setThreadNamePrefix("ScheduledTask-")

        taskRegistrar.setTaskScheduler(taskScheduler)
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler =
            AsyncUncaughtExceptionHandler { throwable, _, _ ->
                LoggerFactory.getLogger("Async").error("Async error", throwable)
            }

    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = ASYNC_POOL_SIZE
        executor.setThreadNamePrefix("AsyncTask-")
        executor.initialize()
        return executor
    }

}