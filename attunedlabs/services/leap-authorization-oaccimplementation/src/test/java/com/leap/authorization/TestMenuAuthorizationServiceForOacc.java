package com.leap.authorization;

public class TestMenuAuthorizationServiceForOacc {
	/*private Logger logger = LoggerFactory.getLogger(getClass());
	private Properties properties;
	private IMenuAuthorizationService menuService;
	private List<String> permissionList;
	private IAuthorizationSetup setupService;

	@Before
	public void init() throws InvalidConfiguration, IOException {
		permissionList = new ArrayList<>();
		permissionList.add(OaccTestConstant.TEST_VIEW);
		permissionList.add(OaccTestConstant.TEST_DELETE);
		permissionList.add(OaccTestConstant.TEST_EDIT);
		menuService = new MenuAuthorizationServiceImpl();
	//	setupService = new AuthorizationSetupImpl();
		properties = new Properties();
		properties.load(getClass().getClassLoader().getResourceAsStream(OaccTestConstant.RESOURCE_FILE_NAME));
	}

	*//**
	 * Test case to create new resource class menu.
	 *//*
	// @Test
	public void testCreateResourceClassMenu() throws AuthorizationSetupException {
		setupService.addNewResourceTypeToAuthorize(OaccTestConstant.TEST_RESRC_CLASS_MENU, permissionList);
	}

	// @Test
	public void testCreatePermissionForMenuClass() throws AuthorizationSetupException {
		List<String> permission = new ArrayList<>();
		permission.add("READ");
		setupService.addNewPermissionsOnResourceType(permission, OaccTestConstant.TEST_RESRC_CLASS_MENU);
	}

	// @Test
	public void testAddMenuItems() throws ResourceRegistryException, InvalidConfiguration {
		String[] itemsToBeadded = properties.getProperty("menuTest").split(",");
		for (String item : itemsToBeadded) {
			logger.debug(item);
		}
		List<String> addables = Arrays.asList(itemsToBeadded);
		menuService.addMenuItems(addables, OaccTestConstant.TEST_TENANT);
	}

	@Test
	public void testGetAllMenuItemsAccessibleforUser() throws ResourceAccessException {
		List<String> setOfItems = menuService.getAllMenuItemsAccessibleforUser(OaccTestConstant.TEST_USER_ROCK,
				OaccTestConstant.TEST_TENANT);
		Assert.assertTrue(setOfItems.size() == 0);
	}

	@Test
	public void testGetAllMenuItemsAccessibleforRole() throws ResourceAccessException {
		List<String> setOfItems = menuService.getAllMenuItemsAccessibleforRole(OaccTestConstant.TEST_ROLE_SF_DEVELOPER,
				OaccTestConstant.TEST_TENANT);
		Assert.assertTrue(setOfItems.size() == 0);
	}

	@Test
	public void testHasPermissionForUser() throws ResourceAuthorizeException {
		Boolean result = menuService.hasPermissionForUser(OaccTestConstant.TEST_USER_KIM, OaccTestConstant.TEST_ITME_1,
				OaccTestConstant.TEST_VIEW,OaccTestConstant.TEST_TENANT);
		Assert.assertEquals(Boolean.parseBoolean("true"), result);
	}*/
}