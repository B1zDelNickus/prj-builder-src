package codes.spectrum.sources.demo.transport.bus

import codes.spectrum.bus.builder.buildBus
import codes.spectrum.bus.integration.BusNavigator
import codes.spectrum.message.queue.RabbitConstants
import codes.spectrum.sources.config.GlobalConfig
import codes.spectrum.sources.config.getOrDefault
import codes.spectrum.sources.demo.DemoConstants
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory

val sourceBus = buildBus(DemoConstants.SOURCE_ID) {
    topic(DemoConstants.EXCHANGE_NAME) {
        route(DemoConstants.DEFAULT_BUS_AGENT_ROUTE) {
            queue(DemoConstants.DEFAULT_BUS_AGENT_QUEUE)
        }
        route(DemoConstants.DEFAULT_BUS_AGENT_ERROR_ROUTE) {
            queue(DemoConstants.DEFAULT_BUS_AGENT_ERROR_QUEUE)
        }
    }

    delay()

    service(DemoConstants.DEFAULT_BUS_AGENT_SERVICE_NAME) {
        input(DemoConstants.DEFAULT_BUS_AGENT_SERVICE_INPUT, DemoConstants.DEFAULT_BUS_AGENT_QUEUE)
        out(DemoConstants.DEFAULT_BUS_AGENT_SERVICE_INPUT, DemoConstants.DEFAULT_BUS_AGENT_ROUTE)
        out(DemoConstants.DEFAULT_BUS_AGENT_SERVICE_ERROR, DemoConstants.DEFAULT_BUS_AGENT_ERROR_ROUTE)
    }
}

private val host = GlobalConfig.getOrDefault("rabbitHost", RabbitConstants.DEFAULT_PROD_HOST)
private val port = GlobalConfig.getOrDefault("rabbitPort", RabbitConstants.DEFAULT_PROD_PORT)
private val user = GlobalConfig.getOrDefault("rabbitUser", RabbitConstants.DEFAULT_PROD_USER)
private val password = GlobalConfig.getOrDefault("rabbitPassword", RabbitConstants.DEFAULT_PROD_PASS)

val connectionFactory by lazy { CachingConnectionFactory(host, port).apply {
    setUsername(user)
    setPassword(password)
}
}

val navigator by lazy { BusNavigator(sourceBus, connectionFactory) }
