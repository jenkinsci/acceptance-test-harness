package org.jenkinsci.test.acceptance.docker.fixtures;

/**
 * gets username and password for a service on a docker container
 *
 * @author Tobias Meyer
 */
public interface IPasswordDockerContainer {
    /**
     * Gets the passsword for a service on the docker server
     *
     * @return password
     */
    String getPassword() ;
    /**
     * Gets the username for a service on the docker server
     *
     * @return username
     */
    String getUsername() ;

}
