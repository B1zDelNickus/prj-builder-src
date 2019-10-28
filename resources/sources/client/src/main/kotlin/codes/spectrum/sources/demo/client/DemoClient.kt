package codes.spectrum.sources.demo.client

import codes.spectrum.sources.core.client.ClientBase
import codes.spectrum.sources.demo.model.DemoData
import codes.spectrum.sources.demo.model.DemoQuery


class DemoClient(baseUrl: String = "http://127.0.0.1:8080") : ClientBase<DemoQuery, DemoData>(baseUrl)