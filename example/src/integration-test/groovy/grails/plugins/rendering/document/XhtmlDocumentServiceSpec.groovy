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
package grails.plugins.rendering.document

import grails.core.GrailsApplication
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.w3c.dom.Document

@Integration
class XhtmlDocumentServiceSpec extends ProductionEnvironmentSpec {

	XhtmlDocumentService xhtmlDocumentService
	GrailsApplication grailsApplication

	def "render a taglib template outside web request"() {
		when:
		Document document = xhtmlDocumentService.createDocument(template: '/taglib', model: [value: 'from a job'])

		then:
		hiddenFieldValue(document) == 'from a job'
		RequestContextHolder.requestAttributes == null
	}

	def "render taglib template on a background thread"() {
		given:
		Document document = null
		Throwable error = null

		when:
		def thread = Thread.start {
			try {
				document = xhtmlDocumentService.createDocument(template: '/taglib', model: [value: 'from a thread'])
			} catch (Throwable t) {
				error = t
			}
		}
		thread.join(30_000)

		then:
		!thread.alive
		error == null
		hiddenFieldValue(document) == 'from a thread'
	}

	def "does not write into current request's output"() {
		given:
		GrailsWebRequest original = GrailsWebMockUtil.bindMockWebRequest(grailsApplication.mainContext)
		def requestOut = new StringWriter()
		original.out = requestOut

		when:
		Document document = xhtmlDocumentService.createDocument(template: '/taglib', model: [value: 'in a request'])

		then:
		hiddenFieldValue(document) == 'in a request'
		original.out.is(requestOut)
		requestOut.toString() == ''
		RequestContextHolder.requestAttributes.is(original)
	}

	def "renders template that reads caller's request state"() {
		given:
		GrailsWebRequest original = GrailsWebMockUtil.bindMockWebRequest(grailsApplication.mainContext)
		MockHttpServletRequest request = original.currentRequest as MockHttpServletRequest
		request.setAttribute('foo', 'bar')
		request.addParameter('q', 'query')
		request.session.setAttribute('user', 'alice')
		original.params.id = '42'

		when:
		Document document = xhtmlDocumentService.createDocument(template: '/requestState')

		then:
		paragraphs(document) == ['bar', 'query', '42', 'alice']
		original.currentResponse.contentType == null
		RequestContextHolder.requestAttributes.is(original)
	}

	private static String hiddenFieldValue(Document document) {
		document.getElementsByTagName('input').item(0).getAttribute('value')
	}

	private static List<String> paragraphs(Document document) {
		def nodes = document.getElementsByTagName('p')
		(0..<nodes.length).collect { nodes.item(it).textContent }
	}
}
