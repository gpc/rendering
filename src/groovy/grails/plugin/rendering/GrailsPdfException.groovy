package grails.plugin.rendering

class GrailsPdfException extends Exception {

	GrailsPdfException(CharSequence message, Throwable cause = null) {
		super(message.toString(), cause)
	}

}