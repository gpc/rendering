package grails.plugins.rendering.document

import grails.gsp.PageRenderer
import groovy.transform.CompileStatic
import jakarta.servlet.ServletContext
import jakarta.servlet.http.HttpServletResponse
import org.grails.web.servlet.WrappedResponseHolder
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.context.ApplicationContext
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.i18n.FixedLocaleResolver
import org.springframework.web.servlet.support.RequestContextUtils

@CompileStatic
class RenderEnvironment {

    final Writer out
    final Locale locale
    final ApplicationContext applicationContext

    private RequestAttributes originalRequestAttributes
    private GrailsWebRequest renderRequestAttributes
    private HttpServletResponse originalWrappedResponse

    RenderEnvironment(ApplicationContext applicationContext, Writer out, Locale locale = null) {
        this.out = out
        this.locale = locale
        this.applicationContext = applicationContext
    }

    private void init() {
        originalRequestAttributes = RequestContextHolder.getRequestAttributes()
        originalWrappedResponse = WrappedResponseHolder.wrappedResponse
        GrailsWebRequest originalWebRequest = originalRequestAttributes instanceof GrailsWebRequest ?
                (GrailsWebRequest) originalRequestAttributes : null

        Locale renderLocale = locale ?: (originalWebRequest ? RequestContextUtils.getLocale(originalWebRequest.currentRequest) : Locale.default)

        def request = PageRenderer.PageRenderRequestCreator.createInstance('/', renderLocale)
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new FixedLocaleResolver(renderLocale))
        def response = PageRenderer.PageRenderResponseCreator.createInstance(
                out instanceof PrintWriter ? (PrintWriter) out : new PrintWriter(out), renderLocale)

        renderRequestAttributes = new GrailsWebRequest(request, response, servletContext, applicationContext)
        renderRequestAttributes.controllerName = originalWebRequest?.controllerName
        renderRequestAttributes.out = out

        RequestContextHolder.setRequestAttributes(renderRequestAttributes)
        WrappedResponseHolder.wrappedResponse = response
    }

    private void close() {
        RequestContextHolder.setRequestAttributes(originalRequestAttributes)
        WrappedResponseHolder.wrappedResponse = originalWrappedResponse
    }

    private ServletContext getServletContext() {
        applicationContext instanceof WebApplicationContext ?
                ((WebApplicationContext) applicationContext).servletContext : null
    }

    /**
     * Establish an environment inheriting the locale of the current request if there is one
     */
    static with(ApplicationContext applicationContext, Writer out, Closure block) {
        with(applicationContext, out, null, block)
    }

    /**
     * Establish an environment with a specific locale
     */
    static with(ApplicationContext applicationContext, Writer out, Locale locale, Closure block) {
        def env = new RenderEnvironment(applicationContext, out, locale)
        env.init()
        try {
            block(env)
        } finally {
            env.close()
        }
    }

    String getControllerName() {
        renderRequestAttributes.controllerName
    }
}
