package com.attunedlabs.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.config.persistence.ConfigPersistenceServiceTest;
import com.attunedlabs.config.persistence.TenantConfigTreeServiceTest;
import com.attunedlabs.config.persistence.dao.ConfigFeatureMasterDAOTest;
import com.attunedlabs.config.persistence.dao.ConfigNodeDAOTest;
import com.attunedlabs.config.persistence.dao.ConfigNodeDataDAOTest;
import com.attunedlabs.config.persistence.dao.FeatureDeploymentDAOTest;
import com.attunedlabs.config.persistence.dao.PrettyURLMappingDaoTest;

@RunWith(Suite.class)
@SuiteClasses({		
		ConfigPersistenceServiceTest.class, 
		TenantConfigTreeServiceTest.class, 
		ConfigFeatureMasterDAOTest.class,
		ConfigNodeDAOTest.class, 
		ConfigNodeDataDAOTest.class, 
		FeatureDeploymentDAOTest.class,
		PrettyURLMappingDaoTest.class
		})
public class PersistenceTestSuites {
	
}
