package codes.spectrum.sources.demo.agent

import codes.spectrum.sources.core.SourceDefinition
import codes.spectrum.sources.core.rest.runWithStateInfo
import codes.spectrum.sources.core.rest.setupCors
import codes.spectrum.sources.core.rest.setupNegotiation
import io.ktor.application.Application
import io.ktor.routing.route
import io.ktor.routing.routing

val module: Application.() -> Unit = {
    setupNegotiation()
    setupCors()

    routing {
        route(SourceDefinition.Instance.agentApiRoot) {
            route("crawler") {
                handle {
                    runWithStateInfo { DemoCrawlerRest.Instanse.execute(this) }
                }
            }
            route("loader") {
                handle {
                    runWithStateInfo { DemoLoaderRest.Instanse.execute(this) }
                }
            }
            route("saver") {
                handle {
                    runWithStateInfo { DemoSaverRest.Instanse.execute(this) }
                }
            }
        }
    }
}