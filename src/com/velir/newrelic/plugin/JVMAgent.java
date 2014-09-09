package com.velir.newrelic.plugin;

import java.io.BufferedReader;
import java.io.FileInputStream;
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
	public static final String JMAP = "/usr/java/default/bin/jmap";

	private final String name;
	private final Runtime runtime;
	private final String pidfile;
	private final String jmap;
	private String pid;

	public JVMAgent(String name, Runtime runtime, String pidfile, String jmap) {
		super(GUID, VERSION);
		this.name = name;
		this.runtime = runtime;
		this.pidfile = pidfile;
		this.jmap = (jmap == null || jmap.matches("\\s*")) ? JMAP : jmap;
	}

	@Override
	public void pollCycle() {
		boolean acquiredHeapUsage = false;
		try {
			if (pid == null) {
				pid = getPID(pidfile);
				LOG.info("Will attach to java process " + pid);
			}
			String[] processArgs = { jmap, "-heap", pid};
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
						acquiredHeapUsage = true;
					}
				}

			}
			bufferedReader.close();
			process.destroy();

		} catch (IOException e) {
			LOG.error("could not execute jmap", e);
		}
		if (! acquiredHeapUsage) {
			// maybe the pid has changed because the server has been restarted? reset pid here
			pid = null;
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

	private String getPID (String pidfile) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(pidfile);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
		return bufferedReader.readLine();

	}
}
