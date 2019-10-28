package codes.spectrum.sources.demo

object DemoConstants {
    const val SOURCE_ID = "SOURCE_ID"

    const val EXCHANGE_NAME = "$SOURCE_ID-exchange"

    const val DEFAULT_BUS_AGENT_ROUTE = "$SOURCE_ID.agent"
    const val DEFAULT_BUS_AGENT_QUEUE = "$SOURCE_ID-agent"

    const val DEFAULT_BUS_AGENT_ERROR_ROUTE = "$SOURCE_ID.agent.error"
    const val DEFAULT_BUS_AGENT_ERROR_QUEUE = "$SOURCE_ID-agent-error"

    const val DEFAULT_BUS_AGENT_SERVICE_NAME = "$SOURCE_ID-agent-service"
    const val DEFAULT_BUS_AGENT_SERVICE_INPUT = "$SOURCE_ID-agent-input"
    const val DEFAULT_BUS_AGENT_SERVICE_ERROR = "$SOURCE_ID-agent-error"

}