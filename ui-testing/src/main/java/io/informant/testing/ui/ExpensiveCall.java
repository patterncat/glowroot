/**
 * Copyright 2012-2013 the original author or authors.
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
package io.informant.testing.ui;

import java.util.Random;

import checkers.igj.quals.Immutable;

/**
 * @author Trask Stalnaker
 * @since 0.5
 */
@Immutable
public class ExpensiveCall {

    private static final Random random = new Random();

    private final int maxTimeMillis;
    private final int maxSpanTextLength;

    ExpensiveCall(int maxTimeMillis, int maxSpanTextLength) {
        this.maxTimeMillis = maxTimeMillis;
        this.maxSpanTextLength = maxSpanTextLength;
    }

    void execute() {
        int route = random.nextInt(10);
        switch (route) {
            case 0:
                execute0();
                return;
            case 1:
                execute1();
                return;
            case 2:
                execute2();
                return;
            case 3:
                execute3();
                return;
            case 4:
                execute4();
                return;
            case 5:
                execute5();
                return;
            case 6:
                execute6();
                return;
            case 7:
                execute7();
                return;
            case 8:
                execute8();
                return;
            case 9:
                execute9();
                return;
        }
    }

    private void execute0() {
        try {
            Thread.sleep(random.nextInt(maxTimeMillis));
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void execute1() {
        try {
            Thread.sleep(random.nextInt(maxTimeMillis));
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void execute2() {
        try {
            Thread.sleep(random.nextInt(maxTimeMillis));
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void execute3() {
        try {
            Thread.sleep(random.nextInt(maxTimeMillis));
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void execute4() {
        try {
            Thread.sleep(random.nextInt(maxTimeMillis));
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void execute5() {
        try {
            Thread.sleep(random.nextInt(maxTimeMillis));
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void execute6() {
        try {
            Thread.sleep(random.nextInt(maxTimeMillis));
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void execute7() {
        try {
            Thread.sleep(random.nextInt(maxTimeMillis));
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void execute8() {
        try {
            Thread.sleep(random.nextInt(maxTimeMillis));
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void execute9() {
        try {
            Thread.sleep(random.nextInt(maxTimeMillis));
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getSpanText() {
        return getSpanText(random.nextInt(5) > 0);
    }

    private String getSpanText(boolean spaces) {
        int spanTextLength = random.nextInt(maxSpanTextLength);
        StringBuilder sb = new StringBuilder(spanTextLength);
        for (int i = 0; i < spanTextLength; i++) {
            // random lowercase character
            sb.append((char) ('a' + random.nextInt(26)));
            if (spaces && random.nextInt(6) == 0) {
                // on average, one of six characters will be a space
                sb.append(' ');
            }
        }
        return sb.toString();
    }
}
