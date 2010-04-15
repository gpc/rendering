grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.dependency.resolution = {
	inherits( "global" )
	log "warn"
	repositories {
		grailsPlugins()
		grailsHome()
		mavenCentral()
		mavenRepo "http://download.java.net/maven/2/"
	}
	dependencies {
		compile("org.xhtmlrenderer:core-renderer:R8")
		test("org.apache.pdfbox:pdfbox:1.0.0") {
			exclude 'jempbox'
			exported = false
		}
	}
}