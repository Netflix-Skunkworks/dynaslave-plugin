package com.netflix.jenkins.plugins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.slaves.CommandLauncher;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.Messages;
import hudson.slaves.SlaveComputer;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link hudson.slaves.ComputerLauncher} that destroys itself upon a connection termination.
 */
public class DynaSlaveCommandLauncher extends CommandLauncher {
    private static final Logger LOG = Logger.getLogger(DynaSlaveCommandLauncher.class.getName());

    @DataBoundConstructor
    public DynaSlaveCommandLauncher(String command) {
        super(command);
    }

    public DynaSlaveCommandLauncher(String command, EnvVars env) {
        super(command, env);
    }

    @Override
    public void afterDisconnect(SlaveComputer computer, TaskListener listener) {
        if (!Jenkins.getInstance().isTerminating()) {
            try {
                Jenkins.getInstance().removeNode(computer.getNode());
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Error removing slave", e);
            }
        }
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<ComputerLauncher> {
        public String getDisplayName() {
            return "Command launcher that deletes the node when JNLP disconnects";
        }

        public FormValidation doCheckCommand(@QueryParameter String value) {
            return (Util.fixEmptyAndTrim(value) == null) ?
                    FormValidation.error(Messages.CommandLauncher_NoLaunchCommand()) : FormValidation.ok();
        }
    }
}
