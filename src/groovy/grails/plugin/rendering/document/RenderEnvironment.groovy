package grails.plugin.rendering.document

import grails.util.GrailsWebUtil

import org.codehaus.groovy.grails.web.servlet.WrappedResponseHolder
import org.springframework.context.ApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.i18n.FixedLocaleResolver
import org.springframework.web.servlet.support.RequestContextUtils

class RenderEnvironment {

	final Writer out
	final Locale locale
	final ApplicationContext applicationContext

	private originalRequestAttributes
	private renderRequestAttributes

	private originalOut

	RenderEnvironment(ApplicationContext applicationContext, Writer out, Locale locale = null) {
		this.out = out
		this.locale = locale
		this.applicationContext = applicationContext
	}

	private init() {
		originalRequestAttributes = RequestContextHolder.getRequestAttributes()
		renderRequestAttributes = GrailsWebUtil.bindMockWebRequest(applicationContext)

		if (originalRequestAttributes) {
			renderRequestAttributes.controllerName = originalRequestAttributes.controllerName
		}

		def renderLocale
		if (locale) {
			renderLocale = locale
		} else if (originalRequestAttributes) {
			renderLocale = RequestContextUtils.getLocale(originalRequestAttributes.request)
		}

		renderRequestAttributes.request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE,
			new FixedLocaleResolver(defaultLocale: renderLocale))

		renderRequestAttributes.setOut(out)
		WrappedResponseHolder.wrappedResponse = renderRequestAttributes.currentResponse
	}

	private close() {
		RequestContextHolder.setRequestAttributes(originalRequestAttributes) // null ok
		WrappedResponseHolder.wrappedResponse = originalRequestAttributes?.currentResponse
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
