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
package grails.plugins.rendering.test


import org.springframework.context.*

class BackgroundRenderingService implements ApplicationListener<RenderEvent>, ApplicationContextAware {

	static transactional = false

	ApplicationContext applicationContext
	def pdfRenderingService

	void onApplicationEvent(RenderEvent event) {
		try {
			pdfRenderingService.render(template: '/simple', model: [var: 1])
			event.source.set(null)
		} catch (Throwable e) {
			event.source.set(e)
		}
	}

	def fireEvent(errorHolder) {
		applicationContext.publishEvent(new RenderEvent(errorHolder))
	}
}

class RenderEvent extends ApplicationEvent {
	RenderEvent(renderError) {
		super(renderError)
	}
}
