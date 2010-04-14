package grails.plugin.pdf

import org.apache.pdfbox.util.PDFTextStripper
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument

import org.codehaus.groovy.grails.plugins.PluginManagerHolder

import grails.plugin.spock.*

class GrailsPdfSpec extends IntegrationSpec {

	def pdfRenderingService
	def grailsApplication
	def pluginManager
	
	def simpleRender() {
		when:
		def lines = extractTextLines(template: '/pdf', model: [var: 1])
		then:
		lines[0] == 'This is a PDF!'
		lines[1] == '1'
	}
	
	def renderTemplateInPlugin() {
		when:
		def lines = extractTextLines(template: '/plugin-pdf', plugin: 'pdf-plugin-test', model: [var: 1])
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

	def badXmlThrowsXmlParseException() {
		when:
		pdfRenderingService.render(template: "/bad-xml")
		then:
		thrown(XmlParseException)
	}

	def createController() {
		grailsApplication.mainContext['PdfTestController']
	}

	def getControllerClass() {
		// Have to do this because it's in the root package
		this.class.classLoader.loadClass('PdfTestController')
	}
	
	def extractTextLines(Map renderArgs) {
		extractTextLines(createPdf(renderArgs))
	}
	
	def extractTextLines(byte[] bytes) {
		extractTextLines(createPdf(new ByteArrayInputStream(bytes)))
	}
	
	def extractTextLines(PDDocument pdf) {
		def lines = new PDFTextStripper().getText(pdf).readLines()
		pdf.close()
		lines
	}

	def createPdf(Map renderArgs) {
		def inStream = new PipedInputStream()
		def outStream = new PipedOutputStream(inStream)
		pdfRenderingService.render(renderArgs, outStream)
		outStream.close()
		createPdf(inStream)
	}
	
	def createPdf(InputStream inputStream) {
		def parser = new PDFParser(inputStream)
		parser.parse()
		parser.getPDDocument()
	}
}
