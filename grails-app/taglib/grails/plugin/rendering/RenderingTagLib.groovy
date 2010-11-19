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

package grails.plugin.rendering

import org.apache.commons.codec.binary.Base64

/**
 * @todo need tests for this.
 */
class RenderingTagLib {

	static namespace = "rendering"

	def inlineImage = {
		def mimeType = it.remove('mimeType')
		if (mimeType == null) {
			throwTagError("'mimeType' is required")
		}
		
		def bytes = it.remove('bytes')
		if (bytes == null) {
			throwTagError("'bytes' is required")
		}
		
		out << '<img src="data:'
		out << mimeType
		out << ';base64,'
		out << new String(new Base64().encode(bytes), "UTF-8")
		out << '" '
		it.each { k,v -> out << "$k=\"${v ? v.encodeAsHTML() : ''}\" " }
		out << ' />'
	}
	
	def inlineGif = {
		it.mimeType = "image/gif"
		out << inlineImage(it)
	}
	
	def inlinePng = {
		println "inlinePng: $it"
		it.mimeType = "image/png"
		out << inlineImage(it)
	}
	
	def inlineJpeg = {
		it.mimeType = "image/jpeg"
		out << inlineImage(it)
	}
	
}