package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;

/* Copied and adapted from ProfileCredentialsProvider aws-java-sdk/1.9.38/samples/AwsFlowFramework/src/com/amazonaws/services/simpleworkflow/flow/examples/common/ConfigHelper.java */
public class ConfigHelper {
	private Properties sampleConfig;
	
    private String swfServiceUrl;
    private String swfAccessId;
    private String swfSecretKey;
    private String domain;
	
	private ConfigHelper(File propertiesFile) throws IOException {
        loadProperties(propertiesFile);
    }
	
    private void loadProperties(File propertiesFile) throws IOException {
        FileInputStream inputStream = new FileInputStream(propertiesFile);
        sampleConfig = new Properties();
        sampleConfig.load(inputStream);

        this.setSwfServiceUrl(sampleConfig.getProperty(ConfigConstants.SWF_SERVICE_URL_KEY));
        this.setSwfAccessId(sampleConfig.getProperty(ConfigConstants.SWF_ACCESS_ID_KEY));
        this.setSwfSecretKey(sampleConfig.getProperty(ConfigConstants.SWF_SECRET_KEY_KEY));
        
        this.setDomain(sampleConfig.getProperty(ConfigConstants.DOMAIN_KEY));
    }
	
    public static ConfigHelper createConfig() throws IOException, IllegalArgumentException {
    	ConfigHelper configHelper = null;
    	File accessProperties = new File(ConfigConstants.ACCESS_PROPERTIES_RELATIVE_PATH, ConfigConstants.ACCESS_PROPERTIES_FILENAME);
    	configHelper = new ConfigHelper(accessProperties);
    	
    	return configHelper;
    }
    
    public AmazonSimpleWorkflow createSWFClient() {
    	AWSCredentials awsCredentials = new BasicAWSCredentials(this.swfAccessId, this.swfSecretKey);
        AmazonSimpleWorkflow client = new AmazonSimpleWorkflowClient(awsCredentials);
        client.setEndpoint(this.swfServiceUrl);
        return client;
    }

	public String getSwfServiceUrl() {
		return swfServiceUrl;
	}

	public void setSwfServiceUrl(String swfServiceUrl) {
		this.swfServiceUrl = swfServiceUrl;
	}

	public String getSwfAccessId() {
		return swfAccessId;
	}

	public void setSwfAccessId(String swfAccessId) {
		this.swfAccessId = swfAccessId;
	}

	public String getSwfSecretKey() {
		return swfSecretKey;
	}

	public void setSwfSecretKey(String swfSecretKey) {
		this.swfSecretKey = swfSecretKey;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
