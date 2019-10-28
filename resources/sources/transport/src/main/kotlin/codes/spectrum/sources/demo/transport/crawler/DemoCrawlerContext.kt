package codes.spectrum.sources.demo.transport.crawler

import codes.spectrum.sources.SourceContext

class DemoCrawlerContext(
    query: DemoCrawlerRequest = DemoCrawlerRequest(),
    result: DemoCrawlerResult = DemoCrawlerResult()
) : SourceContext<DemoCrawlerQuery, DemoCrawlerData>(query = query, result = result)