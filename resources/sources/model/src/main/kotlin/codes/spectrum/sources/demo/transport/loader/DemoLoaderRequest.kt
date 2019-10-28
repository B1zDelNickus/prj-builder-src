package codes.spectrum.sources.demo.transport.loader

import codes.spectrum.sources.DebugMode
import codes.spectrum.sources.SourceQuery

class DemoLoaderRequest(
    query: DemoLoaderQuery = DemoLoaderQuery(),
    debug: DebugMode = DebugMode(),
    timeout: Long = -1L
) : SourceQuery<DemoLoaderQuery>(query = query) {
    init {
        this.debug = debug
        this.timeout = timeout
    }
}