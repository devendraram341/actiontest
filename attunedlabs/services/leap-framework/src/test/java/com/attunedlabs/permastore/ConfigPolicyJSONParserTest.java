package com.attunedlabs.permastore;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigPolicyJSONParserTest {

	private final String jsonString = "{\"Policy\":{\"Name\":\"Testing\",\"Version\":\"1.0\",\"Group\":\"TestGroup\",\"Facts\":{\"FactKey1\":\"1234\",\"FactKey1\":\"5678\"},\"WhenApplicable\":{\"Evaluation\":\"TestEvaliation\"},\"PolicyData\":{\"data\":\"testData\"}}}";

	private ConfigPolicyJSONParser jsonParser = new ConfigPolicyJSONParser(jsonString);
	final Logger log = LoggerFactory.getLogger(ConfigPolicyJSONParserTest.class);

	@Test
	public void testGetConfigPolicy() {
		ConfigPolicy configPolicyData = jsonParser.getConfigPolicy();
		Assert.assertNotNull("Config policy Should not be null ::", configPolicyData);
		Assert.assertEquals("ConfigPolicy name Should be same as 'Testing' :: ", "Testing", configPolicyData.getName());
	}
}
