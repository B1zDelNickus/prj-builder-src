package codes.spectrum.sources.demo.agent

import codes.spectrum.sources.bus.BusSourceAdapter
import codes.spectrum.sources.demo.transport.saver.*

class DemoSaverBus(internalSource: IDemoSaverSource = DemoSaver()):
    BusSourceAdapter<DemoSaverQuery, DemoSaverData>(internalSource, DemoSaverContext::class.java){
    companion object{
        val Instance by lazy{ DemoSaverBus() }
    }
}
