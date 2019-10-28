package codes.spectrum.sources.demo.transport.bus

import codes.spectrum.bus.integration.BusNavigator
import codes.spectrum.bus.rabbit.BusRabbitInitializer
import codes.spectrum.message.queue.getBodyAsString
import codes.spectrum.sources.demo.DemoConstants
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate

/**
 * Тест для миграции bus
 */
class DemoBusInitializerTest: StringSpec({

    /**
     * Замени параметры для миграции на prod или stage
     * Локально можно запустить так
     * docker run -p 5672:5672 -e RABBITMQ_DEFAULT_USER=test -e RABBITMQ_DEFAULT_PASS=test rabbitmq:3
     */
    "bus initialize".config(enabled = false) {
        val host = "localhost"
        val port = 5672
        val user = "test"
        val password = "test"

        val connectionFactory = CachingConnectionFactory(host, port).apply {
            setUsername(user)
            setPassword(password)
        }

        BusRabbitInitializer(connectionFactory, sourceBus, true)

        val rabbitTemplate = RabbitTemplate(connectionFactory)
        val busNavigator = BusNavigator(sourceBus, rabbitTemplate)

        val messageToSend = "message"

        busNavigator.send(DemoConstants.DEFAULT_BUS_AGENT_SERVICE_NAME, DemoConstants.DEFAULT_BUS_AGENT_SERVICE_INPUT, messageToSend)
        println("Послано $messageToSend")
        val readMessage = busNavigator.read(DemoConstants.DEFAULT_BUS_AGENT_SERVICE_NAME, DemoConstants.DEFAULT_BUS_AGENT_SERVICE_INPUT).getBodyAsString(charSet = "UTF-8")
        println("Прочитано $readMessage")

        readMessage shouldBe messageToSend
    }

})