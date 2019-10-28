package codes.spectrum.sources.demo.model

import codes.spectrum.sources.SourceResult

class DemoResult(
    data: DemoData = DemoData()
): SourceResult<DemoData>(data = data)