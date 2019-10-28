package codes.spectrum.sources.demo.provider

import codes.spectrum.sources.config.IConfig
import codes.spectrum.sources.config.getOrDefault
import codes.spectrum.sources.demo.DemoContext
import codes.spectrum.sources.demo.IDemoSource

class DemoSource : IDemoSource {
    override suspend fun execute(context: DemoContext, config: IConfig) {
        context.result.data.answer = "${context.query.query.query}   ${config.getOrDefault("DEMO_MESSAGE","processed")}"
    }
}