package com.softicar.gradle.test.logger.plugin.test.run;

import com.softicar.gradle.test.logger.plugin.test.method.TestMethodName;
import java.util.Map;
import java.util.Map.Entry;
import org.gradle.api.logging.Logger;

public class RedundantTestRunLogger {

	private final Logger logger;

	public RedundantTestRunLogger(Logger logger) {

		this.logger = logger;
	}

	public void log(TestRunTracker testRunTracker) {

		Map<TestMethodName, Integer> redundantTestRunMap = testRunTracker.getRedundantTestRunMap();
		if (!redundantTestRunMap.isEmpty()) {
			logger.lifecycle(String.format("WARNING: %s test(s) were executed several times:", redundantTestRunMap.size()));
			logger.lifecycle("");

			int counter = 0;
			for (Entry<TestMethodName, Integer> entry: redundantTestRunMap.entrySet()) {
				++counter;
				logger.lifecycle(String.format("Redundant Test #%s >  %s x  %s", counter, entry.getValue(), entry.getKey().getNameString()));
			}

			logger.lifecycle("");
		}
	}
}
