package codes.spectrum.sources.demo.agent

import codes.spectrum.sources.demo.transport.saver.*
import codes.spectrum.sources.core.rest.RestSourceAdapter

class DemoSaverRest(internalSource: IDemoSaverSource = DemoSaver()):
    RestSourceAdapter<DemoSaverQuery, DemoSaverData>(internalSource, DemoSaverContext::class.java){
    companion object{
        val Instanse by lazy{ DemoSaverRest() }
    }
}
