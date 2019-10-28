package codes.spectrum.sources.demo.transport.loader

import codes.spectrum.sources.SourceContext

class DemoLoaderContext(
    query: DemoLoaderRequest = DemoLoaderRequest(),
    result: DemoLoaderResult = DemoLoaderResult()
) : SourceContext<DemoLoaderQuery, DemoLoaderData>(query = query, result = result)
