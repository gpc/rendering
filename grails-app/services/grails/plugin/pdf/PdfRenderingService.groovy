package grails.plugin.pdf

import org.xhtmlrenderer.pdf.ITextRenderer

import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.codehaus.groovy.grails.commons.GrailsResourceUtils
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource

import groovy.text.Template

import javax.servlet.http.HttpServletResponse

class PdfRenderingService {

	static transactional = false
	
	def groovyPagesTemplateEngine

	OutputStream render(Map args, OutputStream outputStream = new ByteArrayOutputStream()) {
		
		// We should eventually support some kind of flag for large file support
		// this would mean using piped input/output streams to avoid copying
		// the large amounts of data. This means we can't store the actual xhtml
		// for error messages though, which is why we don't use piped IO by default.
		
		def xhtmlWriter = new StringWriter()
		createTemplate(args).make(args.model).writeTo(xhtmlWriter)
		def xhtml = xhtmlWriter.toString()
		xhtmlWriter.close()

		if (log.debugEnabled) {
			log.debug("xhtml for $args -- \n ${xhtml}")
		}

		def builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		def xhtmlInputSource = new InputSource(new StringReader(xhtml))
		
		def doc
		try {
			doc = builder.parse(xhtmlInputSource)
		} catch (Exception e) {
			if (log.errorEnabled) {
				log.error("xml parse exception for $xhtml", e)
			}
			throw new XmlParseException(xhtml, e)
		}

		try {
			def renderer = new ITextRenderer()
			renderer.setDocument(doc, null)
			renderer.layout()
			renderer.createPDF(outputStream)
		} catch (Exception e) {
			if (log.errorEnabled) {
				log.error("pdf rendering exception for $xhtml", e)
			}
			throw new RenderingException(xhtml, e)
		}
		
		outputStream
	}

	OutputStream render(Map args, HttpServletResponse response) {
		def baos = render(args)
		response.setContentType("application/pdf")
		response.setContentLength(baos.size())
		if (args.filename) {
			response.setHeader("Content-Disposition", "attachment; filename=\"$args.filename\";")
		}
		baos.writeTo(response.outputStream)
		baos
	}

	protected createTemplate(args) {
		groovyPagesTemplateEngine.createTemplate(resolveGspTemplateResource(args))
	}
		
	protected resolveGspTemplateResource(Map args) {
		assertTemplateArgumentProvided(args)
		
		def resource = groovyPagesTemplateEngine.getResourceForUri(args.template)
		if (!resource || !resource.exists()) {
			if (args.plugin) {
				def plugin = PluginManagerHolder.pluginManager.getGrailsPlugin(args.plugin)
				if (!plugin) {
					throw new IllegalArgumentException("No plugin named '$args.plugin' is installed")
				}
				def pathToView = '/plugins/'+GCU.getScriptName(plugin.name)+'-'+plugin.version+'/'+GrailsResourceUtils.GRAILS_APP_DIR+'/views'
				def uri = GrailsResourceUtils.WEB_INF +pathToView + args.template+".gsp";
				resource = groovyPagesTemplateEngine.getResourceForUri(uri)
			}
		}
		
		if (!resource || !resource.exists()) {
			throwUnknownTemplateError(args)
		}
		
		resource
	}
	
	protected assertTemplateArgumentProvided(Map args) {
		if (!args.template) {
			throw new IllegalArgumentException("The 'template' argument must be specified")
		}
	}

	protected throwUnknownTemplateError(Map args) {
		throw new UnknownTemplateException(args.template, args.plugin)
	}
}
