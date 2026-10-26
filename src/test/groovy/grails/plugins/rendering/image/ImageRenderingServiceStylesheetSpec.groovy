package grails.plugins.rendering.image

import com.sun.net.httpserver.HttpServer
import org.w3c.dom.Document
import spock.lang.Specification

import javax.xml.parsers.DocumentBuilderFactory
import java.awt.Color
import java.util.concurrent.atomic.AtomicInteger

class ImageRenderingServiceStylesheetSpec extends Specification {

    AtomicInteger cssRequests = new AtomicInteger()
    HttpServer server

    def setup() {
        server = HttpServer.create(new InetSocketAddress(InetAddress.loopbackAddress, 0), 0)
        server.createContext('/s.css') { exchange ->
            cssRequests.incrementAndGet()
            byte[] body = 'body { background-color: #ff0000; margin: 0; }'.getBytes('UTF-8')
            exchange.responseHeaders.add('Content-Type', 'text/css')
            exchange.sendResponseHeaders(200, body.length)
            exchange.responseBody.withCloseable { it.write(body) }
        }
        server.start()
    }

    def cleanup() {
        server.stop(0)
    }

    def "linked stylesheet is fetched once and applied"() {
        given:
        def document = parse('<link rel="stylesheet" type="text/css" href="s.css"/>')

        when:
        def image = new PngRenderingService().createBufferedImage([base: "http://127.0.0.1:${server.address.port}/", render: [width: 100]], document)
        def pixel = new Color(image.getRGB(5, 5))

        then:
        cssRequests.get() == 1
        pixel.red > 200
        pixel.green < 60
        pixel.blue < 60
    }

    def "renders without a base url"() {
        when:
        def image = new PngRenderingService().createBufferedImage([render: [width: 100]], parse(''))

        then:
        image.width == 100
    }

    private static Document parse(String head) {
        def xhtml = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>${head}</head><body><p>x</p></body></html>"
        def factory = DocumentBuilderFactory.newInstance()
        factory.namespaceAware = true
        factory.newDocumentBuilder().parse(new ByteArrayInputStream(xhtml.getBytes('UTF-8')))
    }
}
