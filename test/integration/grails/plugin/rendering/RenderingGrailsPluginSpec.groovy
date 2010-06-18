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
