package codes.spectrum.sources.demo.transport.loader

import codes.spectrum.sources.ISourceHandler
import codes.spectrum.sources.config.IConfig

interface IDemoLoaderSource : ISourceHandler<DemoLoaderContext> {
    object Empty : IDemoLoaderSource {
        override suspend fun execute(context: DemoLoaderContext, config: IConfig){

        }
    }
}
