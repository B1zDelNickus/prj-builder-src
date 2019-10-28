package codes.spectrum.sources.demo.transport.saver

import codes.spectrum.sources.ISourceHandler
import codes.spectrum.sources.config.IConfig

interface IDemoSaverSource : ISourceHandler<DemoSaverContext> {
    object Empty : IDemoSaverSource {
        override suspend fun execute(context: DemoSaverContext, config: IConfig){

        }
    }
}
