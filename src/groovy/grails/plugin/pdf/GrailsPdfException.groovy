package grails.plugin.pdf

class GrailsPdfException extends Exception {

	GrailsPdfException(CharSequence message, Throwable cause = null) {
		super(message.toString(), cause)
	}

}