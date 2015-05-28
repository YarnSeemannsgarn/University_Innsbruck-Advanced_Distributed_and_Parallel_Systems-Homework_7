package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;

/* Copied and adapted from ProfileCredentialsProvider aws-java-sdk/1.9.38/samples/AwsFlowFramework/src/com/amazonaws/services/simpleworkflow/flow/examples/common/ConfigHelper.java */
public class ConfigHelper {
	private Properties config;
	
	private String ec2EndpointUrl;
	private String ec2KeyName;
	private String pemFilePath;
    private String swfServiceUrl;
    private String accessId;
    private String secretKey;
    private String domain;
	
	private ConfigHelper(File propertiesFile) throws IOException {
        loadProperties(propertiesFile);
    }
	
    private void loadProperties(File propertiesFile) throws IOException {
        FileInputStream inputStream = new FileInputStream(propertiesFile);
        config = new Properties();
        config.load(inputStream);

        this.setEc2EndpointUrl(config.getProperty(ConfigConstants.EC2_ENDPOINT_URL_KEY));
        this.setPemFilePath(config.getProperty(ConfigConstants.PEM_FILE_PATH));
        this.setEc2KeyName(config.getProperty(ConfigConstants.EC2_KEY_NAME));
        
        this.setSwfServiceUrl(config.getProperty(ConfigConstants.SWF_SERVICE_URL_KEY));
        this.setAccessId(config.getProperty(ConfigConstants.ACCESS_ID_KEY));
        this.setSecretKey(config.getProperty(ConfigConstants.SECRET_KEY_KEY));
        
        this.setDomain(config.getProperty(ConfigConstants.DOMAIN_KEY));
    }
	
    public static ConfigHelper createConfig() throws IOException, IllegalArgumentException {
    	ConfigHelper configHelper = null;
    	File accessProperties = new File(ConfigConstants.ACCESS_PROPERTIES_RELATIVE_PATH, ConfigConstants.ACCESS_PROPERTIES_FILENAME);
    	configHelper = new ConfigHelper(accessProperties);
    	
    	return configHelper;
    }
    
    public AmazonSimpleWorkflow createSWFClient() {
    	AWSCredentials awsCredentials = new BasicAWSCredentials(this.accessId, this.secretKey);
        AmazonSimpleWorkflow client = new AmazonSimpleWorkflowClient(awsCredentials);
        client.setEndpoint(this.swfServiceUrl);
        return client;
    }
    
    public AmazonEC2Client createEC2Client() {
    	AWSCredentials awsCredentials = new BasicAWSCredentials(this.accessId, this.secretKey);
    	AmazonEC2Client client = new AmazonEC2Client(awsCredentials);
        client.setEndpoint(this.ec2EndpointUrl);
        return client;
    }    

	public String getSwfServiceUrl() {
		return swfServiceUrl;
	}

	public void setSwfServiceUrl(String swfServiceUrl) {
		this.swfServiceUrl = swfServiceUrl;
	}

	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	public String getsecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getEc2EndpointUrl() {
		return ec2EndpointUrl;
	}

	public void setEc2EndpointUrl(String ec2EndpointUrl) {
		this.ec2EndpointUrl = ec2EndpointUrl;
	}

	public String getPemFilePath() {
		return pemFilePath;
	}

	public void setPemFilePath(String pemFilePath) {
		this.pemFilePath = pemFilePath;
	}

	public String getEc2KeyName() {
		return ec2KeyName;
	}

	public void setEc2KeyName(String ec2KeyName) {
		this.ec2KeyName = ec2KeyName;
	}
}
