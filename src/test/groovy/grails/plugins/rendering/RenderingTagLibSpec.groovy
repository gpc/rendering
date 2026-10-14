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

import grails.plugins.rendering.datauri.DataUri
import grails.testing.web.taglib.TagLibUnitTest
import org.grails.taglib.GrailsTagException
import spock.lang.Shared
import spock.lang.Specification

class RenderingTagLibSpec extends Specification implements TagLibUnitTest<RenderingTagLib> {

	@Shared bytes = [1,2,3] as byte[]
	
	@Shared String template

	protected getOutputMimeType() {
		getDataUri().mimeType
	}

	protected getOutputBytes() {
		getDataUri().bytes
	}

	protected getDataUri() {
		new DataUri(getSrcAttribute())
	}

	protected getSrcAttribute() {
		def m = output =~ /^.+src="(.+?)".+$/
		assert m
		m[0][1]
	}

	protected getOutput() {
		applyTemplate( template, [bytes:bytes, mimeType:params.mimeType] )
	}

	def "inline image tag"() {
		given:
		params.mimeType = 'abc/123'
        template = '<rendering:inlineImage mimeType="${mimeType}" bytes="${bytes}" />'

		expect:
		outputMimeType == "abc/123"
		outputBytes == bytes
		getDataUri().base64 == true
	}

	def "png tag"() {
		given:
		template = '<rendering:inlinePng bytes="${bytes}" />'

		expect:
		outputMimeType == "image/png"
		outputBytes == bytes
	}

	def "gif tag"() {
		given:
		template = '<rendering:inlineGif bytes="${bytes}" />'

		expect:
		outputMimeType == "image/gif"
		outputBytes == bytes
	}

	def "jpeg tag"() {
		given:
		template = '<rendering:inlineJpeg bytes="${bytes}" />'

		expect:
		outputMimeType == "image/jpeg"
		outputBytes == bytes
	}

	def "#tag renders exactly one img"() {
		given:
		template = "<rendering:${tag} bytes=\"\${bytes}\" />"

		expect:
		output.findAll('<img').size() == 1

		where:
		tag << ['inlineGif', 'inlinePng', 'inlineJpeg']
	}

	def "extra attributes are copied onto the img"() {
		given:
		template = '<rendering:inlineImage mimeType="image/png" bytes="${bytes}" alt="logo" class="thumb" />'

		expect:
		output.contains('alt="logo"')
		output.contains('class="thumb"')
		!output.contains('mimeType')
	}

	def "inlineImage without mimeType fails"() {
		when:
		applyTemplate('<rendering:inlineImage bytes="${bytes}" />', [bytes: bytes])

		then:
		def e = thrown(GrailsTagException)
		e.message.contains("'mimeType' is required")
	}

	def "#template fails without bytes"() {
		when:
		applyTemplate(template)

		then:
		def e = thrown(GrailsTagException)
		e.message.contains("'bytes' is required")

		where:
		template << [
				'<rendering:inlineImage mimeType="image/png" />',
				'<rendering:inlinePng />',
				'<rendering:inlineGif />',
				'<rendering:inlineJpeg />'
		]
	}

	def "attribute values are html escaped"() {
		when:
		def output = applyTemplate('<rendering:inlinePng bytes="${bytes}" alt="${alt}" />',
				[bytes: bytes, alt: '"><script>x</script>'])

		then:
		output.contains('alt="&quot;&gt;&lt;script&gt;x&lt;/script&gt;"')
		!output.contains('<script>')
	}
}
