package com.ulisesbocchio.jasyptspringboot.aop;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter;
import com.ulisesbocchio.jasyptspringboot.InterceptionMode;
import com.ulisesbocchio.jasyptspringboot.configuration.EnvCopy;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.env.PropertySource;

import java.util.List;

/**
 * @author Ulises Bocchio
 */
public class EncryptableMutablePropertySourcesInterceptor implements MethodInterceptor {

    private final InterceptionMode interceptionMode;
    private final List<Class<PropertySource<?>>> skipPropertySourceClasses;
    private final EncryptablePropertyResolver resolver;
    private final EncryptablePropertyFilter filter;
    private final EnvCopy envCopy;

    public EncryptableMutablePropertySourcesInterceptor(InterceptionMode interceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter, EnvCopy envCopy) {
        this.interceptionMode = interceptionMode;
        this.skipPropertySourceClasses = skipPropertySourceClasses;
        this.resolver = resolver;
        this.filter = filter;
        this.envCopy = envCopy;
    }

    private Object makeEncryptable(Object propertySource) {
        return EncryptablePropertySourceConverter.makeEncryptable(interceptionMode, skipPropertySourceClasses, resolver, filter, (PropertySource<?>) propertySource);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String method = invocation.getMethod().getName();
        Object[] arguments = invocation.getArguments();
        switch (method) {
            case "addFirst":
                envCopy.get().getPropertySources().addFirst((PropertySource<?>) arguments[0]);
                return invocation.getMethod().invoke(invocation.getThis(), makeEncryptable(arguments[0]));
            case "addLast":
                envCopy.get().getPropertySources().addLast((PropertySource<?>) arguments[0]);
                return invocation.getMethod().invoke(invocation.getThis(), makeEncryptable(arguments[0]));
            case "addBefore":
                envCopy.get().getPropertySources().addBefore((String) arguments[0], (PropertySource<?>) arguments[1]);
                return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
            case "addAfter":
                envCopy.get().getPropertySources().addAfter((String) arguments[0], (PropertySource<?>) arguments[1]);
                return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
            case "replace":
                envCopy.get().getPropertySources().replace((String) arguments[0], (PropertySource<?>) arguments[1]);
                return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
            default:
                return invocation.proceed();
        }

    }
}
