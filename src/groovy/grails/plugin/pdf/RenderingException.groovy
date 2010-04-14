package grails.plugin.pdf

class RenderingException extends GrailsPdfException {

	RenderingException(xml, cause) {
		super("Failed to render: $xml", cause)
	}

}