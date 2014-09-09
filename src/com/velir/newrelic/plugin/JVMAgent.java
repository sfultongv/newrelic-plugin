package com.velir.newrelic.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.util.Logger;

public class JVMAgent extends Agent {
	private static final Logger LOG = Logger.getLogger(JVMAgent.class);

	public static final String GUID = "com.velir.newrelic.plugin.JVMAgent";
	public static final String VERSION = "0.1.0";
	public static final Pattern USED_PATTERN = Pattern.compile("\\s*used\\s*=\\s*(\\d+).*");

	private final String name;
	private final Runtime runtime;
	private final String[] processArgs;

	public JVMAgent(String name, Runtime runtime, String[] processArgs) {
		super(GUID, VERSION);
		this.name = name;
		this.runtime = runtime;
		this.processArgs = processArgs;
	}

	@Override
	public void pollCycle() {
		try {
			Process process = runtime.exec(processArgs);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String input;
			LOG.debug("starting to read from jmap");
			while ((input = bufferedReader.readLine()) != null) {
				LOG.debug(input);
				if (input.startsWith("Heap Usage:")) {
					LOG.debug("----- Getting heap data -----");
					Long heapUsage = getHeapUsage(bufferedReader);
					if (heapUsage != null) {
						reportMetric("JVM/Allocated Heap", "bytes", heapUsage);
					}
				}

			}
			bufferedReader.close();
			process.destroy();

		} catch (IOException e) {
			LOG.error("could not execute jmap", e);
		}
	}

	public Long getHeapUsage (BufferedReader bufferedReader) throws IOException {
		String input;
		while ((input = bufferedReader.readLine()) != null) {
			LOG.debug(input);
			Matcher matcher = USED_PATTERN.matcher(input);
			if (matcher.matches()) {
				LOG.debug("----- Found used heap -----");
				return Long.parseLong(matcher.group(1));
			}
		}
		return null;
	}

	@Override
	public String getAgentName() {
		return name;
	}
}
