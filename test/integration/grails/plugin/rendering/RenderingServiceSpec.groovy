package grails.plugin.rendering

import grails.plugin.rendering.document.UnknownTemplateException
import grails.plugin.rendering.document.XmlParseException

import org.apache.pdfbox.util.PDFTextStripper
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument

import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.springframework.mock.web.MockHttpServletResponse

import grails.plugin.spock.*

import spock.lang.*

abstract class RenderingServiceSpec extends IntegrationSpec {

	def grailsApplication
	def pluginManager
	
	abstract getRenderer()
	
	def simpleRender() {
		when:
		renderer.render(simpleTemplate)
		then:
		notThrown(Exception)
	}
	
	def renderTemplateInPlugin() {
		when:
		renderer.render(pluginTemplate)
		then:
		notThrown(Exception)
	}
	
	def renderWithNoTemplateThrowsException() {
		when:
		renderer.render([:])
		then:
		thrown(IllegalArgumentException)
	}

	def renderWithUnknownTemplateThrowsException() {
		when:
		renderer.render(template: "/asdfasdfasd")
		then:
		thrown(UnknownTemplateException)
	}
	

	def renderToResponse() {
		given:
		def response = createMockResponse()
		when:
		renderer.render(simpleTemplate, response)
		then:
		response.contentAsByteArray.size() > 0
	}

	def renderToResponseViaBytes() {
		given:
		def response = createMockResponse()
		when:
		def bytes = renderer.render(simpleTemplate).toByteArray()
		def args = simpleTemplate.clone()
		args.remove('template')
		args.bytes = bytes
		renderer.render(args, response)
		then:
		response.contentAsByteArray.size() > 0
	}

	def badXmlThrowsXmlParseException() {
		when:
		renderer.render(template: "/bad-xml")
		then:
		thrown(XmlParseException)
	}

	protected createController() {
		grailsApplication.mainContext['RenderingController']
	}
	
	protected getSimpleTemplate(Map args = [:]) {
		[template: '/simple', model: [var: 1]] + args
	}

	protected getPluginTemplate(Map args = [:]) {
		[template: '/plugin-pdf', plugin: 'pdf-plugin-test', model: [var: 1]] + args
	}

	protected createMockResponse() {
		new MockHttpServletResponse()
	}
}
