class PdfTestController {

	def doit = { 
		renderPdf(template)
	}

	def jpeg = {
		renderPdfJpeg(template + [width: 200])
	}

	def gif = {
		renderPdfGif(template + [scale: [width: 100]])
	}

	def png = {
		renderPdfPng(template + [width: 100])
	}
	
	protected getTemplate() {
		[template: '/pdf', model: [var: params.id]]
	}
}