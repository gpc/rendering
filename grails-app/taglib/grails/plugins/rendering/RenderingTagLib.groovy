/*
 * Copyright 2010 Grails Plugin Collective
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.rendering

import groovy.transform.CompileStatic
import org.grails.encoder.CodecLookup

@CompileStatic
class RenderingTagLib {

	static namespace = "rendering"

	CodecLookup codecLookup

	def inlineImage(Map attrs) {
		def mimeType = attrs.remove('mimeType')
		if (mimeType == null) {
			throwTagError("'mimeType' is required")
		}

		writeImage(mimeType as String, attrs)
	}

	def inlineGif(Map attrs) {
		attrs.remove('mimeType')
		writeImage('image/gif', attrs)
	}

	def inlinePng(Map attrs) {
		attrs.remove('mimeType')
		writeImage('image/png', attrs)
	}

	def inlineJpeg(Map attrs) {
		attrs.remove('mimeType')
		writeImage('image/jpeg', attrs)
	}

	private void writeImage(String mimeType, Map attrs) {
		def bytes = attrs.remove('bytes')
		if (bytes == null) {
			throwTagError("'bytes' is required")
		}

		out << '<img src="data:'
		out << mimeType
		out << ';base64,'
		out << new String(Base64.encoder.encode(bytes as byte[]), 'UTF-8')
		out << '" '
		attrs.each { k,v -> out << "$k=\"${v ? codecLookup.lookupEncoder('HTML').encode(v) : ''}\" " }
		out << ' />'
	}
}
