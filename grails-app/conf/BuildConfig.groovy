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
grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'docs/manual' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		compile("org.xhtmlrenderer:core-renderer:R8")
		compile("com.lowagie:itext:2.1.0")
		test("org.apache.pdfbox:pdfbox:1.0.0") {
			exclude 'jempbox'
			exported = false
		}
	}
	plugins {
		compile(":spring-events:1.0", ":tomcat:$grailsVersion", ":hibernate:$grailsVersion") {
			export = false
		}

		test(":spock:0.6") {
			exported = false
		}

		build ':release:2.2.1', ':rest-client-builder:1.0.3', {
			export = false
		}
	}
}

if (appName == "grails-rendering") {
	grails.plugin.location.'pdf-plugin-test' = "plugins/pdf-plugin-test"
}
