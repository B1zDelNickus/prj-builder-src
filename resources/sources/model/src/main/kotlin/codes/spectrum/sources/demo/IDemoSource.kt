package codes.spectrum.sources.demo

import codes.spectrum.sources.ISourceHandler
import codes.spectrum.sources.config.IConfig

interface IDemoSource : ISourceHandler<DemoContext> {
    object Empty : IDemoSource {
        override suspend fun execute(context: DemoContext, config: IConfig) {

        }
    }
}