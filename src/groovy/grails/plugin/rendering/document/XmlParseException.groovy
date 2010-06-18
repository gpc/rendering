package grails.plugin.rendering.document

import grails.plugin.rendering.GrailsRenderingException

class XmlParseException extends GrailsRenderingException {

	XmlParseException(xml, cause) {
		super("Could not parse: $xml", cause)
	}

}