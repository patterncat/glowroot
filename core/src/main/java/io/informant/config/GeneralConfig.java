/**
 * Copyright 2011-2013 the original author or authors.
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
package io.informant.config;

import checkers.igj.quals.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.Objects;
import com.google.common.hash.Hashing;

/**
 * Immutable structure to hold the general config.
 * 
 * Default values should be conservative.
 * 
 * @author Trask Stalnaker
 * @since 0.5
 */
@Immutable
@JsonDeserialize(builder = GeneralConfig.Builder.class)
public class GeneralConfig {

    // don't store anything, essentially store threshold is infinite
    public static final int STORE_THRESHOLD_DISABLED = -1;
    // don't expire anything, essentially snapshot expiration is infinite
    public static final int SNAPSHOT_EXPIRATION_DISABLED = -1;

    // if tracing is disabled mid-trace there should be no issue
    // active traces will not accumulate additional spans
    // but they will be logged / emailed if they exceed the defined thresholds
    //
    // if tracing is enabled mid-trace there should be no issue
    // active traces that were not captured at their start will
    // continue not to accumulate spans
    // and they will not be logged / emailed even if they exceed the defined
    // thresholds
    private final boolean enabled;

    // 0 means log all traces, -1 means log no traces
    // (though stuck threshold can still be used in this case)
    private final int storeThresholdMillis;

    // minimum is imposed because of StuckTraceCollector#CHECK_INTERVAL_MILLIS
    // -1 means no stuck messages are gathered, should be minimum 100 milliseconds
    private final int stuckThresholdSeconds;

    // used to limit memory requirement, also used to help limit trace capture size,
    // 0 means don't capture any spans, -1 means no limit
    private final int maxSpans;

    private final int snapshotExpirationHours;

    // size of fixed-length rolling database for storing trace details (spans and merged stack
    // traces)
    private final int rollingSizeMb;

    private final boolean warnOnSpanOutsideTrace;

    private final String version;

    static GeneralConfig getDefault() {
        return new Builder().build();
    }

    public static Builder builder(GeneralConfig base) {
        return new Builder(base);
    }

    private GeneralConfig(boolean enabled, int storeThresholdMillis, int stuckThresholdSeconds,
            int maxSpans, int snapshotExpirationHours, int rollingSizeMb,
            boolean warnOnSpanOutsideTrace, String version) {
        this.enabled = enabled;
        this.storeThresholdMillis = storeThresholdMillis;
        this.stuckThresholdSeconds = stuckThresholdSeconds;
        this.maxSpans = maxSpans;
        this.snapshotExpirationHours = snapshotExpirationHours;
        this.rollingSizeMb = rollingSizeMb;
        this.warnOnSpanOutsideTrace = warnOnSpanOutsideTrace;
        this.version = version;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getStoreThresholdMillis() {
        return storeThresholdMillis;
    }

    public int getStuckThresholdSeconds() {
        return stuckThresholdSeconds;
    }

    public int getMaxSpans() {
        return maxSpans;
    }

    public int getSnapshotExpirationHours() {
        return snapshotExpirationHours;
    }

    public int getRollingSizeMb() {
        return rollingSizeMb;
    }

    public boolean isWarnOnSpanOutsideTrace() {
        return warnOnSpanOutsideTrace;
    }

    @JsonView(WithVersionJsonView.class)
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("enabed", enabled)
                .add("storeThresholdMillis", storeThresholdMillis)
                .add("stuckThresholdSeconds", stuckThresholdSeconds)
                .add("maxSpans", maxSpans)
                .add("snapshotExpirationHours", snapshotExpirationHours)
                .add("rollingSizeMb", rollingSizeMb)
                .add("warnOnSpanOutsideTrace", warnOnSpanOutsideTrace)
                .add("version", version)
                .toString();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private boolean enabled = true;
        private int storeThresholdMillis = 3000;
        private int stuckThresholdSeconds = 180;
        private int maxSpans = 5000;
        private int snapshotExpirationHours = 24 * 7;
        private int rollingSizeMb = 1000;
        private boolean warnOnSpanOutsideTrace = false;

        private Builder() {}

        private Builder(GeneralConfig base) {
            enabled = base.enabled;
            storeThresholdMillis = base.storeThresholdMillis;
            stuckThresholdSeconds = base.stuckThresholdSeconds;
            maxSpans = base.maxSpans;
            snapshotExpirationHours = base.snapshotExpirationHours;
            rollingSizeMb = base.rollingSizeMb;
            warnOnSpanOutsideTrace = base.warnOnSpanOutsideTrace;
        }

        // JsonProperty annotations are needed in order to use ObjectMapper.readerForUpdating()
        // for overlaying values on top of a base config
        @JsonProperty
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        @JsonProperty
        public Builder storeThresholdMillis(int storeThresholdMillis) {
            this.storeThresholdMillis = storeThresholdMillis;
            return this;
        }

        @JsonProperty
        public Builder stuckThresholdSeconds(int stuckThresholdSeconds) {
            this.stuckThresholdSeconds = stuckThresholdSeconds;
            return this;
        }

        @JsonProperty
        public Builder maxSpans(int maxSpans) {
            this.maxSpans = maxSpans;
            return this;
        }

        @JsonProperty
        public Builder snapshotExpirationHours(int snapshotExpirationHours) {
            this.snapshotExpirationHours = snapshotExpirationHours;
            return this;
        }

        @JsonProperty
        public Builder rollingSizeMb(int rollingSizeMb) {
            this.rollingSizeMb = rollingSizeMb;
            return this;
        }

        @JsonProperty
        public Builder warnOnSpanOutsideTrace(boolean warnOnSpanOutsideTrace) {
            this.warnOnSpanOutsideTrace = warnOnSpanOutsideTrace;
            return this;
        }

        public GeneralConfig build() {
            String version = buildVersion();
            return new GeneralConfig(enabled, storeThresholdMillis, stuckThresholdSeconds,
                    maxSpans, snapshotExpirationHours, rollingSizeMb, warnOnSpanOutsideTrace,
                    version);
        }

        private String buildVersion() {
            return Hashing.sha1().newHasher()
                    .putBoolean(enabled)
                    .putInt(storeThresholdMillis)
                    .putInt(stuckThresholdSeconds)
                    .putInt(maxSpans)
                    .putInt(snapshotExpirationHours)
                    .putInt(rollingSizeMb)
                    .putBoolean(warnOnSpanOutsideTrace)
                    .hash().toString();
        }
    }
}