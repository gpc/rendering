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

import spock.lang.*
import grails.plugins.rest.client.RestBuilder
class ControllerRelativeTemplateSpec extends Specification {

	def accessingControllerRelativeTemplateWorks() {
		when:
			def rest = new RestBuilder()
			def resp = rest.get("http://localhost:8080/grails-rendering/rendering/relative")
		then:
			resp.status == 200
	}
}
