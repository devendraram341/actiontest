package com.attunedlabs.idgenerator.service;

/**
 * Leap Java Id Generator Interface
 * 
 * @author Reactiveworks
 *
 */
public interface ILeapGenerateId {

	/**
	 * Method used to generate random Id. By default its length is 8.
	 * 
	 * @return Random Id of length 8
	 */
	public String generateId();

	/**
	 * Method used to generate random Id of a given length. Length should be
	 * grater than 0.
	 * 
	 * @param length
	 * @return Random Id of length provided.
	 */
	public String generateId(int length);

	/**
	 * Method used to generate random Id of a given length, prefix and suffix.
	 * Length should be grater than 0.
	 * 
	 * @param length
	 * @param prefix
	 * @param suffix
	 * @return Random Id of given length,prefix and suffix.
	 */
	public String generateId(int length, String prefix, String suffix);
}
