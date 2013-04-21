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

package grails.plugin.rendering.datauri

import grails.util.GrailsUtil

import org.slf4j.LoggerFactory
import org.xhtmlrenderer.pdf.ITextFSImage
import org.xhtmlrenderer.pdf.ITextOutputDevice
import org.xhtmlrenderer.pdf.ITextUserAgent
import org.xhtmlrenderer.resource.ImageResource

import com.lowagie.text.Image

class DataUriAwareITextUserAgent extends ITextUserAgent {

	private static log = LoggerFactory.getLogger(DataUriAwareITextUserAgent)

	DataUriAwareITextUserAgent(ITextOutputDevice outputDevice) {
		super(outputDevice)
	}

	ImageResource getImageResource(String uri) {
		def resource = _imageCache.get(uri)
		if (resource) {
			return resource
		}

		if (DataUri.isDataUri(uri)) {
			def dataUri = new DataUri(uri)
			if (dataUri.mimeType.startsWith("image/")) {
				try {
					def image = Image.getInstance(dataUri.bytes)
					def factor = sharedContext.dotsPerPixel
					image.scaleAbsolute((image.plainWidth * factor) as float, (image.plainHeight * factor) as float)
					resource = new ImageResource(new ITextFSImage(image))
					_imageCache.put(uri, resource)
					resource
				} catch (Exception e) {
					GrailsUtil.deepSanitize(e)
					log.error("exception creating image from data uri (will use empty image): $dataUri", e)
					new ImageResource(null)
				}
			} else {
				log.error("data uri has a non image mime type (will use empty image): $dataUri")
				new ImageResource(null)
			}
		} else {
			super.getImageResource(uri)
		}
	}
}
