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
if(System.getenv('TRAVIS_BRANCH')) {
    grails.project.repos.grailsCentral.username = System.getenv("GRAILS_CENTRAL_USERNAME")
    grails.project.repos.grailsCentral.password = System.getenv("GRAILS_CENTRAL_PASSWORD")    
}


grails.project.work.dir = 'target'

grails.project.dependency.resolver="maven"
grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	def seleniumVersion = "2.32.0"

	dependencies {
		compile("org.xhtmlrenderer:core-renderer:R8")
		compile("com.lowagie:itext:2.1.0")
		test("org.apache.pdfbox:pdfbox:1.0.0") {
			exclude 'jempbox'
			exported = false
		}
		
	}
	plugins {
		compile(":spring-events:1.0", ":tomcat:7.0.52.1", ":hibernate:3.6.10.14") {
			export = false
		}
		
		build ':release:3.0.1', ':rest-client-builder:2.0.1', {
			export = false
		}

		test ":funky-spock:0.2.2"
	}
}

if (appName == "grails-rendering") {
	grails.plugin.location.'pdf-plugin-test' = "plugins/pdf-plugin-test"
}
