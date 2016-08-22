Byteman Integration for the Arquillian Project

Mocking 2.0, Runtime bytecode manipulation in Integration tests. 

Usage
-----

`@BMRule` and `@BMRules` can be placed on both Class and Method level in the TestClass. 
The given rules will be active during BeforeClass or Before to AfterClass or After.
  

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
<version.byteman>2.0.0</version.byteman>

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

* autoInstallAgent (default false)
  If true the extension will attempt to install the Byteman Agent in the target Container runtime. 
  If false it assumes the Byteman Agent is manually installed.
  autoInstallAgent *requires* tools.jar on the container classpath to perform the installation. 

* agentProperties
  Additional properties to use when auto installing the Byteman Agent. See the Byteman documentation for more details.


Notes
------

When using *autoInstallAgent* with JBoss AS 7 the com.sun.tools.attach package 
has to be exposed as a system package and tools.jar added to the bootstrap classpath.

```xml
<property name="javaVmArguments">-Djboss.modules.system.pkgs=com.sun.tools.attach,org.jboss.byteman -Xbootclasspath/a:${path.tools_jar}</property>
```
