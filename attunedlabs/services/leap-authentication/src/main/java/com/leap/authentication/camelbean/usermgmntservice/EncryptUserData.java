package com.leap.authentication.camelbean.usermgmntservice;

import java.util.Base64;

import javax.xml.bind.DatatypeConverter;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.attunedlabs.security.TenantSecurityConstant;

/**
 * This Class is used to encrypt the request JSON data which contains the
 * userName , domain , password values
 * 
 * @author reactiveworks
 *
 */
public class EncryptUserData {

	protected Logger logger = LoggerFactory.getLogger(EncryptUserData.class.getName());
	static final String CONTENT_TYPE = "Content-type";
	static final String APP_JSON = "application/json";

	public void encryptUserData(Exchange exchange) throws Exception {
		logger.debug("inside the encryptUserData method of EncryptUserData Class");
		Message messageBody = exchange.getIn();
//		String requestBody = messageBody.getBody(String.class);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String requestBody = leapDataContext.getInitialRequestData().getData().toString();
		JSONObject requestJsonObj = new JSONObject(requestBody);
		byte[] jsonRequestData = requestJsonObj.toString().getBytes();
		String jsonRequestbase64Encoded = DatatypeConverter.printBase64Binary(jsonRequestData);
		exchange.getIn().setHeader(CONTENT_TYPE, APP_JSON);
		exchange.getIn().setBody(new JSONObject().put("encryptedData", jsonRequestbase64Encoded).put("success", true));
		messageBody.removeHeader(TenantSecurityConstant.TENANT_TOKEN_LOGIN);
		messageBody.removeHeader(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME_LOGIN);

	}// ..end of method process

	public void createAutoLoginTxtFile(Exchange exchange) throws Exception {
		logger.debug("inside the createAutoLoginTxtFile method of EncryptUserData Class");
		JSONObject requestJsonObj;
		JSONObject encriptedJson;
		try {
			requestJsonObj = getInputJsonFromPostRequest(exchange);
			if (!requestJsonObj.has("credentials")) {
				throw new Exception("Unable to get the value for credentials from the input json");
			}
			exchange.getIn().setBody(requestJsonObj.getJSONObject("credentials"));
			encryptUserData(exchange);
			encriptedJson = getInputJsonFromPostRequest(exchange);
			if (!encriptedJson.has("encryptedData")) {
				throw new Exception("Unable to get the value for encryptedData after encription");
			}
			requestJsonObj.put("credentials", encriptedJson.getString("encryptedData"));
			//tempFile = File.createTempFile("login", ".txt");
			
			//File file=new File("C:/Users/bizruntime44/Downloads/AutoLogin/login.txt");

			/*try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
				bw.write(Base64.getEncoder().encodeToString(requestJsonObj.toString().getBytes()));
			}*/
			
			
			/*try (OutputStream outputStream =new FileOutputStream(tempFile)) {
			    IOUtils.write(Base64.getEncoder().encodeToString(requestJsonObj.toString().getBytes()), outputStream, "UTF-8");
			*/
			
			/*OutputStream fout=new FileOutputStream(tempFile);    
			fout.write(Base64.getEncoder().encodeToString(requestJsonObj.toString().getBytes()).getBytes());
			fout.flush();*/
			
			logger.debug("Base64 encoded temp file created");
			
			//exchange.getIn().setBody(tempFile);
			
			// exchange.getIn().setHeader("Content-Disposition", "inline;filename*=UTF-8''9b62405c463e6be04e6578ae129a6270.txt"); 
			// exchange.getIn().removeHeader("Content-Type");
		       // exchange.getIn().setHeader("Content-Type", MediaType.OCTET_STREAM); 
		        //exchange.getIn().setHeader("Content-Encoding", "gzip"); 
		        //exchange.getIn().setHeader("testHeader", "hi"); 
			
		       // fout.close();
			
			
			
			/*
			 String base64File = "";
		        File file = new File("C:/Users/bizruntime44/Downloads/AutoLogin/login.txt");
		        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
			exchange.getIn().setBody(new String(encoded, StandardCharsets.US_ASCII));
			exchange.getIn().setHeader("Content-Type", MediaType.OCTET_STREAM); */
		        
		        exchange.getIn().setBody(Base64.getEncoder().encodeToString(requestJsonObj.toString().getBytes()));
			logger.debug("exchange header :==>"+exchange.getIn().getHeaders());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public JSONObject getInputJsonFromPostRequest(Exchange exchange) throws Exception {
		logger.debug(".getInputJsonFromPostRequest mtd of AutomationEmulationUtil");
		logger.debug("Input exchange body :" + exchange.getIn().getBody());
		if (exchange.getIn().getBody() != null && !exchange.getIn().getBody(String.class).equals("")) {
			String inputStr = exchange.getIn().getBody(String.class).trim();
			try {
				return new JSONObject(inputStr);
			} catch (JSONException e) {
				throw new Exception("Invalid input request, Unable to convert string to json object");
			}
		} else {
			throw new Exception("Invalid input request, Input request is null or empty");
		}
	}

}// ..end of class EncryptUserData
