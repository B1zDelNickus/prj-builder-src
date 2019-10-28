package codes.spectrum.sources.demo.transport.saver

import codes.spectrum.sources.DebugMode
import codes.spectrum.sources.SourceQuery

class DemoSaverRequest(
    query: DemoSaverQuery = DemoSaverQuery(),
    debug: DebugMode = DebugMode(),
    timeout: Long = -1L
) : SourceQuery<DemoSaverQuery>(query = query) {
    init {
        this.debug = debug
        this.timeout = timeout
    }
}