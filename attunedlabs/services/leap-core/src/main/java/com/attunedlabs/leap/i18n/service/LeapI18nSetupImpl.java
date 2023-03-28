package com.attunedlabs.leap.i18n.service;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.i18n.dao.I18nTextDao;
import com.attunedlabs.leap.i18n.entity.LeapI18nText;
import com.attunedlabs.leap.i18n.entity.LeapValidPossibilities;
import com.attunedlabs.leap.i18n.exception.LocaleRegistryException;
import com.attunedlabs.leap.i18n.exception.LocaleResolverException;
import com.attunedlabs.leap.i18n.utils.LeapI18nUtil;

public class LeapI18nSetupImpl implements ILeapI18nSetup {

	private I18nTextDao localeMessagesDao;
	private static final Logger logger = LoggerFactory.getLogger(LeapI18nSetupImpl.class);

	@Override
	public void addNewLocaleMessage(LeapI18nText localeContext) throws LocaleRegistryException {
		String methodName = "addNewLocaleMessage";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String tenantId = localeContext.getTenantId();
		String siteId = localeContext.getSiteId();
		String feature = localeContext.getFeature();
		String localeId = localeContext.getLocaleId();
		String usage = localeContext.getElementId();
		String i18nId = localeContext.getI18nId();
		String textValue = localeContext.getTextValue();
		logger.trace("{} tenantId: {} : siteId: {}", LEAP_LOG_KEY, tenantId, siteId);
		if (!(LeapI18nUtil.isEmpty(tenantId) || LeapI18nUtil.isEmpty(siteId))) {
			if (!(LeapI18nUtil.isEmpty(localeId) || LeapI18nUtil.isEmpty(textValue) || LeapI18nUtil.isEmpty(feature))) {
				localeMessagesDao = new I18nTextDao();
				// ..Returns the id which is generated when insert
				int id = localeMessagesDao.insertNewMessage(tenantId, siteId, feature, localeContext.getResourceType(),
						localeContext.getMsgVariant(), localeId, usage, i18nId, textValue);
				logger.info("{} Locale-Record successfully inserted: {}", LEAP_LOG_KEY, id);

			} else {
				throw new LocaleRegistryException(
						"Unable to add new LocaleContext to the specified localeContext Empty values exist in the request! : "
								+ localeContext);
			}
		} else {
			throw new LocaleRegistryException(
					"Unable to add new LocaleContext to the specified localeContext Empty values exist in the request! : "
							+ localeContext);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// ..end of the method

	@Override
	public void addNewLocaleMessages(List<LeapI18nText> localeContexts) throws LocaleRegistryException {
		String methodName = "addNewLocaleMessages";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		for (LeapI18nText localeContext : localeContexts) {
			String tenantId = localeContext.getTenantId();
			String siteId = localeContext.getSiteId();
			String feature = localeContext.getFeature();
			String localeId = localeContext.getLocaleId();
			String usage = localeContext.getElementId();
			String i18nId = localeContext.getI18nId();
			String textValue = localeContext.getTextValue();
			if (!(LeapI18nUtil.isEmpty(tenantId) || LeapI18nUtil.isEmpty(siteId))) {
				if (!(LeapI18nUtil.isEmpty(localeId) || LeapI18nUtil.isEmpty(textValue)
						|| LeapI18nUtil.isEmpty(feature))) {
					localeMessagesDao = new I18nTextDao();
					// ..Returns the id which is generated when insert
					int id = localeMessagesDao.insertNewMessage(tenantId, siteId, feature,
							localeContext.getResourceType(), localeContext.getMsgVariant(), localeId, usage, i18nId,
							textValue);
					logger.info("{} Locale-Record successfully inserted:{} ", LEAP_LOG_KEY, id);
				} else {
					throw new LocaleRegistryException(
							"Unable to add new LocaleContext to the specified localeContext Empty values exist in the request! : "
									+ localeContext);
				}
			} else {
				throw new LocaleRegistryException(
						"Unable to add new LocaleContext to the specified localeContext Empty values exist in the request! : "
								+ localeContext);
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// ..end of the method

	/**
	 * 
	 * @param localeMessageContexts
	 * @throws IOException
	 */
	public void buildLocaleBundle(List<LeapI18nText> localeMessageContexts) throws LocaleResolverException {
		Set<String> _tent_site_aNodid_resTyp_varnt_lcleSet = new HashSet<>();
		Map<String, List<LeapI18nText>> map = null;
		if (!localeMessageContexts.isEmpty()) {
			List<LeapI18nText> listOfItems;
			for (LeapI18nText localeMessageContext : localeMessageContexts) {
				String temp = localeMessageContext.getTenantId() + "-" + localeMessageContext.getSiteId() + "-"
						+ localeMessageContext.getFeature() + "-" + localeMessageContext.getLocaleId();
				_tent_site_aNodid_resTyp_varnt_lcleSet.add(LeapI18nUtil.buildBundleFileString(temp));
			}
			map = new HashMap<>();
			for (String allTogether : _tent_site_aNodid_resTyp_varnt_lcleSet) {
				listOfItems = new ArrayList<>();
				for (LeapI18nText localeMessageContext : localeMessageContexts) {
					String temp = localeMessageContext.getTenantId() + "-" + localeMessageContext.getSiteId() + "-"
							+ localeMessageContext.getFeature() + "-" + localeMessageContext.getLocaleId();
					if (temp.equals(allTogether)) {
						listOfItems.add(localeMessageContext);
					}
					map.put(allTogether, listOfItems);
				}
			}
		}
		try {
			writeFiles(map);
		} catch (IOException e) {
			throw new LocaleResolverException(
					"Unable to write the properties file into the classpath specific for locale.", e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param localeMessageContexts
	 * @throws IOException
	 */
	public void buildComboLocaleBundle(List<LeapValidPossibilities> localeComboMessageContexts)
			throws LocaleResolverException {
		String methodName = "buildComboLocaleBundle";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Set<String> _tent_site_aNodid_resTyp_varnt_lcleSet = new HashSet<>();
		Map<String, List<LeapValidPossibilities>> map = null;
		if (!localeComboMessageContexts.isEmpty()) {
			List<LeapValidPossibilities> listOfItems;
			for (LeapValidPossibilities localeMessageContext : localeComboMessageContexts) {
				logger.trace("{} localeMessageContext in first for : {}", LEAP_LOG_KEY, localeMessageContext);
				String temp = localeMessageContext.getTenantId() + "-" + localeMessageContext.getSiteId() + "-"
						+ localeMessageContext.getFeature() + "-" + "vp" + "-" + localeMessageContext.getLocaleId();
				logger.trace("{} temp in first for : {}", LEAP_LOG_KEY, temp);
				_tent_site_aNodid_resTyp_varnt_lcleSet.add(LeapI18nUtil.buildBundleFileString(temp));
			}
			map = new HashMap<>();
			for (String allTogether : _tent_site_aNodid_resTyp_varnt_lcleSet) {
				logger.trace("{} allTogether : {} ", LEAP_LOG_KEY, allTogether);
				listOfItems = new ArrayList<>();
				for (LeapValidPossibilities localeMessageContext : localeComboMessageContexts) {
					logger.trace("{} localeMessageContext in second for : {}", LEAP_LOG_KEY, localeMessageContext);
					String temp = localeMessageContext.getTenantId() + "-" + localeMessageContext.getSiteId() + "-"
							+ localeMessageContext.getFeature() + "-" + "vp" + "-" + localeMessageContext.getLocaleId();
					logger.trace("{} temp in second for : {}", LEAP_LOG_KEY, temp);
					if (temp.equals(allTogether)) {
						listOfItems.add(localeMessageContext);
					}
					map.put(allTogether, listOfItems);
				}
			}
		}
		try {
			logger.trace("{} map : {}", LEAP_LOG_KEY, map);
			writeComboFiles(map);
		} catch (IOException e) {
			throw new LocaleResolverException(
					"Unable to write the properties file into the classpath specific for locale.", e);
		}
	}// ..end of the method

	/**
	 * Private method to work out to write the properties file in to specified path
	 * 
	 * @param properties
	 * @param localeId
	 * @param msgVariant
	 * @throws IOException
	 */
	private void writeFiles(Map<String, List<LeapI18nText>> resourceMap) throws IOException {
		Properties properties;
		for (String localeKeys : resourceMap.keySet()) {
			logger.trace("{} localeKeys : {} ", LEAP_LOG_KEY, localeKeys);
			properties = new Properties();
			List<LeapI18nText> eachList = resourceMap.get(localeKeys);
			logger.trace("{} eachList : {} ", LEAP_LOG_KEY, eachList);
			for (LeapI18nText eachItem : eachList) {
				logger.trace("{} eachItem : {} ", LEAP_LOG_KEY, eachItem);
				properties.setProperty(eachItem.getElementId(), eachItem.getTextValue());
			}
			File file = new File("src/main/resources/" + localeKeys + ".properties");
			logger.trace("{} file : {} ", LEAP_LOG_KEY, file);
			FileOutputStream fileOut = new FileOutputStream(file);
			properties.store(fileOut, localeKeys);
			fileOut.close();
		}
	}// ..end of the method

	/**
	 * Private method to work out to write the properties file in to specified path
	 * 
	 * @param properties
	 * @param localeId
	 * @param msgVariant
	 * @throws IOException
	 */
	private void writeComboFiles(Map<String, List<LeapValidPossibilities>> resourceMap) throws IOException {
		Properties properties;
		for (String localeKeys : resourceMap.keySet()) {
			logger.trace("{} localeKeys : {} ", LEAP_LOG_KEY, localeKeys);
			properties = new Properties();
			List<LeapValidPossibilities> eachList = resourceMap.get(localeKeys);
			logger.trace("{} eachList : {} ", LEAP_LOG_KEY, eachList);
			for (LeapValidPossibilities eachItem : eachList) {
				logger.trace("{} eachItem : {} ", LEAP_LOG_KEY, eachItem);
				properties.setProperty(eachItem.getVpListI18nId() + "." + eachItem.getVpCode(),
						eachItem.getTextValue());
			}
			File file = new File("src/main/resources/" + localeKeys + ".properties");
			FileOutputStream fileOut = new FileOutputStream(file);
			properties.store(fileOut, localeKeys);
			fileOut.close();
		}
	}// ..end of the method

	@Override
	public void deleteLocale(String tenantId, String siteId) throws LocaleRegistryException, LocaleResolverException {
		// TODO Auto-generated method stub

	}
}
