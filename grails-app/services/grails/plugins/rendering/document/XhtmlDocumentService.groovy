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
package grails.plugin.rendering.document

import grails.util.GrailsUtil


import org.w3c.dom.Document
import org.xhtmlrenderer.resource.XMLResource
import org.xml.sax.InputSource
import grails.util.Holders

class XhtmlDocumentService {

	static transactional = false

	def groovyPagesTemplateEngine
	def groovyPagesUriService
	def grailsApplication

	Document createDocument(Map args) {
		createDocument(generateXhtml(args))
	}

	protected createDocument(String xhtml) {
		try {
			createDocument(new InputSource(new StringReader(xhtml)))
		} catch (XmlParseException e) {
			if (log.errorEnabled) {
				GrailsUtil.deepSanitize(e)
				log.error("caught xml parse exception for xhtml: $xhtml", e)
			}
			throw new XmlParseException(xhtml, e)
		}
	}

	protected createDocument(InputSource xhtml) {
		try {
			XMLResource.load(xhtml).document
		} catch (Exception e) {
			if (log.errorEnabled) {
				GrailsUtil.deepSanitize(e)
				log.error("xml parse exception for input source: $xhtml", e)
			}
			throw new XmlParseException(xhtml, e)
		}
	}

	protected generateXhtml(Map args) {
		def xhtmlWriter = new StringWriter()

		RenderEnvironment.with(grailsApplication.mainContext, xhtmlWriter) {
			createTemplate(args).make(args.model).writeTo(xhtmlWriter)
		}

		def xhtml = xhtmlWriter.toString()
		xhtmlWriter.close()

		if (log.debugEnabled) {
			log.debug("xhtml for $args -- \n ${xhtml}")
		}

		xhtml
	}

	protected createTemplate(Map args) {
		if (!args.template) {
			throw new IllegalArgumentException("The 'template' argument must be specified")
		}
		def templateName = args.template

		if (templateName.startsWith("/")) {
			if (!args.controller) {
				args.controller = ""
			}
		} else {
			if (!args.controller) {
				throw new IllegalArgumentException("template names must start with '/' if controller is not provided")
			}
		}

		def contextPath = getContextPath(args)
		def controllerName = args.controller instanceof CharSequence ? args.controller : groovyPagesUriService.getLogicalControllerName(args.controller)
		def templateUri = groovyPagesUriService.getTemplateURI(controllerName, templateName)
		def uris = ["$contextPath/$templateUri", "$contextPath/grails-app/views/$templateUri"] as String[]
		def template = groovyPagesTemplateEngine.createTemplateForUri(uris)

		if (!template) {
			throw new UnknownTemplateException(args.template, args.plugin)
		}

		template
	}

	protected getContextPath(args) {
		def contextPath = args.contextPath?.toString() ?: ""
		def pluginName = args.plugin

		if (pluginName) {
			def plugin = Holders.pluginManager.getGrailsPlugin(pluginName)
			if (plugin && !plugin.isBasePlugin()) {
				contextPath = plugin.pluginPath
			}
		}

		contextPath
	}
}
