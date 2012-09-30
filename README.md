# Jenkins DynaSlave plugin
================

## Background
================
In 2011, Netflix began looking at better ways to manage the Jenkins build infrastructure driving our continuous integration and deployment efforts. Part of that involved forklifting our slave build nodes to the cloud so we could easily add (and remove) capacity as needed. We took this on ahead of wide deployment of IAM and VPC within our organization which presented some unique challenges. In searching for an ideal plugin, we came across the Swarm plugin which uses a UDP registration mechanism that esentially allows slaves to announce their presence to Jenkins for registration. Using that as a foundation (sans UDP), we blended in concepts from other plugins and wound up with the DynaSlave in its current form.

## Overview
================
The Jenkins DynaSlave plugin was written to allow Jenkins slave workers to register themselves.

The plugin exposes an endpoint slaves can call with parameters that describe things about them like the labels they support, the number of executors they can handle, and what their name is.

The plugin handles creating the slave object, firing off the launcher to bring up slave.jar on the node, and will handle cleanup of the node should it terminate unexpectedly.

Consider a scenario whereby your slave nodes exist within an Amazon EC2 auto scaling group. Assuming you have a mechanism to inform the nodes what their autoscaling group name is, one can use that bit of information to specially label nodes in that group and tie jobs to that label, effectively creating a specialized slave cluster.

Another possibility is team self-service. Consider a model whereby teams manage their own jobs and are also responsible for their own slave nodes. Rather than delegate permissions via Jenkins, one can give teams a script they will run from their slave at launch time that will kick off the registration process. Can the nodes run a JVM? They can likely become a slave with the DynaSlave plugin with little additional effort.

## Usage
================

### Defaults
================
The plugin exposes five global defaults that will be applied to slaves that don't override those values (or that slaves inherit automatically regardless of what they provide).
* Default Labels - this is a space-separated list of labels that all slaves registering via the plugin will inherit
* Dynaslave Remote User - the user account passed to the launch script for remote access and activation of slave.jar on the remote end. Depending on one's approach, this may be unnecessary.
* Base Launcher Command - The path to the command on the master that's used to trigger the launch of slave.jar on the remote end. Again, depending on approach, this may be a noop.
* Default prefix - A special naming prefix that is prepended to all slaves registering via the plugin.
* Idle Timeout Period - If a slave node loses connectivity or terminates abnormally, this period defines how long it will be allowed to remain in such a state before being cleaned up

The base launcher command will be executed as <command> <hostname of slave> <remote user>.

### URL Parameters
===============
The plugin registers http://jenkins_base_url/plugins/doCreateSlave as an entrypoint. doCreateSlave recognizes the following query parameters:

* name (the name of the dynamic slave, always automatically prepended w/  the global prefix)
* description (optional description of the slave node)
* executors (integer representing the max number of simultaneous builds per slave)
* remoteFsRoot (the base dir where the slave process lives)
* labels (additional labels to apply to the base set of default labels)
* hostname (the addressable hostname (or ip address) of the node)

An example
```
    http://jenkinshost/plugin/dynaslave/createSlave?name=foobar&executors=2&remoteFsRoot=/apps/jenkins&description=foobar&labels=foo%20bar%20baz%20quux&hostname=foobarbaz.com
```

This creates a slave named ds-foobar (ds- is added implicitly and is derived from the global default prefix) with two executors, remoteFsRoot in /apps/jenkins, a simple description "foobar," adds labels (foo, bar, baz, and quux), having hostname foobarbaz.com.  Once polled, Jenkins creates internal structures registering the node and fires the launcher command.

### Caveats
===============
* The plugin does not provide any application-level security around registration. Future enhancements may allow for optional mechanisms for tightening up registration.
* If one deletes the slave from Jenkins, the node lingers externally. You will want to have your slave nodes periodically phone home to ensure they're still registered, and if not, re-register.

### Near Term Enhancements
===============
* Use of the built-in Cloud abstraction to assist with grouping and isolation of nodes (Netflix currently handles this purely through naming conventions and slave labels)
* Use of that same abstraction to help feed external systems information on the build queue pressure and influence slave pool size (Netflix is currently using some system groovy scripts to this effect)
