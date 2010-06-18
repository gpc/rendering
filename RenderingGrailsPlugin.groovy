class RenderingGrailsPlugin {

	def version = "0.1-SNAPSHOT"
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

	def author = "Grails Plugin Collective"
	def authorEmail = "grails.plugin.collective@gmail.com"
	def title = "Grails Rendering"
	def description = 'Provides rendering of GSPs as PDFs, JPEGs, GIFs and PNGs'
	def documentation = "http://gpc.github.com/grails-rendering"

	def renderMethodTemplate = { ctx, rendererName, Map args ->
		ctx[rendererName].render(args, delegate.response)
		false
	}
	
	def addRenderMethods(ctx, clazz) {
		clazz.metaClass.with {
			renderPdf = this.renderMethodTemplate.curry(ctx, 'pdfRenderingService')
			renderJpeg = this.renderMethodTemplate.curry(ctx, 'jpegRenderingService')
			renderGif = this.renderMethodTemplate.curry(ctx, 'gifRenderingService')
			renderPng = this.renderMethodTemplate.curry(ctx, 'pngRenderingService')
		}
	}

	def doWithDynamicMethods = { ctx ->
		application.controllerClasses.each {
			addRenderMethods(ctx, it.clazz)
		}
	}
	
	def onChange = { event ->
		if (application.isControllerClass(event.source)) {
			addRenderMethods(event.ctx, event.source)
		}
	}
}
