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
package org.jenkinsci.test.acceptance.plugins.docker_build_step;

import com.google.common.base.Joiner;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

public abstract class DockerCommand extends PageAreaImpl {

    protected DockerCommand(DockerBuildStep area) {
        super(area, "dockerCmd");
    }

    @Describable("Create image")
    public static final class CreateImage extends DockerCommand {

        public CreateImage(DockerBuildStep area) {
            super(area);
        }

        public CreateImage contextFolder(String contextFolder) {
            control("dockerFolder").set(contextFolder);
            return this;
        }

        public CreateImage tag(String tag) {
            control("imageTag").set(tag);
            return this;
        }
    }

    @Describable("Create container")
    public static final class CreateContainer extends DockerCommand {

        public CreateContainer(DockerBuildStep area) {
            super(area);
        }

        public CreateContainer name(String image) {
            control("image").set(image);
            return this;
        }
    }

    @Describable("Start container(s)")
    public static class StartContainers extends DockerCommand {

        public StartContainers(DockerBuildStep area) {
            super(area);
        }

        public StartContainers containerIds(String ids) {
            control("containerIds").set(ids);
            return this;
        }
    }

    @Describable("Remove container(s)")
    public static final class RemoveContainers extends DockerCommand {

        public RemoveContainers(DockerBuildStep area) {
            super(area);
        }

        public RemoveContainers containerIds(String... ids) {
            control("containerIds").set(Joiner.on(',').join(ids));
            return this;
        }
    }
}
