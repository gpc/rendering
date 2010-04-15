This plugin adds PDF rendering facilities to Grails applications via the [Flying Saucer XHTML Renderer](https://xhtmlrenderer.dev.java.net/) library.

Rendering is either done directly via the `pdfRenderingService` …

    ByteArrayOutputStream bytes = pdfRenderingService.render(template: "/pdfs/report", model: [data: data])

Or via the `renderPdf()` method added to controllers …

    renderPdf(template: "/pdfs/report", model: [report: reportObject], filename: reportObject.name)

Please see the [User Guide](http://alkemist.github.com/grails-pdf/ "Grails PDF Plugin @ GitHub") for more information.

The plugin is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html "Apache License, Version 2.0 - The Apache Software Foundation") and was contributed to the Grails community by the good people at [Software Projects](http://sp.com.au/ "Software Projects - Home").