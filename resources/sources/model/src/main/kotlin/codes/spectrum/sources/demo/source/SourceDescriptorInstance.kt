package codes.spectrum.sources.demo.source

import codes.spectrum.sources.core.source.SourceDescriptor
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

object SourceDescriptorInstance {

    val Instance: SourceDescriptor by lazy {
        SourceDescriptor(caseDescriptionConverter = ::fromMarkdownToHtmlConverter) {
            //Добавление кейсов
            //+CaseObject

            //Добавление площадок
            //+SystemObject
        }
    }

    private fun fromMarkdownToHtmlConverter(string: String): String =
        HtmlRenderer.builder().build()
            .render(Parser.builder().build().parse(string))
            .replace("\n$".toRegex(), "").replace("\n", "<br>")
}