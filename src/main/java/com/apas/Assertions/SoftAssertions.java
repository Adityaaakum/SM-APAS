package com.apas.Assertions;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.apas.Assertions.HardAssertions;
import com.apas.JiraStatusUpdate.JiraAdaptavistStatusUpdate;

public class SoftAssertions extends HardAssertions {
	private Map<String, String> failedAssertionsMap = null;
	HardAssertions _HardAssertions = null;

	public SoftAssertions() {
		_HardAssertions = new HardAssertions(true);
	}

	public void assertAll() {
		StringBuilder failedAssertions = new StringBuilder("The following assertions have failed::");
		failedAssertionsMap = _HardAssertions.getAssertionMap();
		Set<String> keySet = failedAssertionsMap.keySet();
		Iterator<String> itr = keySet.iterator();
		while (itr.hasNext()) {
			String currentKey = itr.next();
			String currentMessage = failedAssertionsMap.get(currentKey);
			failedAssertions.append("\n\t");
			failedAssertions.append(currentMessage);
		}
		new HardAssertions(false);
		if (JiraAdaptavistStatusUpdate.testStatus.values().contains("Fail")) {
			throw new AssertionError(failedAssertions.toString());
		}
	}
}
