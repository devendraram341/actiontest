package com.attunedlabs.idgenerator.service.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.idgenerator.service.ILeapGenerateId;

/**
 * @author Reactiveworks.
 *
 */
public class LeapJavaRandomIdGenerator implements ILeapGenerateId {

	static Logger logger = LoggerFactory.getLogger(LeapJavaRandomIdGenerator.class);
	public static final String CHARACTERS_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	/**
	 * Method used to generate random Id. By default its length is 8.
	 * 
	 * @return Random Id of length 8
	 */
	@Override
	public String generateId() {
		return RandomStringUtils.random(8, CHARACTERS_SET);
	}

	/**
	 * Method used to generate random Id of a given length. Length should be
	 * grater than 0.
	 * 
	 * @param length
	 * @return Random Id of length provided.
	 */
	@Override
	public String generateId(int length) {
		return RandomStringUtils.random(length, CHARACTERS_SET);
	}

	/**
	 * Method used to generate random Id of a given length, prefix and suffix.
	 * Length should be grater than 0.
	 * 
	 * @param length
	 * @param prefix
	 * @param suffix
	 * @return Random Id of given length,prefix and suffix.
	 */
	@Override
	public String generateId(int length, String prefix, String suffix) {
		String methodName = "generateId";
		logger.debug("{} entered into the method {} with length prefix and suffix", LEAP_LOG_KEY, methodName);
		if (length > 0) {
			if (prefix != null && suffix != null) {
				return prefix.trim() + RandomStringUtils.random(length, CHARACTERS_SET) + suffix.trim();
			}
			if (prefix != null && suffix == null) {
				return prefix.trim() + RandomStringUtils.random(length, CHARACTERS_SET);
			}
			if (suffix != null && prefix == null) {
				return RandomStringUtils.random(length, CHARACTERS_SET) + suffix.trim();
			}
		} else if (length == 0) {
			if (prefix != null && suffix != null) {
				return prefix.trim() + RandomStringUtils.random(8, CHARACTERS_SET) + suffix.trim();
			}
			if (prefix != null && suffix == null) {
				return prefix.trim() + RandomStringUtils.random(8, CHARACTERS_SET);
			}
			if (suffix != null && prefix == null) {
				return RandomStringUtils.random(8, CHARACTERS_SET) + suffix.trim();
			}
		}
		return prefix.trim() + RandomStringUtils.random(length, CHARACTERS_SET) + suffix.trim();

	}

}
