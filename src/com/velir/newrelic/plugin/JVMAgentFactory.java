package com.velir.newrelic.plugin;

import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.util.Logger;

public class JVMAgentFactory extends AgentFactory {
	private static final Logger LOG = Logger.getLogger(JVMAgentFactory.class);

	private final Runtime runtime;

	public JVMAgentFactory() {
		super();
		runtime = Runtime.getRuntime();
	}

	@Override
	public Agent createConfiguredAgent(final Map<String, Object> properties) throws ConfigurationException {
		String name = (String) properties.get("name");
		String pidfile = (String) properties.get("pidfile");
		String jmap = (String) properties.get("jmap");

		if (name == null || pidfile == null) {
			throw new ConfigurationException("'name' and 'pidfile' cannot be null. Do you have a 'config/plugin.json' file?");
		}

		return new JVMAgent(name, runtime, pidfile, jmap);
	}
}
