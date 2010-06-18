package grails.plugin.rendering.document

import grails.plugin.rendering.GrailsRenderingException

class UnknownTemplateException extends GrailsRenderingException {

	UnknownTemplateException(String template, String plugin = null) {
		super("Could not find template for '$template'${plugin ? " (plugin: $plugin)" : ''}")
	}

}