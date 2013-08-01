package com.netflix.jenkins.plugins;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.slaves.RetentionStrategy;
import hudson.slaves.SlaveComputer;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static hudson.util.TimeUnit2.MINUTES;
import static java.util.logging.Level.WARNING;

/**
 * {@link RetentionStrategy} implementation wholesale based off of {@link hudson.slaves.CloudRetentionStrategy}
 * Terminates a node if it's both offline and has been so for at least idleMinutes
 * Only applies to {@link DynaSlave} nodes
 */
public class DynaSlaveRetentionStrategy extends RetentionStrategy<SlaveComputer> {
    private static final Logger LOGGER = Logger.getLogger(DynaSlaveRetentionStrategy.class.getName());

    private AtomicInteger idleMinutes = new AtomicInteger(30);
    public AtomicBoolean disabled = new AtomicBoolean(Boolean.getBoolean(DynaSlaveRetentionStrategy.class.getName() + ".disabled"));

    @DataBoundConstructor
    public DynaSlaveRetentionStrategy(String strIdleMinutes) {
        this.idleMinutes.set(Util.tryParseNumber(strIdleMinutes, 30).intValue());
    }

    public long check(final SlaveComputer c) {
        if (c.isOffline() && !disabled.get()) {
            final long idleMilliseconds = System.currentTimeMillis() - c.getIdleStartMilliseconds();
            final long defaultIdleMinutes = (this.idleMinutes != null ? this.idleMinutes.get() : 30);
            if (idleMilliseconds > MINUTES.toMillis(defaultIdleMinutes)) {
                LOGGER.info("Disconnecting dynaslave " + c.getName());
                try {
                    Hudson.getInstance().removeNode(c.getNode());
                } catch (IOException e) {
                    LOGGER.log(WARNING, "Failed to terminate " + c.getName(), e);
                }
            }
        }
        return 1;
    }

    @Override
    public void start(SlaveComputer c) {
        c.connect(false);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<RetentionStrategy<?>> {
        public String getDisplayName() {
            return "DynaSlave Retention Strategy";
        }
    }
}
