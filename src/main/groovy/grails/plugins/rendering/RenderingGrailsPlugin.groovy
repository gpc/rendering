/*
 * Copyright 2010-2013 Grails Plugin Collective
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
package grails.plugins.rendering

import grails.plugins.*

class RenderingGrailsPlugin extends Plugin {

	def grailsVersion = "3.0 > *"

	def pluginExcludes = [
		"grails-app/views/**",
		"RenderingController**",
		"grails-app/services/grails/plugin/rendering/test/**",
		"src/groovy/grails/plugin/rendering/test/**",
		"plugins/**",
		"web-app/**"
	]

	def observe = ["controllers"]
	def loadAfter = ["controllers"]

	def author = "Grails Plugin Collective"
	def authorEmail = "grails.plugin.collective@gmail.com"
	def title = "Grails Rendering"
	def description = 'Render GSPs as PDFs, JPEGs, GIFs and PNGs'
	def documentation = "http://gpc.github.com/grails-rendering"

	def license = 'APACHE'
	def organization = [name: 'Grails Plugin Collective', url: 'http://github.com/gpc']
	def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPRENDERING']
	def scm = [url: 'https://github.com/gpc/grails-rendering']

}
