package grails.plugin.rendering

class RenderingException extends GrailsRenderingException {

	RenderingException(cause) {
		super("Render failure", cause)
	}

}