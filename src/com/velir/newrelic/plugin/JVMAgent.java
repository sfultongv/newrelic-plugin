package com.velir.newrelic.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.newrelic.metrics.publish.Agent;

public class JVMAgent extends Agent {

	public static final String GUID = "com.velir.newrelic.plugin.JVMAgent";
	public static final String VERSION = "0.1.0";
	public static final Pattern USED_PATTERN = Pattern.compile("\\s*used\\s*=\\s*(d+).*");

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
			while ((input = bufferedReader.readLine()) != null) {
				if (input.startsWith("Heap Usage:")) {
					Integer heapUsage = getHeapUsage(bufferedReader);
					if (heapUsage != null) {
						reportMetric("JVM/Allocated Heap", "bytes", heapUsage);
					}
				}

			}
			bufferedReader.close();
			process.destroy();

		} catch (IOException e) {
			//do something?
		}
	}

	public Integer getHeapUsage (BufferedReader bufferedReader) throws IOException {
		String input;
		while ((input = bufferedReader.readLine()) != null) {
			Matcher matcher = USED_PATTERN.matcher(input);
			if (matcher.matches()) {
				return Integer.parseInt(matcher.group(1));
			}
		}
		return null;
	}

	@Override
	public String getAgentName() {
		return name;
	}
}
