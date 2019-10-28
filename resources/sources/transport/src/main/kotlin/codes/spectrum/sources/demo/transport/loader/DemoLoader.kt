package codes.spectrum.sources.demo.transport.loader

import codes.spectrum.konveyor.IKonveyorEnvironment
import codes.spectrum.konveyor.konveyor
import codes.spectrum.sources.config.IConfig
import codes.spectrum.api.SourceState

class DemoLoader : IDemoLoaderSource {

    override suspend fun execute(context: DemoLoaderContext, config: IConfig) {
        try {
            konveyor.exec(context, config as IKonveyorEnvironment)
        } catch (exception: Throwable) {
            context.result.error = exception
            context.result.status = SourceState.GENERAL_ERROR
        }
    }

    companion object {
        val konveyor = konveyor<DemoLoaderContext> {
            exec {
                result.status = SourceState.OK
            }
        }
    }
}