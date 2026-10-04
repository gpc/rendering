package grails.plugins.rendering.document

import grails.gsp.PageRenderer
import groovy.transform.CompileStatic
import jakarta.servlet.ServletContext
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.grails.web.servlet.WrappedResponseHolder
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
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
    private boolean ownsRenderRequest
    private Object originalOut
    private Object originalLocaleResolver

    RenderEnvironment(ApplicationContext applicationContext, Writer out, Locale locale = null) {
        this.out = out
        this.locale = locale
        this.applicationContext = applicationContext
    }

    private void init() {
        originalRequestAttributes = RequestContextHolder.getRequestAttributes()
        originalWrappedResponse = WrappedResponseHolder.wrappedResponse
        Locale renderLocale = locale ?: callerLocale()
        HttpServletResponse response = PageRenderer.PageRenderResponseCreator.createInstance(new PrintWriter(out), renderLocale)

        if (originalRequestAttributes instanceof GrailsWebRequest) {
            renderRequestAttributes = (GrailsWebRequest) originalRequestAttributes
            originalOut = renderRequestAttributes.currentRequest.getAttribute(GrailsApplicationAttributes.OUT)
            originalLocaleResolver = renderRequestAttributes.currentRequest.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE)
        } else {
            ownsRenderRequest = true
            HttpServletRequest request = PageRenderer.PageRenderRequestCreator.createInstance('/', renderLocale)
            renderRequestAttributes = new GrailsWebRequest(request, response, servletContext, applicationContext)
            RequestContextHolder.setRequestAttributes(renderRequestAttributes)
        }

        renderRequestAttributes.currentRequest.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new FixedLocaleResolver(renderLocale))
        renderRequestAttributes.out = out
        WrappedResponseHolder.wrappedResponse = response
    }

    private void close() {
        if (ownsRenderRequest) {
            RequestContextHolder.setRequestAttributes(originalRequestAttributes)
        } else {
            HttpServletRequest request = renderRequestAttributes.currentRequest
            request.setAttribute(GrailsApplicationAttributes.OUT, originalOut)
            request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, originalLocaleResolver)
        }
        WrappedResponseHolder.wrappedResponse = originalWrappedResponse
    }

    private Locale callerLocale() {
        originalRequestAttributes instanceof GrailsWebRequest ?
                RequestContextUtils.getLocale(((GrailsWebRequest) originalRequestAttributes).currentRequest) : Locale.default
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
