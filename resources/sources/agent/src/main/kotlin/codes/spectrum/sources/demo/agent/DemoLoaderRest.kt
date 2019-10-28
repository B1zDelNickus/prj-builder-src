package codes.spectrum.sources.demo.agent

import codes.spectrum.sources.core.rest.RestSourceAdapter
import codes.spectrum.sources.demo.transport.loader.*

class DemoLoaderRest(internalSource: IDemoLoaderSource = DemoLoader()):
    RestSourceAdapter<DemoLoaderQuery, DemoLoaderData>(internalSource, DemoLoaderContext::class.java){
    companion object{
        val Instanse by lazy{ DemoLoaderRest() }
    }
}
