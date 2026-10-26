/*
 * Copyright 2010-2013 Grails Plugin Collective
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


import grails.plugins.Plugin
import groovy.transform.CompileStatic

@CompileStatic
class RenderingGrailsPlugin extends Plugin {

    def grailsVersion = "8.0 > *"

    def pluginExcludes = [
            "grails-app/views/**",
            "example/**"
    ]

    def observe = ["controllers"]
    def loadAfter = ["controllers"]

    def author = "Grails Plugin Collective"
    def authorEmail = "grails.plugin.collective@gmail.com"
    def title = "Grails Rendering"
    def description = 'Render GSPs as PDFs, JPEGs, GIFs and PNGs'
    def documentation = "https://gpc.github.io/rendering/"

    def license = 'APACHE'
    def organization = [name: 'Grails Plugin Collective', url: 'https://github.com/gpc']
    def issueManagement = [system: 'GitHub', url: 'https://github.com/gpc/rendering/issues']
    def scm = [url: 'https://github.com/gpc/rendering.git']

}
