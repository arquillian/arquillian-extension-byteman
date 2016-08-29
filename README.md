# Byteman Integration for the Arquillian Project

Mocking 2.0, Runtime bytecode manipulation in Integration tests. 

Usage
-----

`@BMRule` and `@BMRules` can be placed on both Class and Method level in the TestClass. 

The given rules will be active during `BeforeClass` or `Before` to `AfterClass` or `After`.
  

```java
@RunWith(Arquillian.class)
@BMRules(
        @BMRule(
                name = "Throw exception on success", targetClass = "StatelessManagerBean", targetMethod = "forcedClassLevelFailure", 
                action = "throw new java.lang.RuntimeException()")
)
public class BytemanFaultInjectionTestCase {

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(StatelessManager.class, StatelessManagerBean.class);
    }

    @EJB(mappedName = "java:module/StatelessManagerBean")
    private StatelessManager bean;

    @Test(expected = EJBException.class)
    @BMRule(
            name = "Throw exception on success", targetClass = "StatelessManagerBean", targetMethod = "forcedMethodLevelFailure", 
            action = "throw new java.lang.RuntimeException()")
    public void shouldBeAbleToInjectMethodLevelThrowRule()
    {
        Assert.assertNotNull("Verify bean was injected", bean);
        bean.forcedMethodLevelFailure();
    }
}
```

Required dependencies
---------------------

```xml
<version.byteman>LATEST_RELEASE</version.byteman>

<dependency>
    <groupId>org.jboss.byteman</groupId>
    <artifactId>byteman</artifactId>
    <version>${version.byteman}</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.jboss.byteman</groupId>
    <artifactId>byteman-submit</artifactId>
    <version>${version.byteman}</version>
    <scope>provided</scope>
</dependency>
```


Configuration
-------------

```xml
<extension qualifier="byteman">
  <property name="autoInstallAgent">true</property>
  <property name="agentProperties">org.jboss.byteman.verbose=true</property>
</extension>
```

* `autoInstallAgent` (default false)
  If `true` the extension will attempt to install the Byteman Agent in the target Container runtime. 
  If `false` it assumes the Byteman Agent is manually installed.
  `autoInstallAgent` *requires* `tools.jar` on the container classpath to perform the installation. 

* `agentProperties`
  Additional properties to use when auto installing the Byteman Agent. See the [Byteman documentation](http://byteman.jboss.org/docs.html) for more details.

How to run the tests
--------------------
As we are using [Arquillian Chameleon](https://github.com/arquillian/arquillian-container-chameleon), it's very easy to run the tests against different containers. To see the comprehensive list of what is supported out-of-the-box [check here](https://github.com/arquillian/arquillian-container-chameleon/blob/master/src/main/resources/chameleon/default/containers.yaml).

To run test against different version of WildFly only change `chameleonTarget` like follow:

```bash
$ mvn test -Darq.container.chameleon.configuration.chameleonTarget="wildfly:10.1.0.Final:managed"
```

If you have not provided `chameleonTarget` then it will take default value provided in `arquillian.xml`.

While running the tests from an IDE, variable `${path.tools_jar}` in `arquillian.xml` is not defined, as it's only defined in Maven build. Thus the execution results with `NoClassDefFoundError`. One possible way to overcome this problem is to hardcode Byteman settings in `arquillian.xml` pointing to the `tools.jar` file from your JDK. See commented out section in `arquillian.xml`.

Notes
------

When using `autoInstallAgent` with application server such as WildFly, the `com.sun.tools.attach` package has to be exposed as a system package and `tools.jar` added to the bootstrap classpath.

```xml
<property name="javaVmArguments">-Djboss.modules.system.pkgs=com.sun.tools.attach,org.jboss.byteman -Xbootclasspath/a:${path.tools_jar}</property>
```
