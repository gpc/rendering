class PdfTestController {

	def doit = { 
		renderPdf(template)
	}

	def jpeg = {
		renderPdfJpeg(template + [width: 200])
	}

	def gif = {
		renderPdfGif(template + [render: [width: 600, height: 200], clip: [height: true, width: true], resize: [width: 600, height: 200]])
	}

	def png = {
		renderPdfPng(template + [width: 100])
	}
	
	protected getTemplate() {
		[template: 'pdf', model: [var: params.id]]
	}
}