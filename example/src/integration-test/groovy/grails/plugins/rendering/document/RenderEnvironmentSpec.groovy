package grails.plugins.rendering.document

import grails.core.GrailsApplication
import grails.testing.mixin.integration.Integration
import grails.util.Environment
import grails.util.GrailsWebMockUtil
import org.grails.web.servlet.WrappedResponseHolder
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.support.RequestContextUtils
import spock.lang.Specification

@Integration
class RenderEnvironmentSpec extends Specification {

    GrailsApplication grailsApplication

    private String previousEnv

    def setup() {
        previousEnv = System.getProperty(Environment.KEY)
        System.setProperty(Environment.KEY, Environment.PRODUCTION.name)
        RequestContextHolder.resetRequestAttributes()
        WrappedResponseHolder.wrappedResponse = null
    }

    def cleanup() {
        if (previousEnv == null) {
            System.clearProperty(Environment.KEY)
        } else {
            System.setProperty(Environment.KEY, previousEnv)
        }
        RequestContextHolder.resetRequestAttributes()
        WrappedResponseHolder.wrappedResponse = null
    }

    def "bind a web request when one does not exist"() {
        given:
        def out = new StringWriter()
        GrailsWebRequest bound = null

        when:
        RenderEnvironment.with(grailsApplication.mainContext, out) {
            bound = GrailsWebRequest.lookup()
        }

        then:
        Environment.current == Environment.PRODUCTION
        bound != null
        bound.out.is(out)
    }

    def "leave no request bound afterwards"() {
        when:
        RenderEnvironment.with(grailsApplication.mainContext, new StringWriter()) {}

        then:
        RequestContextHolder.requestAttributes == null
        WrappedResponseHolder.wrappedResponse == null
    }

    def "restore the callers request"() {
        given:
        GrailsWebRequest original = GrailsWebMockUtil.bindMockWebRequest(grailsApplication.mainContext)
        original.controllerName = 'book'
        def layoutBufferingResponse = new MockHttpServletResponse()
        WrappedResponseHolder.wrappedResponse = layoutBufferingResponse
        GrailsWebRequest bound = null
        String controllerName = null

        when:
        RenderEnvironment.with(grailsApplication.mainContext, new StringWriter()) { RenderEnvironment env ->
            bound = GrailsWebRequest.lookup()
            controllerName = env.controllerName
        }

        then:
        !bound.is(original)
        controllerName == 'book'
        RequestContextHolder.requestAttributes.is(original)
        WrappedResponseHolder.wrappedResponse.is(layoutBufferingResponse)
    }

    def "restore state when the block throws"() {
        given:
        GrailsWebRequest original = GrailsWebMockUtil.bindMockWebRequest(grailsApplication.mainContext)

        when:
        RenderEnvironment.with(grailsApplication.mainContext, new StringWriter()) {
            throw new IllegalStateException('boom')
        }

        then:
        thrown(IllegalStateException)
        RequestContextHolder.requestAttributes.is(original)
    }

    def "render locale is #expected when explicit=#explicit and request=#requestLocale"() {
        given:
        if (requestLocale) {
            GrailsWebRequest original = GrailsWebMockUtil.bindMockWebRequest(grailsApplication.mainContext)
            (original.currentRequest as MockHttpServletRequest).addPreferredLocale(requestLocale)
        }
        Locale seen = null

        when:
        RenderEnvironment.with(grailsApplication.mainContext, new StringWriter(), explicit) {
            seen = RequestContextUtils.getLocale(GrailsWebRequest.lookup().currentRequest)
        }

        then:
        seen == expected

        where:
        explicit      | requestLocale | expected
        Locale.FRENCH | Locale.GERMAN | Locale.FRENCH
        null          | Locale.GERMAN | Locale.GERMAN
        null          | null          | Locale.default
    }
}
