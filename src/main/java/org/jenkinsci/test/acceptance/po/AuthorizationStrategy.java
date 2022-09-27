package org.jenkinsci.test.acceptance.po;

/**
 * Base type for {@link PageArea} for Authorization Strategy.
 * <p>
 * Use {@link Describable} annotation to register an implementation.
 *
 * @see GlobalSecurityConfig#useAuthorizationStrategy(Class)
 */
public abstract class AuthorizationStrategy extends PageAreaImpl {
    protected AuthorizationStrategy(GlobalSecurityConfig context, String path) {
        super(context, path);
    }
}
