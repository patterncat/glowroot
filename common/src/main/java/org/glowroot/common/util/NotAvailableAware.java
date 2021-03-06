/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.glowroot.common.util;

import javax.annotation.Nullable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class NotAvailableAware {

    public static final int NA = -1;

    private NotAvailableAware() {}

    public static double add(double x, double y) {
        if (isNA(x) || isNA(y)) {
            return NA;
        }
        return x + y;
    }

    public static long add(long x, long y) {
        if (isNA(x) || isNA(y)) {
            return NA;
        }
        return x + y;
    }

    public static double addMillisToNanos(double x, long y) {
        if (isNA(x) || isNA(y)) {
            return NA;
        }
        return x + MILLISECONDS.toNanos(y);
    }

    public static long addMillisToNanos(long x, long y) {
        if (isNA(x) || isNA(y)) {
            return NA;
        }
        return x + MILLISECONDS.toNanos(y);
    }

    public static boolean isNA(double value) {
        return value == NA;
    }

    public static boolean isNA(long value) {
        return value == NA;
    }

    public static @Nullable Double orNull(double value) {
        return value == NA ? null : value;
    }
}
