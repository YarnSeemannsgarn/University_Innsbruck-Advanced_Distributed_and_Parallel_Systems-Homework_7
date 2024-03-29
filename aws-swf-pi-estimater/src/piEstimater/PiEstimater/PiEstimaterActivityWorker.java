package piEstimater.PiEstimater;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;
import common.ConfigHelper;

public class PiEstimaterActivityWorker {
	public static void main(String[] args) throws Exception {
		ConfigHelper configHelper = ConfigHelper.createConfig();
		AmazonSimpleWorkflow service = configHelper.createSWFClient();
		String domain = configHelper.getDomain();

		String taskListToPoll = domain + "List";

		ActivityWorker aw = new ActivityWorker(service, domain, taskListToPoll);
		aw.addActivitiesImplementation(new PiEstimaterActivitiesImpl());
		aw.start();
	}
}
