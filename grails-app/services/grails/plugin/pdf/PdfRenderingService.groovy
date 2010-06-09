package grails.plugin.pdf

import org.xhtmlrenderer.pdf.ITextRenderer
import org.xhtmlrenderer.simple.Graphics2DRenderer

import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.codehaus.groovy.grails.commons.GrailsResourceUtils
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

import org.xhtmlrenderer.resource.XMLResource
import org.xml.sax.InputSource

import groovy.text.Template

import javax.servlet.http.HttpServletResponse

import java.awt.*
import java.awt.image.*
import java.awt.geom.AffineTransform
import javax.imageio.ImageIO

class PdfRenderingService {

	// We should eventually support some kind of flag for large file support
	// this would mean using piped input/output streams to avoid copying
	// the large amounts of data. This means we can't store the actual xhtml
	// for error messages though, which is why we don't use piped IO by default.
	
	static transactional = false
	
	static DEFAULT_BUFFERED_IMAGE_TYPE = BufferedImage.TYPE_INT_ARGB
	
	def groovyPagesTemplateEngine
	def groovyPagesUriService
	
	OutputStream render(Map args, OutputStream outputStream = new ByteArrayOutputStream()) {
		def doc = generateDoc(args)

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
		writeToResponse(args, render(args), "application/pdf", response)
	}

	BufferedImage image(Map args) {
		def bufferedImageType = args.bufferedImageType ?: DEFAULT_BUFFERED_IMAGE_TYPE
		
		def renderWidth = args.render?.width?.toInteger() ?: 10
		def renderHeight = args.render?.height?.toInteger()
		
		def autosizeWidth = args.autosize?.width == null || args.autosize?.width == true
		def autosizeHeight = args.autosize?.height == null || args.autosize?.height == true
		
		def doc = generateDoc(args)
		
		def renderer = new Graphics2DRenderer()
		renderer.setDocument(doc, args.base)
		
		def imageWidth = renderWidth
		def imageHeight = renderHeight
		def needsLayout = true
		
		if (!renderHeight || autosizeWidth || autosizeHeight) {
			def tempRenderHeight = renderHeight ?: 10000
			def dim = new Dimension(renderWidth, tempRenderHeight)
			
			// do layout with temp buffer to calculate height
			def tempImage = new BufferedImage(dim.width.intValue(), dim.height.intValue(), bufferedImageType)
			def tempGraphics = tempImage.graphics
			renderer.layout(tempGraphics, dim)
			needsLayout = false
			tempGraphics.dispose()
			
			if (autosizeWidth) {
				imageWidth = renderer.minimumSize.width.intValue()
			} 
			if (!renderHeight || autosizeHeight) {
				imageHeight = renderer.minimumSize.height.intValue()
			}
		}

		def image = new BufferedImage(imageWidth, imageHeight, bufferedImageType)
		def graphics = image.graphics
		if (needsLayout) {
			renderer.layout(graphics, new Dimension(imageWidth, imageHeight))
		}
		renderer.render(graphics)
		graphics.dispose()
		
		if (args.scale) {
			scale(image, args.scale, bufferedImageType)
		} else if (args.resize) {
			resize(image, args.resize, bufferedImageType)
		} else {
			image
		}
	}
	
	protected scale(image, Map scaleArgs, bufferedImageType) {
		def width = scaleArgs.width?.toInteger()
		def height = scaleArgs.height?.toInteger()
		
		if (width && height) {
			scale(image, width, height, bufferedImageType)
		} else if (width && !height) {
			scale(image, width, width, bufferedImageType)
		} else if (!width && height) {
			scale(image, height, height, bufferedImageType)
		} else {
			throw new IllegalStateException("Unhandled scale height/width combination")
		}
	}
	
