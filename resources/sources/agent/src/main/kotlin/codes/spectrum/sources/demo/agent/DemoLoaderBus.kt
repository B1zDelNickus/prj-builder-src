package codes.spectrum.sources.demo.agent

import codes.spectrum.sources.bus.BusSourceAdapter
import codes.spectrum.sources.demo.transport.loader.*

class DemoLoaderBus(internalSource: IDemoLoaderSource = DemoLoader()):
    BusSourceAdapter<DemoLoaderQuery, DemoLoaderData>(internalSource, DemoLoaderContext::class.java){
    companion object{
        val Instance by lazy{ DemoLoaderBus() }
    }
}
