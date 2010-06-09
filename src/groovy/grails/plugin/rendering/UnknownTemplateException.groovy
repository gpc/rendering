package grails.plugin.rendering

class UnknownTemplateException extends GrailsPdfException {

	UnknownTemplateException(String template, String plugin = null) {
		super("Could not find template for '$template'${plugin ? " (plugin: $plugin)" : ''}")
	}

}