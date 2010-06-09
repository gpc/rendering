package grails.plugin.rendering

class XmlParseException extends GrailsPdfException {

	XmlParseException(xml, cause) {
		super("Could not parse: $xml", cause)
	}

}