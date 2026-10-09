/*
 * Copyright 2010 Grails Plugin Collective
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugins.rendering.pdf

import grails.plugins.rendering.RenderingServiceSpec
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.text.PDFTextStripper

class PdfRenderingServiceSpec extends RenderingServiceSpec {

	def pdfRenderingService

	def getRenderer() {
		pdfRenderingService
	}

	def "pdf contains rendered template text"() {
		when:
		def lines = extractTextLines(simpleTemplate)

		then:
		lines*.trim().containsAll(['This is a PDF!', '1'])
	}

	def "data url image is embedded in pdf"() {
		when:
		def image = loadPdf(dataUriTemplate).withCloseable { PDDocument pdf ->
			def resources = pdf.getPage(0).resources
			resources.XObjectNames
					.collect { resources.getXObject(it) }
					.findAll { it instanceof PDImageXObject }
					.collect { PDImageXObject img -> [img.width, img.height] }
		}

		then:
		image == [[5, 5]]
	}

	def "encoding pdf keeps polish characters"() {
		when:
		def text = extractTextLines(template: '/encoding-test', base: "http://localhost:${serverPort}/rendering").join('\n')

		then:
		text.contains('łłłłłasdfasdfłłł')
		text.contains('Płeć')
	}

	def "encoding pdf embeds the custom font"() {
		when:
		def embeddedFontNames = loadPdf( encodingTemplate).withCloseable { PDDocument pdf ->
			def resources = pdf.getPage(0).resources
			resources.fontNames
					.collect { resources.getFont(it) }
					.findAll { it.embedded }
					*.name
		}

		then:
		embeddedFontNames.any { it.endsWith('ArialUnicodeMS') }
	}

	protected Map getEncodingTemplate() {
		[template: '/encoding-test', base: "http://localhost:${serverPort}/rendering"]
	}

	protected byte[] renderPdfBytes(Map renderArgs) {
		(pdfRenderingService.render(renderArgs) as ByteArrayOutputStream).toByteArray()
	}

	protected PDDocument loadPdf(Map renderArgs) {
		Loader.loadPDF(renderPdfBytes(renderArgs))
	}

	protected List<String> extractTextLines(Map renderArgs) {
		loadPdf(renderArgs).withCloseable { PDDocument pdf ->
			new PDFTextStripper().getText(pdf).readLines()
		}
	}
}
