[![Build Status](https://travis-ci.org/gpc/rendering.svg?branch=master)](https://travis-ci.org/gpc/rendering)

Rendering Grails Plugin
=======================

This plugin adds PDF, GIF, PNG and JPEG rendering facilities to Grails applications via the [XHTML Renderer](https://github.com/flyingsaucerproject/flyingsaucer) library.

Install
-------

To install just add to the dependency block in the `build.gradle`

For Grails 7.x
```groovy
implementation 'io.github.gpc:rendering:7.0.1'
```

For Grails 3.x - 6.x
```groovy
implementation 'org.grails.plugins:rendering:2.0.3'
```

Usage
-----

Rendering is either done directly via one of the `«format»RenderingService` services …

    ByteArrayOutputStream bytes = pdfRenderingService.render(template: "/pdfs/report", model: [data: data])

Or via one of the `render«format»()` methods added to controllers …

    renderPdf(template: "/pdfs/report", model: [report: reportObject], filename: reportObject.name)

Please see the [User Guide](http://gpc.github.io/rendering/ "Grails Rendering Plugin @ GitHub") for more information.

The plugin is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html "Apache License, Version 2.0 - The Apache Software Foundation") and is produced under the [Grails Plugin Collective](https://github.com/gpc).
However, it does include [LGPL](http://www.gnu.org/licenses/lgpl.html) libraries: [XhtmlRenderer](https://github.com/flyingsaucerproject/flyingsaucer)
