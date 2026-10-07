package grails.plugins.rendering.document

import grails.util.Environment
import org.grails.web.servlet.WrappedResponseHolder
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification

abstract class ProductionEnvironmentSpec extends Specification {

    private String previousEnv

    def setup() {
        previousEnv = System.getProperty(Environment.KEY)
        System.setProperty(Environment.KEY, Environment.PRODUCTION.name)
        assert Environment.current == Environment.PRODUCTION
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
}
