package grails.plugin.rendering.document

import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xhtmlrenderer.resource.XMLResource
import groovy.text.Template

import org.codehaus.groovy.grails.plugins.PluginManagerHolder

import grails.util.GrailsUtil

class XhtmlDocumentService {

	static transactional = false

	def groovyPagesTemplateEngine
	def groovyPagesUriService
	
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
		createTemplate(args).make(args.model).writeTo(xhtmlWriter)
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
			def plugin = PluginManagerHolder.pluginManager.getGrailsPlugin(pluginName)
			if (plugin && !plugin.isBasePlugin()) {
				contextPath = plugin.pluginPath
			}
		}
		
		contextPath
	}

}