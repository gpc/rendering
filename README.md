[![Build Status](https://travis-ci.org/gpc/grails-rendering.svg?branch=master)](https://travis-ci.org/gpc/grails-rendering)

Rendering Grails Plugin
=======================

This plugin adds PDF, GIF, PNG and JPEG rendering facilities to Grails applications via the [XHTML Renderer](https://xhtmlrenderer.dev.java.net/) library.

Rendering is either done directly via one of the `«format»RenderingService` services …

    ByteArrayOutputStream bytes = pdfRenderingService.render(template: "/pdfs/report", model: [data: data])

Or via one of the `render«format»()` methods added to controllers …

    renderPdf(template: "/pdfs/report", model: [report: reportObject], filename: reportObject.name)

Please see the [User Guide](http://gpc.github.com/grails-rendering/ "Grails Rendering Plugin @ GitHub") for more information.

The plugin is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html "Apache License, Version 2.0 - The Apache Software Foundation") and is produced under the [Grails Plugin Collective](http://gpc.github.com/). 
However, it does [LGPL](http://www.gnu.org/licenses/lgpl.html) libraries: [XhtmlRenderer](https://code.google.com/p/flying-saucer/) and [iText](http://sourceforge.net/projects/itext/).
