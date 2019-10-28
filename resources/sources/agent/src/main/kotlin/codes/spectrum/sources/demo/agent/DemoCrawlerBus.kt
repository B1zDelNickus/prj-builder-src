package codes.spectrum.sources.demo.agent

import codes.spectrum.sources.bus.BusSourceAdapter
import codes.spectrum.sources.demo.transport.crawler.*

class DemoCrawlerBus(internalSource: IDemoCrawlerSource = DemoCrawler()):
    BusSourceAdapter<DemoCrawlerQuery, DemoCrawlerData>(internalSource, DemoCrawlerContext::class.java){
    companion object{
        val Instance by lazy{ DemoCrawlerBus() }
    }
}
