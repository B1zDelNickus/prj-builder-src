package codes.spectrum.sources.demo.transport.crawler

import codes.spectrum.sources.ISourceHandler
import codes.spectrum.sources.config.IConfig

interface IDemoCrawlerSource : ISourceHandler<DemoCrawlerContext> {
    object Empty : IDemoCrawlerSource {
        override suspend fun execute(context: DemoCrawlerContext, config: IConfig){

        }
    }
}
