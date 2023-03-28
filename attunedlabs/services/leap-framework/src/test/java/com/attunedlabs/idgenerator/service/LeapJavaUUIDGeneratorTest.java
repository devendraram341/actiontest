package com.attunedlabs.idgenerator.service;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.idgenerator.service.impl.LeapJavaUUIDGenerator;

public class LeapJavaUUIDGeneratorTest {

	private ILeapGenerateId javaUUIDGenerateId = new LeapJavaUUIDGenerator();
	final Logger log = LoggerFactory.getLogger(LeapJavaUUIDGeneratorTest.class);

	/**
	 * this method use for check random number generate or not.
	 */
	@Test
	public void testGenerateId() {
		String randomId = javaUUIDGenerateId.generateId();
		Assert.assertNotNull("Random Id Should not be null :: ", randomId);
	}

	/**
	 * this method use for check random number generate with length.
	 */
	@Test
	public void testGenerateIdWithLength() {
		String randomId = javaUUIDGenerateId.generateId(10);
		Assert.assertNotNull("Random Id Should not be null ::", randomId);
		log.debug(randomId);
		Assert.assertEquals("Random id Should be same as total length 32 ::", 32, randomId.length());
	}

	/**
	 * this method use for check random number generate with pre and post data.
	 */
	@Test
	public void testGenerateIdLengthwithPreAndPost() {

		String randomIdWithPre = javaUUIDGenerateId.generateId(10, "ABC", null);
		Assert.assertNotNull("Random Id Should not be null ::", randomIdWithPre);
		Assert.assertEquals("Random id Should be same as total length 35 ::", 35, randomIdWithPre.length());
		Assert.assertTrue(randomIdWithPre.startsWith("ABC"));

		String randomIdWithPreAndPost = javaUUIDGenerateId.generateId(10, "ABC", "XYZ");
		Assert.assertNotNull("Random Id Should not be null ::", randomIdWithPreAndPost);
		Assert.assertEquals("Random id Should be same as total length 38 ::", 38, randomIdWithPreAndPost.length());
		Assert.assertTrue(randomIdWithPreAndPost.startsWith("ABC"));
		Assert.assertTrue(randomIdWithPreAndPost.endsWith("XYZ"));
	}
}
