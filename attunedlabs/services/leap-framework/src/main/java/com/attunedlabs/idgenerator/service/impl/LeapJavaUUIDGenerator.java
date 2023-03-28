package com.attunedlabs.idgenerator.service.impl;

import java.util.UUID;

import com.attunedlabs.idgenerator.service.ILeapGenerateId;

public class LeapJavaUUIDGenerator implements ILeapGenerateId {

	/**
	 * Method used to generate UUID. By default its length is 32.
	 * 
	 * @return Random UUID of length 32.
	 */
	@Override
	public String generateId() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * Method used to generate UUID. By default its length is 32, even if the
	 * length is provided it always provide default UUID of length 32.If we want
	 * random ID of required length then use class 'LeapJavaRandomIdGenerator'.
	 * 
	 * @return Random UUID of length 32.
	 */
	@Override
	public String generateId(int length) {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * Method used to generate UUID of a given prefix and suffix.By default its
	 * length is 32, even if the length is provided it always provide default
	 * UUID of length 32.If we want random ID of required length then use class
	 * 'LeapJavaRandomIdGenerator'.
	 * 
	 * 
	 * @param length
	 * @param prefix
	 * @param suffix
	 * @return UUID of given ,prefix and suffix.
	 */
	@Override
	public String generateId(int length, String prefix, String suffix) {
		if (prefix != null && suffix != null) {
			return prefix.trim() + UUID.randomUUID().toString().replaceAll("-", "") + suffix.trim();
		}
		if (prefix != null && suffix == null) {
			return prefix.trim() + UUID.randomUUID().toString().replaceAll("-", "");
		}
		if (suffix != null && prefix == null) {
			return UUID.randomUUID().toString().replaceAll("-", "") + suffix.trim();
		}
		return prefix.trim() + UUID.randomUUID().toString().replaceAll("-", "") + suffix.trim();
	}

}
