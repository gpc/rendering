package grails.plugin.rendering.pdf

import grails.plugin.rendering.RenderingService
import org.xhtmlrenderer.pdf.ITextRenderer
import org.w3c.dom.Document

class PdfRenderingService extends RenderingService {

	static transactional = false
	
	protected doRender(Map args, Document document, OutputStream outputStream) {
		def renderer = new ITextRenderer()
		renderer.setDocument(document, null)
		renderer.layout()
		renderer.createPDF(outputStream)
	}

	protected getDefaultContentType() {
		"application/pdf"
	}
}
