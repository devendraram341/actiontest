package com.attunedlabs.config.persistence.dao;

import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.persistence.PrettyUrlMapping;
import com.attunedlabs.config.persistence.exception.PrettyUrlMappingDaoException;

public class PrettyURLMappingDaoTest {

	private static PrettyURLMappingDao urlMappingDao;

	/**
	 * In this method used for Initialization.
	 */
	@BeforeClass
	public static void initializeTheDataGrid() {
		urlMappingDao = new PrettyURLMappingDao();
	}

	/**
	 * this method used for Adding prettyUrl data into DB.
	 * 
	 * @throws PrettyUrlMappingDaoException
	 */
	@Test
	public void testAddPrettyUrlMapping() throws PrettyUrlMappingDaoException {
		PrettyUrlMapping prettyUrlMapping =  getPrettyURLData();
		Assert.assertNotNull(prettyUrlMapping);
		
		PrettyUrlMapping prettyUrl = urlMappingDao.getPrettyUrlMappingByPrettyUrlString(prettyUrlMapping);
		if(prettyUrl==null) {
			urlMappingDao.addPrettyUrlMapping(prettyUrlMapping);
		}
		
	}

	/**
	 * this method used for get prettyUrl Data from DB.
	 * 
	 * @throws PrettyUrlMappingDaoException
	 */
	@Test
	public void testGetPrettyUrlMappingByPrettyUrlString() throws PrettyUrlMappingDaoException {
		PrettyUrlMapping prettyUrlMapping = getPrettyURLData();
		Assert.assertNotNull(prettyUrlMapping);

		PrettyUrlMapping prettyUrl = urlMappingDao.getPrettyUrlMappingByPrettyUrlString(prettyUrlMapping);
		Assert.assertNotNull(prettyUrlMapping);
		Assert.assertEquals(GenericTestConstant.TEST_SITE, prettyUrl.getSiteId());
	}

	/**
	 * this method used for get all prettyUrlmapping from DB.
	 * 
	 * @throws PrettyUrlMappingDaoException
	 */
	@Test
	public void testGetAllPrettyUrlMappings() throws PrettyUrlMappingDaoException {
		List<PrettyUrlMapping> listOfPrettyUrl = urlMappingDao.getAllPrettyUrlMappings();
		Assert.assertTrue(listOfPrettyUrl.size() > 0);
	}

	private PrettyUrlMapping getPrettyURLData() {
		PrettyUrlMapping prettyUrlMapping = new PrettyUrlMapping(GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE, "prettyUriTesting", setActualUrl(), 1);
		return prettyUrlMapping;
	}
	
	private String setActualUrl() {
		return GenericTestConstant.TEST_FEATUREGROUP + "/" + GenericTestConstant.TEST_FEATURE + "/" + GenericTestConstant.TEST_SERVICE;
	}
}
