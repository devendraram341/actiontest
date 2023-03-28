package com.attunedlabs.leapentity;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.CONTENT_TYPE;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.HTTPS;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.JKS;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.STATIC_SOURCE;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.TLS;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leapentity.config.jaxb.AuthenticationConfig;
import com.attunedlabs.leapentity.config.jaxb.EntityRestRequestHeader;
import com.attunedlabs.leapentity.config.jaxb.SSLConfig;
import com.attunedlabs.leapentity.service.LeapEntityServiceException;

public class LeapEntityHttpUtility {

	protected static Logger logger = LoggerFactory.getLogger(LeapEntityHttpUtility.class.getName());
	static final ClassLoader CLASS_LOADER = LeapEntityHttpUtility.class.getClassLoader();

	public static String httpPost(String url, String body, String contentType,
			List<EntityRestRequestHeader> requestRequiredHeaders, SSLConfig sslconfig, AuthenticationConfig authConfig)
			throws LeapEntityServiceException {
		String methodName = "httpPost";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		try {
			if (body == null || body.isEmpty()) {
				throw new LeapEntityServiceException(new Throwable(), "Body is either null or empty",
						"Body is either null or empty", 401);
			}

			CloseableHttpClient httpClient = null;
			if (url.trim().startsWith(HTTPS)) {
				httpClient = buildHttpsClient(sslconfig, httpClient);
			} else {
				httpClient = HttpClientBuilder.create().build();
			}

			HttpPost request = new HttpPost(url.trim());
			request.addHeader(CONTENT_TYPE, contentType);
			StringEntity params = new StringEntity(body.trim());
			addHeadersToRequest(request, requestRequiredHeaders, body);
			request.setEntity(params);
			logger.debug("{} URL : {}, request data : {}", LEAP_LOG_KEY, url , request);
			logger.debug("{} Headers in : {}" ,LEAP_LOG_KEY, Arrays.toString(request.getAllHeaders()));
			HttpResponse response = httpClient.execute(request);
			String respBody = IOUtils.toString(response.getEntity().getContent());
			int resultCode = response.getStatusLine().getStatusCode();
			if (resultCode >= 200 && resultCode < 299) {
				return respBody;
			} else
				throw new LeapEntityServiceException(new Throwable(),
						" unable to perform the http call to webservice due to responseBody :" + respBody, respBody,
						resultCode);

		} catch (Exception e) {
			e.printStackTrace();
			throw new LeapEntityServiceException(new Throwable(),
					" unable to perform the http call to webservice due to responseBody ", e.getMessage(), 400);
		}

	}// ..end of the
		// method
		// httpPost

	/**
	 * @return
	 * @throws LeapEntityServiceException
	 * 
	 */
	public static String sendGet(String url, String contentType, List<EntityRestRequestHeader> requestRequiredHeaders,
			SSLConfig sslconfig, AuthenticationConfig authConfig) throws LeapEntityServiceException {
		String methodName = "sendGet";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			CloseableHttpClient httpClient = null;
			if (url.trim().startsWith(HTTPS)) {
				httpClient = buildHttpsClient(sslconfig, httpClient);
			} else {
				httpClient = HttpClientBuilder.create().build();
			}
			HttpGet request = new HttpGet(url.trim());
			addHeadersToGetRequest(request, requestRequiredHeaders);
			request.addHeader(CONTENT_TYPE, contentType);
			HttpResponse response = httpClient.execute(request);
			String respBody = IOUtils.toString(response.getEntity().getContent());
			int resultCode = response.getStatusLine().getStatusCode();
			if (resultCode >= 200 && resultCode < 299) {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return respBody;
			} else
				throw new LeapEntityServiceException(new Throwable(),
						" unable to perform the http call to webservice due to responseBody :" + respBody, respBody,
						resultCode);
		} catch (Exception e) {
			e.printStackTrace();
			throw new LeapEntityServiceException(new Throwable(),
					" unable to perform the http call to webservice due to responseBody ", e.getMessage(), 400);
		}
		

	}// ..end of the method sendGet

	/**
	 * This method is used to build the https client based on the SSL config
	 * 
	 * @param sslconfig
	 *            :: {@link SSLConfig}
	 * @param httpClient
	 *            :: {@link CloseableHttpClient}
	 * @return http client
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws KeyManagementException
	 */
	private static CloseableHttpClient buildHttpsClient(SSLConfig sslconfig, CloseableHttpClient httpClient)
			throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException,
			KeyManagementException {
		if (sslconfig != null) {
			String sslFilePath = sslconfig.getSSLFilePath();
			String keyPassword = sslconfig.getKeyPassword();
			SSLContext sslContext = SSLContext.getInstance(TLS);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore ks = KeyStore.getInstance(JKS);
			ks.load(CLASS_LOADER.getResourceAsStream(sslFilePath), keyPassword.toCharArray());
			tmf.init(ks);
			sslContext.init(null, tmf.getTrustManagers(), null);
			httpClient = HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			}).build();
		}
		return httpClient;
	}// ..end
		// of
		// the
		// method
		// buildHttpsClient

	/**
	 * This method is used to add the headers to the request
	 * 
	 * @param request
	 *            :: {@link HttpPost}
	 * @param requestRequiredHeaders
	 *            :: {@link EntityRestRequestHeader}
	 * @param body
	 *            :: {@link String} -> request body
	 */
	private static void addHeadersToRequest(HttpPost request, List<EntityRestRequestHeader> requestRequiredHeaders,
			String body) {
		for (EntityRestRequestHeader entityRestRequestHeader : requestRequiredHeaders) {
			String source = entityRestRequestHeader.getSource();
			if (source.equalsIgnoreCase(STATIC_SOURCE))
				request.addHeader(entityRestRequestHeader.getName(), entityRestRequestHeader.getValue());
			else {
				String entityKey = entityRestRequestHeader.getEntityKey();
				JSONObject requestBody = new JSONObject(body);
				String value = requestBody.getString(entityKey);
				request.addHeader(entityRestRequestHeader.getName(), value);
			}
		} // ..end of for loop

	}// ..end of the method addHeadersToRequest

	/**
	 * This method is used to add the headers to the request
	 * 
	 * @param request
	 *            :: {@link HttpPost}
	 * @param requestRequiredHeaders
	 *            :: {@link EntityRestRequestHeader}
	 * @param body
	 *            :: {@link String} -> request body
	 */
	private static void addHeadersToGetRequest(HttpGet request, List<EntityRestRequestHeader> requestRequiredHeaders) {
		for (EntityRestRequestHeader entityRestRequestHeader : requestRequiredHeaders) {
			String source = entityRestRequestHeader.getSource();
			if (source.equalsIgnoreCase(STATIC_SOURCE))
				request.addHeader(entityRestRequestHeader.getName(), entityRestRequestHeader.getValue());
		} // ..end of for loop

	}// ..end of the method addHeadersToRequest

}
