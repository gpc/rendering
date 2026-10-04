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
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.i18n.FixedLocaleResolver
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

    def "reuse the callers request and restore it afterwards"() {
        given:
        GrailsWebRequest original = GrailsWebMockUtil.bindMockWebRequest(grailsApplication.mainContext)
        original.controllerName = 'book'
        def callerOut = new StringWriter()
        original.out = callerOut
        def callerLocaleResolver = new FixedLocaleResolver(Locale.ITALIAN)
        original.currentRequest.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, callerLocaleResolver)
        def layoutBufferingResponse = new MockHttpServletResponse()
        WrappedResponseHolder.wrappedResponse = layoutBufferingResponse
        def renderOut = new StringWriter()
        GrailsWebRequest bound = null
        Writer boundOut = null
        String controllerName = null

        when:
        RenderEnvironment.with(grailsApplication.mainContext, renderOut) { RenderEnvironment env ->
            bound = GrailsWebRequest.lookup()
            boundOut = bound.out
            controllerName = env.controllerName
        }

        then:
        bound.is(original)
        boundOut.is(renderOut)
        controllerName == 'book'
        RequestContextHolder.requestAttributes.is(original)
        original.out.is(callerOut)
        original.currentRequest.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE).is(callerLocaleResolver)
        WrappedResponseHolder.wrappedResponse.is(layoutBufferingResponse)
    }

    def "restore state when the block throws"() {
        given:
        def callerOut = new StringWriter()
        GrailsWebRequest original = GrailsWebMockUtil.bindMockWebRequest(grailsApplication.mainContext)
        original.out = callerOut

        when:
        RenderEnvironment.with(grailsApplication.mainContext, new StringWriter()) {
            throw new IllegalStateException('boom')
        }

        then:
        thrown(IllegalStateException)
        RequestContextHolder.requestAttributes.is(original)
        original.out.is(callerOut)
    }

    def "restore the outer renders writer after a nested render"() {
        given:
        def outerOut = new StringWriter()
        Writer afterInner = null

        when:
        RenderEnvironment.with(grailsApplication.mainContext, outerOut) {
            RenderEnvironment.with(grailsApplication.mainContext, new StringWriter()) {}
            afterInner = GrailsWebRequest.lookup().out
        }

        then:
        afterInner.is(outerOut)
        RequestContextHolder.requestAttributes == null
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
