/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.test.acceptance.docker;

import org.apache.commons.io.FileUtils;
import org.hamcrest.core.Is;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author asotobueno
 */
public class DockerTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void should_load_fixture_from_default_location() throws IOException {
        File outputDir = folder.newFolder();
        Docker docker = new Docker();
        docker.copyDockerfileDirectory(TestContainer.class, TestContainer.class.getAnnotation(DockerFixture.class), outputDir);
        assertThat(new File(outputDir, "Dockerfile").exists(), is(true));
    }

    @Test
     public void should_load_fixture_from_location() throws IOException {
        File outputDir = folder.newFolder();
        Docker docker = new Docker();
        docker.copyDockerfileDirectory(CustomTestContainer.class, CustomTestContainer.class.getAnnotation(DockerFixture.class), outputDir);
        assertThat(new File(outputDir, "Dockerfile").exists(), is(true));
    }

    @Test
    public void should_load_fixture_from_location_when_using_classpath_schema() throws IOException {
        File outputDir = folder.newFolder();
        Docker docker = new Docker();
        docker.copyDockerfileDirectory(CustomClasspathTestContainer.class, CustomClasspathTestContainer.class.getAnnotation(DockerFixture.class), outputDir);
        assertThat(new File(outputDir, "Dockerfile").exists(), is(true));
    }

    @Test
    public void should_load_fixture_from_location_when_using_file_schema() throws IOException {
        final File dockerfileDirectory = folder.newFolder();
        FileUtils.writeStringToFile(new File(dockerfileDirectory, "Dockerfile"), "FROM java:8-jre");

        File outputDir = folder.newFolder();
        Docker docker = new Docker();
        DockerFixture annotation = new DockerFixture() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return DockerFixture.class;
            }

            @Override
            public String id() {
                return "test";
            }

            @Override
            public int[] ports() {
                return new int[]{8080};
            }

            @Override
            public String bindIp() {
                return "127.0.0.1";
            }

            @Override
            public String dockerfile() {
                return "file://" + dockerfileDirectory.getAbsolutePath();
            }
        };

        docker.copyDockerfileDirectory(CustomFileTestContainer.class, annotation, outputDir);
        assertThat(new File(outputDir, "Dockerfile").exists(), is(true));
    }


    @Test
    public void should_load_fixture_from_default_jar() throws IOException, ClassNotFoundException {

        //Creates a jar file with required content
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClass(TestJarContainer.class)
                .addClasses(DockerContainer.class, DockerFixture.class)
                .addAsResource(
                        new StringAsset("FROM java:8-jre"),
                        "/org/jenkinsci/test/acceptance/docker/DockerTest/TestJarContainer/Dockerfile"
                );
        File jarDirectory = folder.newFolder();
        File jarFile = new File(jarDirectory, "test.jar");
        jar.as(ZipExporter.class).exportTo(jarFile);

        //Creates a class loader that depends on the system one loading the jar. Jars are isolated from the test ones.
        ClassLoader classloader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, null);
        Class<? extends DockerContainer> clazz = (Class<? extends DockerContainer>) Class.forName("org.jenkinsci.test.acceptance.docker.DockerTest$TestJarContainer", true, classloader);

        //Executes the test
        File outputDir = folder.newFolder();
        Docker docker = new Docker();
        docker.copyDockerfileDirectory(clazz, TestJarContainer.class.getAnnotation(DockerFixture.class), outputDir);
        assertThat(new File(outputDir, "Dockerfile").exists(), is(true));
    }

    @Test
    public void should_load_fixture_from_jar() throws IOException, ClassNotFoundException {

        //Creates a jar file with required content
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClass(CustomTestJarContainer.class)
                .addClasses(DockerContainer.class, DockerFixture.class)
                .addAsResource(
                        new StringAsset("FROM java:8-jre"),
                        "/test/Dockerfile"
                );
        File jarDirectory = folder.newFolder();
        File jarFile = new File(jarDirectory, "test.jar");
        jar.as(ZipExporter.class).exportTo(jarFile);

        //Creates a class loader that depends on the system one loading the jar. Jars are isolated from the test ones.
        ClassLoader classloader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, null);
        Class<? extends DockerContainer> clazz = (Class<? extends DockerContainer>) Class.forName("org.jenkinsci.test.acceptance.docker.DockerTest$CustomTestJarContainer", true, classloader);

        //Executes the test
        File outputDir = folder.newFolder();
        Docker docker = new Docker();
        docker.copyDockerfileDirectory(clazz, CustomTestJarContainer.class.getAnnotation(DockerFixture.class), outputDir);
        assertThat(new File(outputDir, "Dockerfile").exists(), is(true));
    }

    @DockerFixture(id="test", ports = 8080, dockerfile = "classpath://test")
    public static class CustomClasspathTestContainer extends DockerContainer {
    }

    @DockerFixture(id="test", ports = 8080, dockerfile = "file://calculateatruntime")
    public static class CustomFileTestContainer extends DockerContainer {
    }

    @DockerFixture(id="test", ports = 8080, dockerfile = "test")
    public static class CustomTestContainer extends DockerContainer {
    }

    @DockerFixture(id="test", ports = 8080)
    public static class TestContainer extends DockerContainer {
    }

    @DockerFixture(id="testjar", ports = 8080)
    public static class TestJarContainer extends DockerContainer {
    }

    @DockerFixture(id="testjar", ports = 8080, dockerfile = "test")
    public static class CustomTestJarContainer extends DockerContainer {
    }
}
