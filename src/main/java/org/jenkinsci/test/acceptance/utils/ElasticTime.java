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
package org.jenkinsci.test.acceptance.utils;

import java.util.concurrent.TimeUnit;

/**
 * Scale time measurement to support individual execution schemes.
 *
 * Due to the nature of the harness, the framework is full of timeouts waiting for things to happen. Given the number of
 * modes of execution, environments and configurations there is, there are no right timeouts to balance the desire to
 * abort operations that takes too long and necessity not to interrupt operations taking a bit more time to complete
 * successfully.
 *
 * To reflect that, use <tt>-DElasticTime.factor</tt> and configure the factor to slow down / speed up the measured time
 * for your executions. Floating point values are accepted too.
 *
 * This implementation takes number of concurrent threads into account.
 *
 * @author ogondza
 */
public class ElasticTime {

    /**
     * Amount of threads executing concurrently. Time is slowed down proportionally multiplying the time;
     */
    private final int concurrency = Integer.parseInt(System.getProperty("forkCount", "1"));

    /**
     * Relative performance difference compared to reference environment (Upstream CI).
     *
     * Default is 1.0 (no difference). Use >1 in case of slower environment, <1 in case of faster one.
     */
    private final double factor = Double.parseDouble(System.getProperty("ElasticTime.factor", "1.0"));

    public long seconds(long secs) {
        return milliseconds(TimeUnit.SECONDS.toMillis(secs));
    }

    public long milliseconds(long ms) {
        double actualSeconds = concurrency * factor;
        return Math.round(ms * actualSeconds);
    }
}
