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
import org.springframework.web.context.request.ServletRequestAttributes
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
            GrailsWebRequest callerRequest = (GrailsWebRequest) originalRequestAttributes
            originalOut = callerRequest.currentRequest.getAttribute(GrailsApplicationAttributes.OUT)
            originalLocaleResolver = callerRequest.currentRequest.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE)
            renderRequestAttributes = callerRequest
        } else {
            ServletContext context = servletContext
            HttpServletRequest request = PageRenderer.PageRenderRequestCreator.createInstance('/', renderLocale)
            if (context) {
                request.setAttribute(GrailsApplicationAttributes.APP_URI_ATTRIBUTE, context.contextPath)
            }
            GrailsWebRequest ownRequest = new GrailsWebRequest(request, response, context, applicationContext)
            RequestContextHolder.setRequestAttributes(ownRequest)
            renderRequestAttributes = ownRequest
            ownsRenderRequest = true
        }

        renderRequestAttributes.currentRequest.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new FixedLocaleResolver(renderLocale))
        renderRequestAttributes.out = out
        WrappedResponseHolder.wrappedResponse = response
    }

    private void close() {
        if (renderRequestAttributes == null) {
            return
        }
        try {
            if (ownsRenderRequest) {
                renderRequestAttributes.requestCompleted()
            }
        } finally {
            restoreCallerState()
        }
    }

    private void restoreCallerState() {
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
        originalRequestAttributes instanceof ServletRequestAttributes ?
                RequestContextUtils.getLocale(((ServletRequestAttributes) originalRequestAttributes).request) : Locale.default
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
        try {
            env.init()
            block(env)
        } finally {
            env.close()
        }
    }

    String getControllerName() {
        renderRequestAttributes.controllerName
    }
}
