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
package org.jenkinsci.test.acceptance.recorder;

import org.junit.runner.Description;
import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.screenrecorder.ScreenRecorder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JUnitScreenRecorder extends ScreenRecorder {

    private Description description;
    private Format format;

    public JUnitScreenRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat,
                               Format screenFormat, Format mouseFormat, Format audioFormat,
                               File movieFolder, Description description) throws IOException, AWTException {
        super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
        this.format = fileFormat;
        this.description = description;
    }

    protected File createMovieFile(Format fileFormat) throws IOException {
        if(!this.movieFolder.exists()) {
            this.movieFolder.mkdirs();
        } else if(!this.movieFolder.isDirectory()) {
            throw new IOException("\"" + this.movieFolder + "\" is not a directory.");
        }

        final File f = generateOutput(fileFormat);
        return f;
    }

    public void removeMovieFile() {
        final File output = generateOutput(this.format);
        if(output.exists()) {
            output.delete();
        }
    }

    private File generateOutput(final Format fileFormat) {
        final String filename = this.description.getClassName() + "-" + this.description.getMethodName();
        return new File(this.movieFolder, filename + "." + Registry.getInstance().getExtension(fileFormat));
    }
}
