package piEstimater.PiEstimater;

import java.net.InetAddress;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import common.ConfigHelper;

public class PiEstimaterMain {
	public static void main(String[] args) throws Exception {
		ConfigHelper configHelper = ConfigHelper.createConfig();
		AmazonSimpleWorkflow service = configHelper.createSWFClient();
		String domain = configHelper.getDomain();

		PiEstimaterWorkflowClientExternalFactory factory = new PiEstimaterWorkflowClientExternalFactoryImpl(service, domain);
		PiEstimaterWorkflowClientExternal piEstimater = factory.getClient(InetAddress.getLocalHost().getHostName());
		piEstimater.estimatePi(10);
	}
}