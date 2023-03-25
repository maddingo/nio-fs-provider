package no.maddin.niofs.webdav;

import java.util.logging.Logger;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

public class LoggingListener implements TestExecutionListener {

	Logger log = Logger.getLogger(LoggingListener.class.getName());
	
	public LoggingListener() {
		
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		log.fine(String.format("%s %s", testIdentifier, entry));
	}

	
	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		log.fine(String.format("TestPlan Execution Started: %s", testPlan));
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		log.fine(String.format("TestPlan Execution Finished: %s", testPlan));
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		log.fine(String.format(
			"Dynamic Test Registered: %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId()));
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		log.fine(String.format("Execution Started: %s - %s", 
			testIdentifier.getDisplayName(), testIdentifier.getUniqueId()));
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		log.fine(String.format("Execution Skipped: %s - %s - %s",
			testIdentifier.getDisplayName(), testIdentifier.getUniqueId(), reason));
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		log.fine(String.format("Execution Finished: %s - %s - %s", testExecutionResult.getThrowable().orElse(null),
			testIdentifier.getDisplayName(), testIdentifier.getUniqueId(), testExecutionResult));
	}

	
}
