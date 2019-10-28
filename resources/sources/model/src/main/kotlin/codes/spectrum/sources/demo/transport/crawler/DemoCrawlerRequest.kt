package codes.spectrum.sources.demo.transport.crawler

import codes.spectrum.sources.DebugMode
import codes.spectrum.sources.SourceQuery

class DemoCrawlerRequest(
    query: DemoCrawlerQuery = DemoCrawlerQuery(),
    debug: DebugMode = DebugMode(),
    timeout: Long = -1L
) : SourceQuery<DemoCrawlerQuery>(query = query) {
    init {
        this.debug = debug
        this.timeout = timeout
    }
}