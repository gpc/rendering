class PdfGrailsPlugin {

	def version = "0.4"
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

	def addRenderPdf(pdfRenderingService, clazz) {
		clazz.metaClass.renderPdf = { Map args ->
			pdfRenderingService.render(args, delegate.response)
			false
		}
	}

	def doWithDynamicMethods = { ctx ->
		application.controllerClasses.each {
			addRenderPdf(ctx.pdfRenderingService, it.clazz)
		}
	}
	
	def onChange = { event ->
		if (application.isControllerClass(event.source)) {
			addRenderPdf(event.ctx.pdfRenderingService, event.source)
		}
	}
}
