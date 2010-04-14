class PdfTestController {

	def doit = { 
		renderPdf(template: '/pdf', model: [var: params.id])
	}

}