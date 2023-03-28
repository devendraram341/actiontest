
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpURLConnectionExample {

	private final static String URL = "http://localhost:9080/ecomm/json/PAndG/ALPSIntegration/test";
	private final static String POST = "POST";

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 5; i++) {
			sendPostRequest();
		}
	}

	private static void sendPostRequest()
			throws FileNotFoundException, UnsupportedEncodingException, JSONException, IOException {
		final Map<String, String> headerDetails = setHeaderDetails();
		final JSONObject dailyTransJson = new JSONObject();
		dailyTransJson.put("samplekey", "samplevalue");
		new Thread(() -> sendPostRequest(URL, dailyTransJson.toString(), headerDetails)).start();
	}

	private static void sendPostRequest(String requestUrl, String body, Map<String, String> headers) {
		try {
			URL url = new URL(requestUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod(POST);
			Set<Entry<String, String>> headerSet = headers.entrySet();
			for (Entry<String, String> header : headerSet) {
				conn.setRequestProperty(header.getKey(), header.getValue());
			}
			OutputStream os = conn.getOutputStream();
			os.write(body.getBytes());
			os.flush();
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				System.out.println(conn.getResponseMessage());
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			while ((output = br.readLine()) != null) {
				 System.out.println(output);
			}
			conn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Map<String, String> setHeaderDetails() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("siteId", "FR57");
		headers.put("accountId", "P&G");
		return headers;
	}

}