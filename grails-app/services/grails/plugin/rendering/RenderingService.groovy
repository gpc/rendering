package grails.plugin.rendering

import javax.servlet.http.HttpServletResponse
import org.w3c.dom.Document

abstract class RenderingService {

	static transactional = false

	def xhtmlDocumentService
	
	abstract protected doRender(Map args, Document document, OutputStream outputStream)
	
	abstract protected getDefaultContentType()

	OutputStream render(Map args, OutputStream outputStream = new ByteArrayOutputStream()) {
		def document = args.document ?: xhtmlDocumentService.createDocument(args)
		render(args, document, outputStream)
	}

	OutputStream render(Map args, Document document, OutputStream outputStream = new ByteArrayOutputStream()) {
		try {
			doRender(args, document, outputStream)
			outputStream
		} catch (Exception e) {
			if (log.errorEnabled) {
				log.error("Rendering exception", e)
			}
			throw new RenderingException(e)
		}
	}
	
	boolean render(Map args, HttpServletResponse response) {
		if (args.bytes) {
			writeToResponse(args, response, args.bytes)
		} else if (args.input) {
			writeToResponse(args, response, args.input)
		} else {
			if (args.stream) {
				configureResponse(args, response)
				render(args, response.outputStream)
			} else {
				writeToResponse(args, response, render(args).toByteArray())
			}
		}
		false
	}

	protected writeToResponse(Map args, HttpServletResponse response, InputStream input) {
		configureResponse(args, response)
		if (args.contentLength > 0) {
			response.setContentLength(args.contentLength)
		}
		response.outputStream << input
	}

	protected writeToResponse(Map args, HttpServletResponse response, byte[] bytes) {
		configureResponse(args, response)
		response.setContentLength(bytes.size())
		response.outputStream << bytes
	}
	
	protected configureResponse(Map args, HttpServletResponse response) {
		setContentType(args, response)
		setResponseHeaders(args, response)
	}
	
	protected setResponseHeaders(Map args, HttpServletResponse response) {
		setContentDisposition(args, response)
	}
	
	protected setContentType(Map args, HttpServletResponse response) {
		response.setContentType(args.contentType ?: getDefaultContentType())
	}

	protected setContentDisposition(Map args, HttpServletResponse response) {
		if (args.filename) {
			response.setHeader("Content-Disposition", "attachment; filename=\"$args.filename\";")
		}
	}

}
