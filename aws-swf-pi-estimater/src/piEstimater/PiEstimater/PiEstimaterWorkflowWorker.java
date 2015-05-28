package piEstimater.PiEstimater;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;
import common.ConfigHelper;

public class PiEstimaterWorkflowWorker {
	public static void main(String[] args) throws Exception {
		ConfigHelper configHelper = ConfigHelper.createConfig();
		AmazonSimpleWorkflow service = configHelper.createSWFClient();
		String domain = configHelper.getDomain();

		String taskListToPoll = domain + "List";

		WorkflowWorker wfw = new WorkflowWorker(service, domain, taskListToPoll);
		wfw.addWorkflowImplementationType(PiEstimaterWorkflowImpl.class);
		wfw.start();
	}
}
