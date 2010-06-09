package grails.plugin.pdf

class UnknownViewException extends GrailsPdfException {

	UnknownViewException(String view, String plugin = null) {
		super("Could not find view for '$view'${plugin ? " (plugin: $plugin)" : ''}")
	}

}