	protected resize(image, Map resizeArgs, bufferedImageType) {
		def width = resizeArgs.width?.toInteger()
		def height = resizeArgs.height?.toInteger()
		
		if (width && height) {
			resize(image, width, height, bufferedImageType)
		} else if (width && !height) {
			height = (image.height * (width / image.width)).toInteger()
			resize(image, width, height, bufferedImageType)
		} else if (!width && height) {
			width = (image.width * (height / image.height)).toInteger()
			resize(image, width, height, bufferedImageType)
		} else {
			throw new IllegalStateException("Unhandled resize height/width combination")
		}
	}
	
	protected resize(image, width, height, bufferedImageType) {
		def widthScale = width / image.width
		def heightScale = height / image.height
		
		doScaleTransform(image, width, height, widthScale, heightScale, bufferedImageType)
	}
	
	protected scale(image, widthScale, heightScale, bufferedImageType) {
		def width = image.width * widthScale
		def height = image.height * heightScale
		
		doScaleTransform(image, width, height, widthScale, heightScale, bufferedImageType)
	}
	
	protected doScaleTransform(image, width, height, widthScale, heightScale, bufferedImageType) {
		def scaled = new BufferedImage(width, height, bufferedImageType)
		
		def graphics = scaled.createGraphics()
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
 		def transform = AffineTransform.getScaleInstance(widthScale, heightScale)
		graphics.drawRenderedImage(image, transform)
		graphics.dispose()
		
		scaled
	}
	
	OutputStream image(Map args, String type, OutputStream outputStream = new ByteArrayOutputStream()) {
		if (ImageIO.write(image(args), type, outputStream)) {
			outputStream
		} else {
			throw new IllegalArgumentException("ImageIO.write() failed to find writer for type '$type'")
		}
	}
	
	protected image(Map args, String type, String contentType, HttpServletResponse response) {
		writeToResponse(args, image(args, type), contentType, response)
	}
	
	protected writeToResponse(Map args, ByteArrayOutputStream bytes, String contentType, HttpServletResponse response) {
		setFilename(args, response)
		response.setContentType(contentType)
		response.setContentLength(bytes.size())
		bytes.writeTo(response.outputStream)
		bytes
	}
	
	OutputStream jpeg(Map args, OutputStream outputStream = new ByteArrayOutputStream()) {
		image(args, "jpeg", outputStream)
	}

	OutputStream jpeg(Map args, HttpServletResponse response) {
		image(args, "jpeg", "image/jpeg", response)
	}
	
	OutputStream gif(Map args, OutputStream outputStream = new ByteArrayOutputStream()) {
		image(args, "gif", outputStream)
	}

	OutputStream gif(Map args, HttpServletResponse response) {
		image(args, "gif", "image/gif", response)
	}

	OutputStream png(Map args, OutputStream outputStream = new ByteArrayOutputStream()) {
		image(args, "png", outputStream)
	}
	
	OutputStream png(Map args, HttpServletResponse response) {
		image(args, "png", "image/png", response)
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

	protected setFilename(Map args, HttpServletResponse response) {
		if (args.filename) {
			response.setHeader("Content-Disposition", "attachment; filename=\"$args.filename\";")
		}
	}
		
	protected generateDoc(Map args) {
		generateDoc(generateXhtml(args))
	}
	
	protected generateDoc(String xhtml) {
		def doc
		try {
			doc = XMLResource.load(new InputSource(new StringReader(xhtml))).document
		} catch (Exception e) {
			if (log.errorEnabled) {
				log.error("xml parse exception for $xhtml", e)
			}
			throw new XmlParseException(xhtml, e)
		}
		doc
	}

	protected createTemplate(args) {
		assertTemplateArgumentProvided(args)
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
			throwUnknownTemplateError(args)
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
	
	protected assertTemplateArgumentProvided(Map args) {
		if (!args.template) {
			throw new IllegalArgumentException("The 'template' argument must be specified")
		}
	}

	protected throwUnknownTemplateError(Map args) {
		throw new UnknownTemplateException(args.template, args.plugin)
	}
}
