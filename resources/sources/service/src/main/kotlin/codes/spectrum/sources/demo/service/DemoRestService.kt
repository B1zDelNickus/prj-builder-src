package codes.spectrum.sources.demo.service

import codes.spectrum.sources.core.rest.RestSourceAdapter
import codes.spectrum.sources.demo.DemoContext
import codes.spectrum.sources.demo.IDemoSource
import codes.spectrum.sources.demo.model.DemoData
import codes.spectrum.sources.demo.model.DemoQuery
import codes.spectrum.sources.demo.provider.DemoSource

class DemoRestService(internalSource: IDemoSource = DemoSource()):
    RestSourceAdapter<DemoQuery, DemoData>(internalSource, DemoContext::class.java){
    companion object{
        val Instanse by lazy{ DemoRestService() }
    }
}