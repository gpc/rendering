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
log4j = {
	debug  'grails.plugin.rendering'
}

grails {
	doc {
		title = "Grails Rendering Plugin"
		subtitle = "Render GSPs as PDFs, JPEGs, GIFs and PNGs"
		authors = "Grails Plugin Collective"
		copyright = "Copies of this document may be made for your own use and for distribution to others, provided that you do not charge any fee for such copies and further provided that each copy contains this Copyright Notice, whether distributed in print or electronically."
		footer = "Developed by the <a href='http://gpc.github.com'>Grails Plugin Collective</a>"
		
		alias {
			considerations = "2. GPS Considerations"
			pdfs = "3. Rendering PDFs"
			images = "4. Rendering Images"
			controllers = "5. Rendering From Controllers"
		}
	}
}