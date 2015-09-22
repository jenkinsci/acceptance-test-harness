package org.jenkinsci.test.acceptance.recorder;

import org.junit.runner.Description;
import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.screenrecorder.ScreenRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class JUnitScreenRecorder extends ScreenRecorder {

    private static final Logger logger = LoggerFactory.getLogger(JUnitScreenRecorder.class);

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
            if (!this.movieFolder.mkdirs()) {
                logger.warn("Directory {} could not be created. mkdirs operation returned false.", this.movieFolder);
            }
        } else {
            if(!this.movieFolder.isDirectory()) {
                logger.warn("{} is not a directory.", this.movieFolder);
            }
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
