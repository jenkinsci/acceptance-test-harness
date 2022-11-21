/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
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
package org.jenkinsci.test.acceptance.junit;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.jenkinsci.test.acceptance.utils.ElasticTime;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Sleeper;

import com.google.common.base.Function;

/**
 * ATH specific wait object.
 *
 * @author ogondza
 *
 * @param <Subject> Argument type passed to callback.
 */
public class Wait<Subject> extends FluentWait<Subject> {

    private static final class ElasticClock extends Clock {

        private long baseMillis = System.currentTimeMillis();
        private final ElasticTime time;
        private ZoneId zoneId;

        public ElasticClock(ElasticTime time) {
            this(time, ZoneId.of("Z"));
        }

        private ElasticClock(ElasticTime time, ZoneId zoneId) {
            this.time = time;
            this.zoneId = zoneId;
        }

        @Override
        public ZoneId getZone() {
            return zoneId;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new ElasticClock(time, zone);
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis());
        }

        @Override
        public long millis() {
            long realElapsed = System.currentTimeMillis() - baseMillis;
            // slow the clock down..
            long elasticElapsed = Math.round(realElapsed / time.getSlowDownFactor());
            return baseMillis + elasticElapsed;
        }
    }

    /** Predicate and input reference stored when {@link Predicate} is used so we can diagnose. */
    private Predicate<?> predicate;
    private Subject input;

    /**
     * Create wait with configurable timer.
     * <p>
     * This is useful for timeout waiting for tasks to complete that might be dependent on test environment.
     */
    public Wait(Subject input, ElasticTime time) {
        super(input, new ElasticClock(time), Sleeper.SYSTEM_SLEEPER);
        this.input = input;
    }

    /**
     * Create wait with configurable timer.
     * <p>
     * This is useful for timeout waiting wall-clock time to pass.
     *
     * @see Wait#Wait(Object, ElasticTime)
     */
    public Wait(Subject input) {
        super(input);
        this.input = input;
    }

    public Wait<Subject> withMessage(String pattern, Object... args) {
        withMessage(String.format(pattern, args));
        return this;
    }

    // For convenience as we have quite a lot of Callables historically
    public <Return> Return until(final Callable<Return> isTrue) {
        return super.until(new Function<Subject, Return>() {
            @Override
            public Return apply(Subject input) {
                try {
                    return isTrue.call();
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            }

            @Override
            public String toString() {
                return isTrue.toString();
            }
        });
    }

    public void until(final Matcher<? super Subject> matcher) {
        until(new Predicate<Boolean>() {
            @Override public Boolean apply() throws Exception {
                return matcher.matches(input);
            }

            @Override public String diagnose(Throwable lastException, String message) {
                StringDescription desc = new StringDescription();
                matcher.describeMismatch(input, desc);
                return desc.toString();
            }
        });
    }

    public <Return> Return until(final Wait.Predicate<Return> isTrue) {
        Function<Subject, Return> fun = new Function<Subject, Return>() {
            @Override
            public Return apply(Subject input) {
                try {
                    return isTrue.apply();
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            }

            @Override
            public String toString() {
              return isTrue.toString();
            }
        };

        predicate = isTrue;
        try {
            return super.until(fun);
        } finally {
            predicate = null;
        }
    }

    @Override
    protected RuntimeException timeoutException(String message, Throwable lastException) {
        if (predicate != null) {
            String diagnosis = predicate.diagnose(lastException, message);
            if (diagnosis != null && !diagnosis.equals("")) {
                message += ". " + diagnosis;
            }
        }

        return super.timeoutException(message, lastException);
    }

    public static abstract class Predicate<Return> {

        public abstract Return apply() throws Exception;

        /**
         * Create additional text description on the failure.
         * <p>
         * Both lastException and message will be reported separately.
         */
        public abstract String diagnose(Throwable lastException, String message);
    }

    // Return subclass

    @Override
    public Wait<Subject> withTimeout(long duration, TimeUnit unit) {
        return (Wait<Subject>) super.withTimeout(duration, unit);
    }

    @Override
    public Wait<Subject> withMessage(String message) {
        return (Wait<Subject>) super.withMessage(message);
    }

    @Override
    public Wait<Subject> pollingEvery(long duration, TimeUnit unit) {
        return (Wait<Subject>) super.pollingEvery(duration, unit);
    }

    @Override
    public Wait<Subject> ignoring(Class<? extends Throwable> exceptionType) {
        return (Wait<Subject>) super.ignoring(exceptionType);
    }

    @Override
    public Wait<Subject> ignoring(Class<? extends Throwable> firstType, Class<? extends Throwable> secondType) {
        return (Wait<Subject>) super.ignoring(firstType, secondType);
    }
}
