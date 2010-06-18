package grails.plugin.rendering.image

class GifRenderingService extends ImageRenderingService {

	protected getImageType() {
		"gif"
	}

	protected getDefaultContentType() {
		"image/gif"
	}
	
}