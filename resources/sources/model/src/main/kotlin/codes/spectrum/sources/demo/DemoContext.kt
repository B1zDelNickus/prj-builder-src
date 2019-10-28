package codes.spectrum.sources.demo

import codes.spectrum.sources.SourceContext
import codes.spectrum.sources.demo.model.*

class DemoContext(
    query: DemoRequest = DemoRequest(),
    result: DemoResult = DemoResult()
): SourceContext<DemoQuery, DemoData>(query = query, result = result)




