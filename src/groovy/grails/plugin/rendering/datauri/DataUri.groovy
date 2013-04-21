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

import org.apache.commons.codec.binary.Base64

class DataUri {

	// Default values sourced from http://en.wikipedia.org/wiki/Data_URI_scheme
	private String mimeType = "text/plain"
	private String charset = "US-ASCII"
	private boolean base64 = false

	private String data

	DataUri(String uri) {
		if (!isDataUri(uri)) {
			throw new IllegalArgumentException("uri does not start with 'data:' - $uri")
		}

		determineParts(uri.substring(5))
	}

	protected void determineParts(String value) {
		if (!value.contains(",")) {
			throw new IllegalArgumentException("data url does not contain a ',' delimiter: " + value)
		}

		def (metadata, data) = value.split(",", 2)
		if (metadata != "") {
			processMetadata(metadata.split(';'))
		}

		this.data = data
	}

	protected void processMetadata(String[] metadataPieces) {
		for (String metadataPiece in metadataPieces) {
			if (metadataPiece.contains("/")) {
				mimeType = metadataPiece
			} else if (metadataPiece.startsWith("charset=")) {
				charset = metadataPiece.substring(8)
			} else if (metadataPiece == "base64") {
				base64 = true
			} else {
				throw new IllegalArgumentException("can't understand metadata: " + metadataPiece)
			}
		}
	}

	InputStream getInputStream() {
		new ByteArrayInputStream(getBytes())
	}

	byte[] getBytes() {
		if (base64) {
			new Base64().decode(data.getBytes("ASCII"))
		} else {
			URLDecoder.decode(data, "ISO-8859-1").getBytes("ISO-8859-1")
		}
	}

	static boolean isDataUri(String uri) {
		uri?.startsWith("data:")
	}
}
