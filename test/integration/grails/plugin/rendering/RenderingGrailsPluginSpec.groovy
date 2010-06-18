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
package grails.plugin.rendering

import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import grails.plugin.spock.*
import spock.lang.*

class RenderingGrailsPluginSpec extends IntegrationSpec {

	def grailsApplication
	
	@Unroll("rendering #action works from controllers and survives a reload")
	def supportReloadingControllerClasses() {
		when:
		def controller = createController()
		controller."$action"()
		then:
		notThrown(MissingMethodException)
		when:
		PluginManagerHolder.pluginManager.informOfClassChange(reloadControllerClass())
		controller = createController()
		and:
		controller."$action"()
		then:
		notThrown(MissingMethodException)
		where:
		action << ['pdf', 'gif', 'png', 'jpeg']
	}
	
	protected createController() {
		grailsApplication.mainContext['RenderingController']
	}
	
	protected reloadControllerClass() {
		grailsApplication.classLoader.reloadClass('RenderingController')
	}

}
