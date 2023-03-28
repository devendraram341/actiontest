package com.attunedlabs.leapentity.service;

import org.apache.camel.Exchange;
import org.json.JSONObject;

import com.attunedlabs.leapentity.config.jaxb.Entity;
import com.attunedlabs.leapentity.config.jaxb.EntityAccess;
import com.attunedlabs.leapentity.config.jaxb.EntityAccessConfig;
import com.attunedlabs.leapentity.config.jaxb.EntityRestAccess;

/**
 * this will show the service available for entity
 * 
 * @author Reactiveworks
 *
 */
public interface ILeapEntityService {

	/**
	 * this method will perform the entity Operation based on the entity
	 * configuration type
	 * 
	 * @param entity
	 *            {@link Entity}
	 * @param entityAccessConfig
	 *            {@link EntityAccessConfig}
	 * @param entityAccess
	 *            {@link EntityAccess}
	 * @param entityDefaultReq
	 *            {@link JSONObject}
	 * @param exchange
	 *            {@link Exchange}
	 * @return {@link Object}
	 * @throws LeapEntityServiceException
	 *             if unable to perform the operation
	 */
	public Object performEntityOperation(Entity entity, EntityAccessConfig entityAccessConfig,
			EntityAccess entityAccess, EntityRestAccess entityRestAccess, Object entityDefaultReq, Exchange exchange)
			throws LeapEntityServiceException;

}
