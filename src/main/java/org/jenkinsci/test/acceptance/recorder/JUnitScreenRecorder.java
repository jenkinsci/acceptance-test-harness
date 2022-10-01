package org.jenkinsci.test.acceptance.recorder;

import org.jenkinsci.test.acceptance.junit.FailureDiagnostics;
import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.screenrecorder.ScreenRecorder;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.AWTException;
import java.io.File;
import java.io.IOException;

/**
 * Custom Screen Recorder that allow to rename video output file.
 */
public class JUnitScreenRecorder extends ScreenRecorder {

    private FailureDiagnostics diag;
    private Format format;

    public JUnitScreenRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat,
                               Format screenFormat, Format mouseFormat, Format audioFormat,
                               FailureDiagnostics diag) throws IOException, AWTException {
        super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat);
        this.format = fileFormat;
        this.diag = diag;
    }

    @Override
    protected File createMovieFile(Format fileFormat) {
        return generateOutput(fileFormat);
    }

    public void removeMovieFile() {
        final File output = generateOutput(this.format);
        if (output.exists()) {
            output.delete();
        }
    }

    private File generateOutput(final Format fileFormat) {
        return diag.touch("ui-recording." + Registry.getInstance().getExtension(fileFormat));
    }
}
