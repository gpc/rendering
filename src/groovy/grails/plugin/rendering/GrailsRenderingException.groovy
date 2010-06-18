package grails.plugin.rendering

class GrailsRenderingException extends RuntimeException {

	GrailsRenderingException(CharSequence message, Throwable cause = null) {
		super(message.toString(), cause)
	}

}