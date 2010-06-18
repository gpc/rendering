class RenderingController {

	def pdf = { 
		renderPdf(template)
	}

	def jpeg = {
		renderJpeg(template + [width: 200])
	}

	def gif = {
		renderGif(template + [render: [width: 600, height: 200], clip: [height: true, width: true], resize: [width: 600, height: 200]])
	}

	def png = {
		renderPng(template + [width: 100])
	}
	
	protected getTemplate() {
		[template: '/simple', model: [var: params.id]]
	}
}