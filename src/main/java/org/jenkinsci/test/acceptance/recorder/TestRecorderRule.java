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

import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.VideoFormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * JUnit Rule that before executing a test it starts a recording current screen
 * and after the test is executed, it stops recording.
 */
public class TestRecorderRule extends TestWatcher {


    private static final int MAX_RECORDING_TIME_SECS = 120000;
    private static final int FRAME_RATE_PER_SEC = 60;
    private static final int BIT_DEPTH = 16;
    private static final float QUALITY_RATIO = 0.97f;

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

        File movieFolder = new File("target");

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
            throw new IllegalArgumentException("Exception starting test recording.", e);
        } catch (AWTException e) {
            throw new IllegalArgumentException("Exception starting test recording.", e);
        }
    }

    @Override
    protected void succeeded(Description description) {
        if (this.screenRecorder != null) {
            stopRecording();
            if(!saveAllExecutions()) {
                this.screenRecorder.removeMovieFile();
            }
        }
    }

    @Override
    protected void finished(Description description) {
        stopRecording();
    }

    private boolean isRecorderDisabled() {
        return Boolean.getBoolean("RECORDER_DISABLED");
    }

    private boolean saveAllExecutions() {
        return Boolean.getBoolean("RECORDER_SAVE_ALL");
    }

    private void stopRecording() {
        if (this.screenRecorder != null && this.screenRecorder.getState() == ScreenRecorder.State.RECORDING) {
            try {
                TimeUnit.SECONDS.sleep(1);
                screenRecorder.stop();
            } catch (IOException e) {
                throw new IllegalArgumentException("Exception stopping test recording.", e);
            } catch (InterruptedException e) {
                throw new IllegalArgumentException("Exception stopping test recording.", e);
            }
        }
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
                FormatKeys.KeyFrameIntervalKey, screenRate * 10 // one keyframe per 10 seconds
        );
    }
}
