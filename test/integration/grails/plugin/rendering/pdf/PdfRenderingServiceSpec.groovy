package grails.plugin.rendering.pdf

import org.apache.pdfbox.util.PDFTextStripper
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument

import grails.plugin.rendering.RenderingServiceSpec

import grails.plugin.spock.*

import spock.lang.*

class PdfRenderingServiceSpec extends RenderingServiceSpec {

	def pdfRenderingService
	
	def getRenderer() {
		pdfRenderingService
	}
	
	protected extractTextLines(Map renderArgs) {
		extractTextLines(createPdf(renderArgs))
	}
	
	protected extractTextLines(byte[] bytes) {
		extractTextLines(createPdf(new ByteArrayInputStream(bytes)))
	}
	
	protected extractTextLines(PDDocument pdf) {
		protected lines = new PDFTextStripper().getText(pdf).readLines()
		pdf.close()
		lines
	}

	protected createPdf(Map renderArgs) {
		def inStream = new PipedInputStream()
		def outStream = new PipedOutputStream(inStream)
		pdfRenderingService.render(renderArgs, outStream)
		outStream.close()
		createPdf(inStream)
	}
	
	protected createPdf(InputStream inputStream) {
		def parser = new PDFParser(inputStream)
		parser.parse()
		parser.getPDDocument()
	}

}
