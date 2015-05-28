package piEstimater.PiEstimater;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import common.ConfigHelper;


public class PiEstimaterMain {
	public static final int PARAMETERS = 1;
	public static final String INVALID_SYNTAX = "Invalid number of parameters. Arguments are: instances";
	public static final int ACTIVITIES_PER_WORKER = 10;

	// EC2 stuff
	public static final String IMAGE_ID = "ami-d114f295";
	public static final String INSTANCE_TYPE = "t2.small";
	public static final String SECURITY_GROUP = "PiEsitmaterSecurityGroup";
	public static final String REMOTE_USER = "ec2-user";

	// Local dirs and files
	private static final Path CWD = Paths.get(System.getProperty("user.dir"));
	private static final String ACTIVITY_WORKER_JAR = "PiEstimaterActivityWorker.jar";
	private static final String ACCESS_PROPERTIES = "access.properties";
	private static final Path ACTIVITY_WORKER_PATH = CWD.resolve(ACTIVITY_WORKER_JAR);
	private static final Path ACCESS_PROPERTIES_PATH = CWD.resolve(ACCESS_PROPERTIES);

	public static void main(String[] args) {
		// Check parameters
		if (args.length < PARAMETERS)
			throw new IllegalArgumentException(INVALID_SYNTAX);
		int instancesNum = Integer.parseInt(args[0]);

		// Create EC2 Client
		ConfigHelper configHelper = null;
		try {
			configHelper = ConfigHelper.createConfig();
		} catch(Exception e) {
			e.printStackTrace();
		}

		// Create EC2 instances (following http://docs.aws.amazon.com/AWSSdkDocsJava/latest//DeveloperGuide/how-to-ec2.html)
		System.out.println("Launching " + instancesNum + " EC2 instances");
		AmazonEC2Client amazonEC2Client = configHelper.createEC2Client();

		CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();
		csgr.withGroupName(SECURITY_GROUP).withDescription("Security Group for PiEstimater");
		try {
			amazonEC2Client.createSecurityGroup(csgr);
		} catch (AmazonServiceException e) {
			// Security group already exists
		}

		IpPermission ipPermission = new IpPermission();
		ipPermission.withIpProtocol("tcp").withFromPort(22).withToPort(22).withIpRanges("0.0.0.0/0"); // enable ssh

		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
				new AuthorizeSecurityGroupIngressRequest();
		authorizeSecurityGroupIngressRequest.withGroupName(SECURITY_GROUP).withIpPermissions(ipPermission);

		try {
			amazonEC2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
		} catch (AmazonServiceException e) {
			// Key pair already exists
		}

		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
		runInstancesRequest.withImageId(IMAGE_ID)
		.withInstanceType(INSTANCE_TYPE)
		.withMinCount(instancesNum)
		.withMaxCount(instancesNum)
		.withKeyName(configHelper.getEc2KeyName())
		.withSecurityGroups(SECURITY_GROUP);

		RunInstancesResult instancesResult = amazonEC2Client.runInstances(runInstancesRequest);
		System.out.println("Launched " + instancesNum + " EC2 instances");

		System.out.println("Waiting for EC2 instances to be ready");
		DescribeInstancesResult result = waitForInstances(instancesResult.getReservation().getInstances(), amazonEC2Client);
		System.out.println("All EC2 instances are ready");

		// Transfer jar files to ec2 instances and start workers
		JSch.setConfig("StrictHostKeyChecking", "no");
		JSch jsch = new JSch();

		// TODO: Parallize
		try {
			jsch.addIdentity(configHelper.getPemFilePath());

			for (Reservation reservation : result.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					Session session;

					session = jsch.getSession(REMOTE_USER, instance.getPublicIpAddress(), 22);
					System.out.println("Try ssh until it works");
					while(true){
						try{
							// Connect until ssh works
							session.connect();
						} catch (JSchException e) {
							wait10Secs();
							continue;
						}
						System.out.println("");
						break;
					}

					String scpCommand = "scp -oStrictHostKeyChecking=no -i " + configHelper.getPemFilePath() + " " + ACTIVITY_WORKER_PATH +
							" " + ACCESS_PROPERTIES_PATH + " " + REMOTE_USER + "@" + instance.getPublicIpAddress() + ":~/";
					String javaCommand = "java -jar " + ACTIVITY_WORKER_JAR;
					
					String sshCommand = "ssh -oStrictHostKeyChecking=no -i " + configHelper.getPemFilePath() + " " + REMOTE_USER + "@" + 
							instance.getPublicIpAddress() + " \"" + javaCommand + "\"";
					

					System.out.println("Copy files to " + instance.getInstanceId());
					Process scpProcess = Runtime.getRuntime().exec(scpCommand);
					scpProcess.waitFor();

					System.out.println("Run Active worker on " + instance.getInstanceId());
					Process sshProcess = Runtime.getRuntime().exec(sshCommand);
					sshProcess.waitFor();
					
					session.disconnect();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create SWFclient
		AmazonSimpleWorkflow service = configHelper.createSWFClient();
		String domain = configHelper.getDomain();

		// Create SWFclient
		PiEstimaterWorkflowClientExternalFactory factory = new PiEstimaterWorkflowClientExternalFactoryImpl(service, domain);
		PiEstimaterWorkflowClientExternal piEstimater = null;
		try {
			piEstimater = factory.getClient(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		System.out.println("Start workflow");
		piEstimater.estimatePi(instancesNum * ACTIVITIES_PER_WORKER);
		
		//TODO: Delete instances
	}

	private static DescribeInstancesResult waitForInstances(List<Instance> instances, AmazonEC2Client amazonEC2Client){		
		List<String> instanceIds = new ArrayList<String>();
		for (Instance instance : instances) {
			instanceIds.add(instance.getInstanceId());
		}

		while(true) {
			DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceIds);
			DescribeInstancesResult result = amazonEC2Client.describeInstances(request);

			boolean allReady = true;
			for (Reservation reservation : result.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					if(instance.getPublicIpAddress() == null || !instance.getState().getName().equals("running")){
						allReady = false;
						break;
					}
				}
			}

			if(allReady) {
				System.out.println("");
				return result;
			} else {
				wait10Secs();
			}
		}
	}

	private static void wait10Secs() {
		try {
			Thread.sleep(1000 * 10);
			System.out.print(".");
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}
