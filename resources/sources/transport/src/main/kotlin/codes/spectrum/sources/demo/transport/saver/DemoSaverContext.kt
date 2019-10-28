package codes.spectrum.sources.demo.transport.saver

import codes.spectrum.sources.SourceContext

class DemoSaverContext(
    query: DemoSaverRequest = DemoSaverRequest(),
    result: DemoSaverResult = DemoSaverResult()
) : SourceContext<DemoSaverQuery, DemoSaverData>(query = query, result = result)
