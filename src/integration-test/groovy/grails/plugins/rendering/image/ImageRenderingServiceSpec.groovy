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
package grails.plugins.rendering.image

import grails.plugins.rendering.RenderingServiceSpec

import javax.imageio.ImageIO

abstract class ImageRenderingServiceSpec extends RenderingServiceSpec {

	protected toBufferedImage(baos) {
		ImageIO.read(new ByteArrayInputStream(baos.toByteArray()))
	}

	protected image(Map args) {
		toBufferedImage(renderer.render(args))
	}

	def renderImageWithHeight() {
		when:
		def image = image(getSimpleTemplate(render: [width: 200, height: 100], autosize: [width: false, height: false]))
		then:
		image.height == 100
		image.width == 200
	}

	def renderImageWithAutoHeight() {
		when:
		def image = image(getSimpleTemplate(render: [width: 200, height: 400]))
		then:
		image.width == 200
		image.height == 200
	}

	def imageResize() {
		when:
		def image = image(getSimpleTemplate(render: [width: 200], resize: resize))
		then:
		image.width == width
		image.height == height
		where:
		resize | width | height
		[width: 400] | 400 | 400
		[height: 400] | 400 | 400
		[width: 400, height: 200] | 400 | 200
	}

	def imageScale() {
		when:
		def image = image(getSimpleTemplate(render: [width: 200], scale: scale))
		then:
		image.width == width
		image.height == height
		where:
		scale | width | height
		[width: 2] | 400 | 400
		[height: 2] | 400 | 400
		[width: 4, height: 2] | 800 | 400
	}

/*	// Excercises the normal non http response code as well
	@Unroll("render #type to http response")
	def renderImagesToHttpResponse() {
		given:
		def response = new MockHttpServletResponse()
		def filename = "test.$type"
		when:
		pdfRenderingService."$type"(getSimpleTemplate(filename: filename, width: 200), response)
		then:
		response.contentAsByteArray.size() > 0
		response.contentType == "image/$type"
		response.getHeader("Content-Disposition") == "attachment; filename=\"$filename\";"
		where:
		type << ["jpeg", "gif", "png"]
	}
*/
}
