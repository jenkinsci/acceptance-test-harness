# Exercised Plugins Report
It is possible to create an **Exercised Plugins** Report by specifying the `EXERCISEDPLUGINREPORTER` environment variable.

## Reporter Types
The following types are available:

 * `console` (default)
 * `textfile`

## Console Reporter
The Console Reporter simply logs the plugin and its version to standard output.

Here is an example:

    Plugin ldap/1.20.2 is installed

## Text File Reporter
The Text File Reporter will create a properties file in the `target` folder containing a list of plugin names and their versions prefixed by the test name.

Here is an example:

	plugins.LdapPluginTest\:\:ldap = 1.10.2
	plugins.ActiveDirectoryTest\:\:active-directory = 1.38

This Reporter type can be very useful when you want to be able to see which plugins and their versions were tested with a particular version of Jenkins Core.

	Note: The output file is re-created at the start of each test suite run.

