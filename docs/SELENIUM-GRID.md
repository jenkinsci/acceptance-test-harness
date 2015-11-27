#Running ATH tests against Selenium Grid

This feature enables us to test ATH against Selenium Grid

##Requirements
* JENKINS_URL must be accessible for the Selenium hub
* If running against existing Jenkins instance using Jenkins Under Test (JUT) =EXISTING, A running Jenkins instance with pre-installed [Form-Element-Path plugin](https://wiki.jenkins-ci.org/display/JENKINS/Form+Element+Path+Plugin)
* Selenium hub running, with at least one node registered to it. Can be modified with parameter [gridHubURL](../src/main/resources/seleniumGrid.properties#L1) <br/ >
Refer this link for detailed information about [Selenium Grid] (http://www.seleniumhq.org/docs/07_selenium_grid.jsp)
* Browser (Make sure that the Selenium node supports the browser of your choice). Can be modified with parameter [browserval](../src/main/resources/seleniumGrid.properties#L3)

## (Optional) Configuring additional capabilities

Additional capabilities supported by your node can be configured and passed to [FallbackConfig.java](../src/main/java/org/jenkinsci/test/acceptance/FallbackConfig.java#L127-L139) <br />
as following:

```java
case "seleniumgrid":
cap.setBrowserName(properties.getProperty( /* capability or key-value pairs */ ));
```
In case you are giving browser specific capabilities, such as `"Firefox","30"` then replace this capability instead of [browserval](../src/main/resources/seleniumGrid.properties#L3)

## Running tests

Set BROWSER=seleniumGrid. Refer to [BROWSER.md](BROWSER.md) for more information about different browser types. <br />

Example:
`TYPE=existing BROWSER=seleniumGrid JENKINS_URL=http://localhost:8080/jenkins/ mvn clean install`


