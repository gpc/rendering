class PdfGrailsPlugin {

	def version = "0.5-SNAPSHOT"
	def grailsVersion = "1.2.0 > *"
	def dependsOn = [:]
	
	def pluginExcludes = [
		"grails-app/views/**/*",
		"grails-app/controllers/**/*",
		"plugins/**/*",
	]
	
	def observe = [
		"controllers"
	]
	
	def loadAfter = [
		"controllers"
	]

	def author = "Luke Daley & Randall Dietz "
	def authorEmail = "ld@ldaley.com & rdietz@sp.com.au"
	def title = "Grails PDF rendering"
	def description = 'Provides the ability to render GSPs as PDFs'
	def documentation = "http://grails.org/plugin/pdf"

	def addRenderMethods(pdfRenderingService, clazz) {
		clazz.metaClass.with {
			
			renderPdf = { Map args ->
				pdfRenderingService.render(args, delegate.response)
				false
			}

			renderPdfImage = { Map args, String imageType, String contentType ->
				pdfRenderingService.image(args, imageType, contentType, delegate.response)
				false
			}

			renderPdfJpeg = { Map args ->
				pdfRenderingService.jpeg(args, delegate.response)
				false
			}

			renderPdfGif = { Map args ->
				pdfRenderingService.gif(args, delegate.response)
				false
			}

			renderPdfPng = { Map args ->
				pdfRenderingService.png(args, delegate.response)
				false
			}
			
		}
	}

	def doWithDynamicMethods = { ctx ->
		application.controllerClasses.each {
			addRenderMethods(ctx.pdfRenderingService, it.clazz)
		}
	}
	
	def onChange = { event ->
		if (application.isControllerClass(event.source)) {
			addRenderMethods(event.ctx.pdfRenderingService, event.source)
		}
	}
}
