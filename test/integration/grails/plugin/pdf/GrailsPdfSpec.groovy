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
		def lines = extractTextLines(simpleTemplate)
		then:
		lines[0] == 'This is a PDF!'
		lines[1] == '1'
	}
	
	def renderTemplateInPlugin() {
		when:
		def lines = extractTextLines(pluginTemplate)
		then:
		lines[0] == 'This is a PDF from a plugin!'
		lines[1] == '1'
	}
	
	def renderWithNoTemplateThrowsException() {
		when:
		pdfRenderingService.render([:])
		then:
		thrown(IllegalArgumentException)
	}

	def renderWithUnknownTemplateThrowsException() {
		when:
		pdfRenderingService.render(template: "asdfasdfasd")
		then:
		thrown(UnknownTemplateException)
	}
	
	def renderImageWithHeight() {
		when:
		def image = pdfRenderingService.image(getSimpleTemplate(width: 200, height: 200))
		then:
		image.height == 200
		image.width == 200
	}
	
	def renderImageWithAutoHeight() {
		when:
		def image = pdfRenderingService.image(getSimpleTemplate(width: 200))
		then:
		image.width == 200
		image.height == 90 // just checking that it's something is all we can do
	}

	// Excercises the normal non http response code as well
	@Unroll("render #type to http response")
	def renderImagesToHttpResponse() {
		given:
		def response = new MockHttpServletResponse()
		def filename = "test.$type"
		when:
		pdfRenderingService."$type"(getSimpleTemplate(filename: filename, width: 200), response)
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
		pdfRenderingService.render(template: "/bad-xml")
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
	
	protected getSimpleTemplate(Map args = [:]) {
		[template: '/pdf', model: [var: 1]] + args
	}

	protected getPluginTemplate(Map args = [:]) {
		[template: '/plugin-pdf', plugin: 'pdf-plugin-test', model: [var: 1]] + args
	}

}
