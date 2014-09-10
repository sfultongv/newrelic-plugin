newrelic-plugin
===============

Setup is very similar to the java wikipedia plugin.

There are two options to be aware of, when configuring agents:
  1. pidfile - a path to a file containing the process id for java is necessary
  2. jmap - the path to the jmap executable can be specified (otherwise defaults to /usr/java/default/bin/jmap)

This isn't a full-featured plugin, it only shows heap usage in bytes, and is intended to be used as a reference for developing a more feature-complete plugin.
