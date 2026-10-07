package grails.plugins.rendering.document

import org.grails.web.servlet.WrappedResponseHolder
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockServletContext
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.context.support.GenericWebApplicationContext
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import spock.lang.Specification

class RenderEnvironmentFailureSpec extends Specification {

    static class ScriptedRequest extends MockHttpServletRequest {
        boolean armed
        Closure<Boolean> throwOnGet = { String name -> false }
        Closure<Boolean> throwOnSet = { String name, Object value -> false }
        int setThrows

        ScriptedRequest(MockServletContext ctx) { super(ctx) }

        @Override
        Object getAttribute(String name) {
            if (armed && throwOnGet(name)) {
                throw new IllegalStateException("boom-get $name")
            }
            super.getAttribute(name)
        }

        @Override
        void setAttribute(String name, Object value) {
            if (armed && throwOnSet(name, value)) {
                throw new IllegalStateException("boom-set #${++setThrows}")
            }
            super.setAttribute(name, value)
        }

        Object raw(String name) { super.getAttribute(name) }
    }

    MockServletContext servletContext = new MockServletContext()
    GenericWebApplicationContext ctx = new GenericWebApplicationContext(servletContext)
    StringWriter callerOut = new StringWriter()
    AcceptHeaderLocaleResolver callerResolver = new AcceptHeaderLocaleResolver()
    MockHttpServletResponse callerWrapped = new MockHttpServletResponse()
    ScriptedRequest callerRequest

    def setup() {
        ctx.refresh()
        callerRequest = new ScriptedRequest(servletContext)
        def callerWebRequest = new GrailsWebRequest(callerRequest, new MockHttpServletResponse(), servletContext, ctx)
        callerRequest.setAttribute(GrailsApplicationAttributes.OUT, callerOut)
        callerRequest.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, callerResolver)
        RequestContextHolder.setRequestAttributes(callerWebRequest)
        WrappedResponseHolder.wrappedResponse = callerWrapped
    }

    def cleanup() {
        RequestContextHolder.resetRequestAttributes()
        WrappedResponseHolder.wrappedResponse = null
    }

    private static boolean thrownFrom(Throwable throwable, String method) {
        throwable.stackTrace.find {
            it.className == RenderEnvironment.class.name
        }?.methodName == method
    }

    def "surface the init failure and leave nothing bound when there is no caller request"() {
        given:
        RequestContextHolder.resetRequestAttributes()
        WrappedResponseHolder.wrappedResponse = null

        when:
        RenderEnvironment.with(ctx, null) {}

        then:
        def ex = thrown(NullPointerException)
        thrownFrom(ex, 'init')
        RequestContextHolder.requestAttributes == null
        WrappedResponseHolder.wrappedResponse == null
    }

    def "surface the init failure and keep the caller's request state"() {
        when:
        RenderEnvironment.with(ctx, null) {}

        then:
        def ex = thrown(NullPointerException)
        thrownFrom(ex, 'init')
        callerRequest.raw(GrailsApplicationAttributes.OUT).is(callerOut)
        callerRequest.raw(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE).is(callerResolver)
        WrappedResponseHolder.wrappedResponse.is(callerWrapped)
    }

    def "keep a plain caller binding when init fails"() {
        given:
        def plain = new ServletRequestAttributes(new MockHttpServletRequest())
        RequestContextHolder.setRequestAttributes(plain)

        when:
        RenderEnvironment.with(ctx, null) {}

        then:
        def ex = thrown(NullPointerException)
        thrownFrom(ex, 'init')
        RequestContextHolder.requestAttributes.is(plain)
    }

    def "keep caller's state when reading its #attribute fails"() {
        given:
        callerRequest.throwOnGet = { String name -> name == attribute }
        callerRequest.armed = true

        when:
        RenderEnvironment.with(ctx, new StringWriter(), Locale.FRENCH) {}

        then:
        def ex = thrown(IllegalStateException)
        ex.message == "boom-get $attribute"
        callerRequest.raw(GrailsApplicationAttributes.OUT).is(callerOut)
        callerRequest.raw(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE).is(callerResolver)
        WrappedResponseHolder.wrappedResponse.is(callerWrapped)

        where:
        attribute << [GrailsApplicationAttributes.OUT, DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE]
    }

    def "restore the caller's state when setting the render writer fails"() {
        given:
        callerRequest.throwOnSet = { String name, Object value -> name == GrailsApplicationAttributes.OUT && callerRequest.setThrows == 0 }
        callerRequest.armed = true

        when:
        RenderEnvironment.with(ctx, new StringWriter(), Locale.FRENCH) {}

        then:
        def ex = thrown(IllegalStateException)
        ex.message == 'boom-set #1'
        callerRequest.raw(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE).is(callerResolver)
        callerRequest.raw(GrailsApplicationAttributes.OUT).is(callerOut)
        WrappedResponseHolder.wrappedResponse.is(callerWrapped)
    }

    def "keep the outer render's writer when a nested init fails"() {
        given:
        def outerOut = new StringWriter()
        Object outInsideOuterAfterNestedFailure = null

        when:
        RenderEnvironment.with(ctx, outerOut, Locale.FRENCH) {
            try {
                RenderEnvironment.with(ctx, null) {}
            } catch (NullPointerException ignored) {
            }
            outInsideOuterAfterNestedFailure = callerRequest.raw(GrailsApplicationAttributes.OUT)
        }

        then:
        outInsideOuterAfterNestedFailure.is(outerOut)
        callerRequest.raw(GrailsApplicationAttributes.OUT).is(callerOut)
    }
}
