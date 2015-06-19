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
package grails.plugin.rendering

import grails.plugin.rendering.document.UnknownTemplateException
import grails.plugin.rendering.document.XmlParseException


import org.springframework.mock.web.MockHttpServletResponse

import spock.lang.*
import grails.test.mixin.integration.IntegrationTestMixin
import grails.test.mixin.*

@TestMixin(IntegrationTestMixin)
abstract class RenderingServiceSpec extends Specification {

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

	def "can handle data uris"() {
		when:
		renderer.render(dataUriTemplate)
		then:
		notThrown(Exception)
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

	protected getDataUriTemplate() {
		[template: '/datauri']
	}

	protected createMockResponse() {
		new MockHttpServletResponse()
	}
}
