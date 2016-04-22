/*
 * Copyright 2015 Grails Plugin Collective
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

import grails.artefact.Enhances
import grails.plugins.rendering.image.GifRenderingService
import grails.plugins.rendering.image.JpegRenderingService
import grails.plugins.rendering.image.PngRenderingService
import grails.plugins.rendering.pdf.PdfRenderingService
import grails.web.api.ServletAttributes
import org.springframework.beans.factory.annotation.Autowired

/**
 * Trait that applies to controllers adding new methods for rendering PDFs, Gif etc.
 */
@Enhances("Controller")
trait RenderingTrait extends ServletAttributes{

	@Autowired(required = false)
	PdfRenderingService pdfRenderingService

	@Autowired(required = false)
	GifRenderingService gifRenderingService

	@Autowired(required = false)
	JpegRenderingService jpegRenderingService

	@Autowired(required = false)
	PngRenderingService pngRenderingService

	boolean renderPdf(Map args)	{
		render pdfRenderingService, args
	}

	boolean renderGif(Map args)	{
		render gifRenderingService, args
	}	

	boolean renderJpeg(Map args)	{
		render jpegRenderingService, args
	}	

	boolean renderPng(Map args)	{
		render pngRenderingService, args
	}	

	private boolean render(RenderingService renderService, Map args) {
		if(!renderService) throw new IllegalStateException("Bean with rendering service was not injected!")

		def adjustedArgs = [controller: this]
		adjustedArgs.putAll args

		renderService.render(adjustedArgs, response)
	}
}