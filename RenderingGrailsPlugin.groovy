/*
 * Copyright 2010 Grails Plugin Collective
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class RenderingGrailsPlugin {

	def version = "0.1"
	def grailsVersion = "1.2.0 > *"
	def dependsOn = [:]
	
	def pluginExcludes = [
		"grails-app/views/**/*",
		"grails-app/controllers/**/*",
		"plugins/**/*",
		"web-app/**/*"
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
	def description = 'Render GSPs as PDFs, JPEGs, GIFs and PNGs'
	def documentation = "http://gpc.github.com/grails-rendering"

	def renderMethodTemplate = { ctx, rendererName, Map args ->
		def adjustedArgs = [controller: delegate]
		adjustedArgs.putAll(args)
		ctx[rendererName].render(adjustedArgs, delegate.response)
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
