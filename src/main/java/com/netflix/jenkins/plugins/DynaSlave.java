package com.netflix.jenkins.plugins;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor.FormException;
import hudson.model.Slave;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * {@link Slave}  created by ad-hoc local systems.
 * Initial concept heavily borrowed from the Swarm plugin
 */
public class DynaSlave extends Slave {
    private static final Logger LOG = Logger.getLogger(DynaSlave.class.getName());

    public DynaSlave(String name, String nodeDescription, String remoteFS, String numExecutors, String labels,
                     String hostname, String defaultRemoteUser, String defaultBaseLauncherCommand,
                     String idleTerminationMinutes)
            throws IOException, FormException {
        super(name, nodeDescription, remoteFS, numExecutors, Mode.NORMAL, labels,
                new DynaSlaveCommandLauncher(defaultBaseLauncherCommand + " " + hostname + " " + defaultRemoteUser),
                new DynaSlaveRetentionStrategy(idleTerminationMinutes), Collections.<NodeProperty<?>>emptyList());
    }

    @DataBoundConstructor
    public DynaSlave(String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode,
                     String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy,
                     List<? extends NodeProperty<?>> nodeProperties)
            throws FormException, IOException {

        super(name, nodeDescription, remoteFS, Util.tryParseNumber(numExecutors, 1).intValue(), mode, labelString,
                launcher, retentionStrategy, nodeProperties);
    }

    @Extension
    public static final class DescriptorImpl extends SlaveDescriptor {
        public String getDisplayName() {
            return "Dynamic Slave";
        }
    }
}
