package codes.spectrum.sources.demo.transport.crawler

import codes.spectrum.konveyor.IKonveyorEnvironment
import codes.spectrum.konveyor.konveyor
import codes.spectrum.sources.config.IConfig
import codes.spectrum.api.SourceState

class DemoCrawler : IDemoCrawlerSource {
    override suspend fun execute(context: DemoCrawlerContext, config: IConfig) {
        try {
            konveyor.exec(context, config as IKonveyorEnvironment)
        } catch (exception: Throwable) {
            context.result.error = exception
            context.result.status = SourceState.GENERAL_ERROR
        }
    }

    companion object {
        val konveyor = konveyor<DemoCrawlerContext> {
            exec {
                result.status = SourceState.OK
            }
        }
    }
}