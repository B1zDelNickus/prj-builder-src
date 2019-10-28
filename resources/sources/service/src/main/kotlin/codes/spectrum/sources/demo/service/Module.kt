package codes.spectrum.sources.demo.service

import codes.spectrum.sources.core.rest.defaultRouting
import codes.spectrum.sources.core.rest.setupCors
import codes.spectrum.sources.core.rest.setupNegotiation
import io.ktor.application.Application

val module: Application.() -> Unit = {
    setupNegotiation()
    setupCors()
    defaultRouting { DemoRestService.Instanse.execute(this) }
}