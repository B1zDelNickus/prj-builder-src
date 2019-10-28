package codes.spectrum.sources.demo.service

import codes.spectrum.sources.core.SourceDefinition
import codes.spectrum.sources.core.service.runServer
import org.slf4j.LoggerFactory


val InstanceLog = LoggerFactory.getLogger(SourceDefinition.Instance.serviceLoggerName)
fun main() {
    try {
        runServer(module = module)
    } catch(e:Throwable) {
        InstanceLog.error("Error source ${SourceDefinition.Instance.name} (${SourceDefinition.Instance.code})",e)
    } finally {
        InstanceLog.info("Finish source ${SourceDefinition.Instance.name} (${SourceDefinition.Instance.code})")
    }
}