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

package grails.plugins.rendering.datauri

import org.apache.commons.codec.binary.Base64
import spock.lang.*

class DataUriSpec extends Specification {

	def "encoding base64 round trip"() {
		given:
		def bytes = getRedDotBytes()
		def base64 = new String(new Base64().encode(bytes), "UTF-8")

		when:
		def dataUri = new DataUri("data:base64,$base64")

		then:
		dataUri.bytes == bytes
	}

	def "encoding ascii octets round trip"() {
		given:
		def bytes = getRedDotBytes()
		def octets = URLEncoder.encode(new String(bytes, "ISO-8859-1"), "ISO-8859-1")

		when:
		def dataUri = new DataUri("data:,$octets")

		then:
		dataUri.bytes == bytes
	}

	protected getRedDotBytes() {
		getClass().getResource("red-dot-5px.png").openStream().bytes
	}
}
