package codes.spectrum.sources.demo.model

import codes.spectrum.sources.DebugMode
import codes.spectrum.sources.SourceQuery

class DemoRequest(
    query: DemoQuery = DemoQuery(),
    debugMode: DebugMode? = null,
    val caseCode: String = ""
): SourceQuery<DemoQuery>(query = query, debug = debugMode)