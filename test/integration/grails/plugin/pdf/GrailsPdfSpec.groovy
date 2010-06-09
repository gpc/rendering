package grails.plugin.pdf

import org.apache.pdfbox.util.PDFTextStripper
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument

import org.codehaus.groovy.grails.plugins.PluginManagerHolder

import org.springframework.mock.web.MockHttpServletResponse

import grails.plugin.spock.*

import spock.lang.*

class GrailsPdfSpec extends IntegrationSpec {

	def pdfRenderingService
	def grailsApplication
	def pluginManager
	
	def simpleRender() {
		when:
		def lines = extractTextLines(simpleView)
		then:
		lines[0] == 'This is a PDF!'
		lines[1] == '1'
	}
	
	def renderViewInPlugin() {
		when:
		def lines = extractTextLines(pluginView)
		then:
		lines[0] == 'This is a PDF from a plugin!'
		lines[1] == '1'
	}
	
	def renderWithNoViewThrowsException() {
		when:
		pdfRenderingService.render([:])
		then:
		thrown(IllegalArgumentException)
	}

	def renderWithUnknownViewThrowsException() {
		when:
		pdfRenderingService.render(view: "asdfasdfasd")
		then:
		thrown(UnknownViewException)
	}
	
	def renderImageWithHeight() {
		when:
		def image = pdfRenderingService.image(getSimpleView(render: [width: 200, height: 100], clip: [width: false, height: false]))
		then:
		image.height == 100
		image.width == 200
	}
	
	def renderImageWithAutoHeight() {
		when:
		def image = pdfRenderingService.image(getSimpleView(render: [width: 200, height: 400]))
		then:
		image.width == 200
		image.height == 200
	}

	def imageResize() {
		when:
		def image = pdfRenderingService.image(getSimpleView(render: [width: 200], resize: resize))
		then:
		image.width == width
		image.height == height
		where:
		resize | width | height 
		[width: 400] | 400 | 400
		[height: 400] | 400 | 400
		[width: 400, height: 200] | 400 | 200
	}

	def imageScale() {
		when:
		def image = pdfRenderingService.image(getSimpleView(render: [width: 200], scale: scale))
		then:
		image.width == width
		image.height == height
		where:
		scale | width | height 
		[width: 2] | 400 | 400
		[height: 2] | 400 | 400
		[width: 4, height: 2] | 800 | 400
	}

	// Excercises the normal non http response code as well
	@Unroll("render #type to http response")
	def renderImagesToHttpResponse() {
		given:
		def response = new MockHttpServletResponse()
		def filename = "test.$type"
		when:
		pdfRenderingService."$type"(getSimpleView(filename: filename, width: 200), response)
		then:
		response.contentAsByteArray.size() > 0
		response.contentType == "image/$type"
		response.getHeader("Content-Disposition") == "attachment; filename=\"$filename\";"
		where:
		type << ["jpeg", "gif", "png"]
	}

	def renderViaController() {
		given:
		def controller = createController()
		when:
		controller.params.id = 2 
		controller.doit()
		def lines = extractTextLines(controller.response.contentAsByteArray)
		then:
		lines[0] == 'This is a PDF!'
	}
	
	def supportReloadingControllerClasses() {
		given:
		createController().class.metaClass.renderPdf = { Map args -> throw new Error() }
		when:
		def controller = createController()
		controller.doit()
		then:
		thrown(Error)
		when:
		PluginManagerHolder.pluginManager.informOfClassChange(getControllerClass())
		controller = createController()
		then:
		controller.doit() == false // false from the return of renderPdf
	}

	protected badXmlThrowsXmlParseException() {
		when:
		pdfRenderingService.render(view: "/bad-xml")
		then:
		thrown(XmlParseException)
	}

	protected createController() {
		grailsApplication.mainContext['PdfTestController']
	}

	protected getControllerClass() {
		// Have to do this because it's in the root package
		this.class.classLoader.loadClass('PdfTestController')
	}
	
	protected extractTextLines(Map renderArgs) {
		extractTextLines(createPdf(renderArgs))
	}
	
	protected extractTextLines(byte[] bytes) {
		extractTextLines(createPdf(new ByteArrayInputStream(bytes)))
	}
	
	protected extractTextLines(PDDocument pdf) {
		protected lines = new PDFTextStripper().getText(pdf).readLines()
		pdf.close()
		lines
	}

	protected createPdf(Map renderArgs) {
		def inStream = new PipedInputStream()
		def outStream = new PipedOutputStream(inStream)
		pdfRenderingService.render(renderArgs, outStream)
		outStream.close()
		createPdf(inStream)
	}
	
	protected createPdf(InputStream inputStream) {
		def parser = new PDFParser(inputStream)
		parser.parse()
		parser.getPDDocument()
	}
	
	protected getSimpleView(Map args = [:]) {
		[view: 'pdf', model: [var: 1]] + args
	}

	protected getPluginView(Map args = [:]) {
		[view: 'plugin-pdf', plugin: 'pdf-plugin-test', model: [var: 1]] + args
	}

}
