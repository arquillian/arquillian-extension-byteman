package org.jboss.arquillian.extension.byteman.api;

/**
 * Execution env type.
 * <p>
 * This enum describes where we need to install the rules;
 * into client's or container's JVM, or both.
 * <p>
 * ALL - install into both, client and container JVM
 * CLIENT - client JVM only
 * CLIENT_CONTAINER - install rules into conatiner's JVM, before executing client test(s)
 * CONTAINER - container JVM only
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public enum ExecType {
    ALL,
    CLIENT,
    CLIENT_CONTAINER,
    CONTAINER
}
