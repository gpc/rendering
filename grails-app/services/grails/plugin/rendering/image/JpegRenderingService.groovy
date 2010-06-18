package grails.plugin.rendering.image

class JpegRenderingService extends ImageRenderingService {

	protected getImageType() {
		"jpeg"
	}

	protected getDefaultContentType() {
		"image/jpeg"
	}
	
}