package com.velir.newrelic.plugin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

public class JVMAgentFactory extends AgentFactory {
	private static final String JMAP = "/usr/java/default/bin/jmap";

	private final Runtime runtime;

	public JVMAgentFactory() {
		super();
		runtime = Runtime.getRuntime();
	}

	@Override
	public Agent createConfiguredAgent(final Map<String, Object> properties) throws ConfigurationException {
		String name = (String) properties.get("name");
		String pidfile = (String) properties.get("pidfile");

		if (name == null || pidfile == null) {
			throw new ConfigurationException("'name' and 'pidfile' cannot be null. Do you have a 'config/plugin.json' file?");
		}

		try {
			String pid = getPID(pidfile);
			String[] args = { JMAP, "-heap", pid};
			return new JVMAgent(name, runtime, args);
		} catch (IOException e) {
			throw new ConfigurationException("could not read pid for java process");
		}

	}

	private String getPID (String pidfile) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(pidfile);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
		return bufferedReader.readLine();

	}

}
