package net.bitnine.ag3.agensalert.config

import net.bitnine.ag3.agensalert.storage.H2SheduleService
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

// https://github.com/ruslanys/sample-spring-boot-netty/blob/master/src/main/kotlin/me/ruslanys/config/ExecutorsConfig.kt

@Configuration
@EnableScheduling
@EnableAsync
class ExecutorsConfig : SchedulingConfigurer, AsyncConfigurer {

    companion object {
        private const val SCHEDULER_POOL_SIZE = 10
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