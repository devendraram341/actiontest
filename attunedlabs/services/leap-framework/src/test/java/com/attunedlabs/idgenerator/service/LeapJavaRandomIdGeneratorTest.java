package com.attunedlabs.idgenerator.service;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.idgenerator.service.impl.LeapJavaRandomIdGenerator;

public class LeapJavaRandomIdGeneratorTest {

	private ILeapGenerateId javaGenerateId = new LeapJavaRandomIdGenerator();
	final Logger log = LoggerFactory.getLogger(LeapJavaRandomIdGeneratorTest.class);

	/**
	 * this method use for check random number generate or not.
	 */
	@Test
	public void testGenerateId() {
		String randomId = javaGenerateId.generateId();
		Assert.assertNotNull("Random Id Should not be null :: ", randomId);
	}

	/**
	 * this method use for check random number generate with length.
	 */
	@Test
	public void testGenerateIdWithLength() {
		String randomId = javaGenerateId.generateId(10);
		Assert.assertNotNull("Random Id Should not be null ::", randomId);
		Assert.assertEquals("Random id Should be same as total length 10 ::", 10, randomId.length());
	}

	/**
	 * this method use for check random number generate with fixed pre and post data.
	 */
	@Test
	public void testGenerateIdLengthwithPreAndPost() {

		String randomIdWithPre = javaGenerateId.generateId(10, "ABC", null);
		Assert.assertNotNull("Random Id Should not be null ::", randomIdWithPre);
		Assert.assertEquals("Random id Should be same as total length 13 ::", 13, randomIdWithPre.length());
		Assert.assertTrue(randomIdWithPre.startsWith("ABC"));

		String randomIdWithPreAndPost = javaGenerateId.generateId(10, "ABC", "XYZ");
		Assert.assertNotNull("Random Id Should not be null ::", randomIdWithPreAndPost);
		Assert.assertEquals("Random id Should be same as total length 16 ::", 16, randomIdWithPreAndPost.length());
		Assert.assertTrue(randomIdWithPreAndPost.startsWith("ABC"));
		Assert.assertTrue(randomIdWithPreAndPost.endsWith("XYZ"));
	}
}
