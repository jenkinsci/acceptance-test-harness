package org.jenkinsci.test.acceptance.recorder;

import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.VideoFormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * JUnit Rule that before executing a test it starts a recording current screen
 * and after the test is executed, it stops recording.
 */
public class TestRecorderRule extends TestWatcher {

    private static final Logger logger = LoggerFactory.getLogger(TestRecorderRule.class);

    private static final int MAX_RECORDING_TIME_SECS = 120000;
    private static final int FRAME_RATE_PER_SEC = 60;
    private static final int BIT_DEPTH = 16;
    private static final float QUALITY_RATIO = 0.97f;
    public static final String TARGET = "target";

    private JUnitScreenRecorder screenRecorder;

    @Override
    protected void starting(Description description) {
        if(!isRecorderDisabled()) {
            startRecording(description);
        }
    }

    private void startRecording(Description des) {
        GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

        File movieFolder = new File(TARGET);

        String mimeType = FormatKeys.MIME_QUICKTIME;
        String videoFormatName = VideoFormatKeys.ENCODING_QUICKTIME_ANIMATION;
        String compressorName = VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_ANIMATION;
        Dimension outputDimension = gc.getBounds().getSize();
        int bitDepth = BIT_DEPTH;
        float quality = QUALITY_RATIO;
        int screenRate = FRAME_RATE_PER_SEC;
        long maxRecordingTime = MAX_RECORDING_TIME_SECS;

        Format outputFormatForScreenCapture = getOutputFormatForScreenCapture(videoFormatName,
                compressorName, outputDimension,
                bitDepth, quality, screenRate);

        try {
            this.screenRecorder = new JUnitScreenRecorder
                    (gc, gc.getBounds(), getFileFormat(mimeType),
                            outputFormatForScreenCapture, null, null, movieFolder, des);
            this.screenRecorder.start();
        } catch (IOException e) {
            logger.warn("Exception starting test recording {}", e);
        } catch (AWTException e) {
            logger.warn("Exception starting test recording {}", e);
        }
    }

    @Override
    protected void succeeded(Description description) {
        if (this.screenRecorder != null) {
            if (saveAllExecutions()) {
                stopRecordingWithFinalWaiting();
            } else {
                stopRecording();
                this.screenRecorder.removeMovieFile();
            }
        }
    }

    @Override
    protected void finished(Description description) {
        stopRecordingWithFinalWaiting();
    }

    private boolean isRecorderDisabled() {
        return Boolean.getBoolean("RECORDER_DISABLED");
    }

    private boolean saveAllExecutions() {
        return Boolean.getBoolean("RECORDER_SAVE_ALL");
    }

    private void stopRecording() {
        stopRecording(false);
    }

    private void stopRecordingWithFinalWaiting() {
        stopRecording(true);
    }

    private void stopRecording(boolean waitTime) {
        if (this.screenRecorder != null && this.screenRecorder.getState() == ScreenRecorder.State.RECORDING) {
            try {
                if (waitTime) {
                    waitUntilLastFramesAreRecorded();
                }
                screenRecorder.stop();
            } catch (IOException e) {
                logger.warn("Exception stoping test recording {}.", e);
            } catch (InterruptedException e) {
                logger.warn("Exception stoping test recording {}.", e);
            }
        }
    }

    private void waitUntilLastFramesAreRecorded() throws InterruptedException {
        //Values below 500 milliseconds result in no recording latest frames.
        TimeUnit.MILLISECONDS.sleep(500);
    }

    private Format getFileFormat(String mimeType) {
        return new Format(FormatKeys.MediaTypeKey, FormatKeys.MediaType.FILE, FormatKeys.MimeTypeKey, mimeType);
    }

    private Format getOutputFormatForScreenCapture(String videoFormatName, String compressorName,
                                                   Dimension outputDimension, int bitDepth, float quality,
                                                   int screenRate) {
        return new Format(FormatKeys.MediaTypeKey, FormatKeys.MediaType.VIDEO, FormatKeys.EncodingKey,
                videoFormatName,
                VideoFormatKeys.CompressorNameKey, compressorName,
                VideoFormatKeys.WidthKey, outputDimension.width,
                VideoFormatKeys.HeightKey, outputDimension.height,
                VideoFormatKeys.DepthKey, bitDepth, FormatKeys.FrameRateKey, Rational.valueOf(screenRate),
                VideoFormatKeys.QualityKey, quality,
                FormatKeys.KeyFrameIntervalKey, screenRate * 5 // one keyframe per 5 seconds
        );
    }
}
