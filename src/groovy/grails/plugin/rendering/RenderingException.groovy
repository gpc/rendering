package grails.plugin.rendering

class RenderingException extends GrailsPdfException {

	RenderingException(xml, cause) {
		super("Failed to render: $xml", cause)
	}

}