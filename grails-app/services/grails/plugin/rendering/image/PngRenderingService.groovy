package grails.plugin.rendering.image

class PngRenderingService extends ImageRenderingService {

	protected getImageType() {
		"png"
	}

	protected getDefaultContentType() {
		"image/png"
	}
	
}