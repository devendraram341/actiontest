package com.leap.authorization;

public class AuthOaccGenericServiceTest {
	/*private Logger logger = LoggerFactory.getLogger(getClass());
	private IAuthorizationGenericService authGenericService;
	private Properties properties;
	private List<String> permissionList;

	@Before
	public void init() throws InvalidConfiguration, IOException {
		permissionList = new ArrayList<>();
		permissionList.add(OaccTestConstant.TEST_VIEW);
		permissionList.add(OaccTestConstant.TEST_DELETE);
		permissionList.add(OaccTestConstant.TEST_EDIT);
		authGenericService = new AuthorizationGenericServiceImpl();
		properties = new Properties();
		properties.load(getClass().getClassLoader().getResourceAsStream(OaccTestConstant.RESOURCE_FILE_NAME));
	}

	*//**
	 * Test case to create user list with disabling password.
	 * 
	 * @throws InvalidConfiguration
	 *//*
	// @Test
	public void testCreateUsers() throws ResourceRegistryException, InvalidConfiguration {
		List<User> userList = new ArrayList<>();
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_KIM);
		user1.setPassword(null);
		User user2 = new User();
		user2.setUserName(OaccTestConstant.TEST_USER_RONY);
		user2.setPassword(null);
		userList.add(user1);
		userList.add(user2);
		authGenericService.registerUsers(userList, OaccTestConstant.TEST_TENANT, false);
	}

	@Test(expected = ResourceRegistryException.class)
	public void testCreateUsersEmptyList() throws ResourceRegistryException, InvalidConfiguration {
		List<User> userList = new ArrayList<>();
		authGenericService.registerUsers(userList, OaccTestConstant.TEST_TENANT, false);
	}

	@Test(expected = ResourceRegistryException.class)
	public void testCreateUsersEmptyTenant() throws ResourceRegistryException, InvalidConfiguration {
		List<User> userList = new ArrayList<>();
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_KIM);
		user1.setPassword(null);
		User user2 = new User();
		user2.setUserName(OaccTestConstant.TEST_USER_RONY);
		user2.setPassword(null);
		userList.add(user1);
		userList.add(user2);
		authGenericService.registerUsers(userList, null, false);
	}

	*//**
	 * Test case to create user list with enabling password.
	 * 
	 * @throws InvalidConfiguration
	 *//*
	// @Test
	public void testCreateUsersWithPassword() throws ResourceRegistryException, InvalidConfiguration {
		List<User> userList = new ArrayList<>();
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_SAM);
		user1.setPassword("pass1".toCharArray());
		User user2 = new User();
		user2.setUserName(OaccTestConstant.TEST_USER_MARY);
		user2.setPassword("pass2".toCharArray());
		userList.add(user1);
		userList.add(user2);
		authGenericService.registerUsers(userList, OaccTestConstant.TEST_TENANT, true);
	}

	@Test(expected = ResourceRegistryException.class)
	public void testCreateUsersEmptyListPass() throws ResourceRegistryException, InvalidConfiguration {
		List<User> userList = new ArrayList<>();
		authGenericService.registerUsers(userList, OaccTestConstant.TEST_TENANT, true);
	}

	@Test(expected = ResourceRegistryException.class)
	public void testCreateUsersEmptyTenantPass() throws ResourceRegistryException, InvalidConfiguration {
		List<User> userList = new ArrayList<>();
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_SAM);
		user1.setPassword("pass1".toCharArray());
		User user2 = new User();
		user2.setUserName(OaccTestConstant.TEST_USER_MARY);
		user2.setPassword("pass2".toCharArray());
		userList.add(user1);
		userList.add(user2);
		authGenericService.registerUsers(userList, null, true);
	}

	*//**
	 * Test case to create single user without password.
	 * 
	 * @throws ResourceRegistryException
	 * @throws InvalidConfiguration
	 *//*
	// @Test
	public void testCreateUsersRock() throws ResourceRegistryException, InvalidConfiguration {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_ROCK);
		authGenericService.registerUser(user1, OaccTestConstant.TEST_TENANT, false);
	}

	*//**
	 * Test to check fail cases while creating user.
	 * 
	 * @throws ResourceRegistryException
	 * @throws InvalidConfiguration
	 *//*
	@Test(expected = ResourceRegistryException.class)
	public void testExistingUserException() throws ResourceRegistryException, InvalidConfiguration {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_ROCK);
		authGenericService.registerUser(user1, OaccTestConstant.TEST_TENANT, false);
	}

	*//**
	 * Test case to create single user with password.
	 * 
	 * @throws ResourceRegistryException
	 * @throws InvalidConfiguration
	 *//*
	// @Test
	public void testCreateUserPassword() throws ResourceRegistryException, InvalidConfiguration {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_SIN);
		user1.setPassword("pass1".toCharArray());
		authGenericService.registerUser(user1, OaccTestConstant.TEST_TENANT, true);
	}

	*//**
	 * Test to check fail cases while creating user.
	 * 
	 * @throws ResourceRegistryException
	 * @throws InvalidConfiguration
	 *//*
	@Test(expected = ResourceRegistryException.class)
	public void testPasswordExistingUserException() throws ResourceRegistryException, InvalidConfiguration {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_ROCKY);
		authGenericService.registerUser(user1, OaccTestConstant.TEST_TENANT, true);
	}

	*//**
	 * Test case to inherit role to user.
	 * 
	 * @throws PermissionGrantException
	 *//*
	@Test
	public void testGrantInheritRoleToUser() throws PermissionGrantException {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_KIM);
		authGenericService.associateUserwithRole(user1, OaccTestConstant.TEST_ROLE_UI_DEVELOPER, "RoiAuthTest");
	}

	@Test(expected = PermissionGrantException.class)
	public void testFailInheritRoleToUser() throws PermissionGrantException {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_KIM);
		authGenericService.associateUserwithRole(user1, OaccTestConstant.TEST_ROLE_UI_DEVELOPER, "User");
	}

	*//**
	 * Test case to create direct permission for user on resources.
	 * 
	 * @throws PermissionGrantException
	 *//*
	@Test
	public void testCreateDirectPermissionOnResourceForKim() throws PermissionGrantException {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_KIM);
		List<String> permissionList = new ArrayList<>();
		permissionList.add(OaccTestConstant.TEST_VIEW);
		permissionList.add(OaccTestConstant.TEST_DELETE);
		permissionList.add(OaccTestConstant.TEST_EDIT);
		authGenericService.grantResourcePermissionsForUser(user1, permissionList, "Item.1", "RoiAuthTest");
	}

	@Test
	public void testCreateDirectPermissionOnResourceForRony() throws PermissionGrantException {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_RONY);
		List<String> permissionList = new ArrayList<>();
		permissionList.add(OaccTestConstant.TEST_VIEW);
		permissionList.add(OaccTestConstant.TEST_DELETE);
		authGenericService.grantResourcePermissionsForUser(user1, permissionList, "Item.1", "RoiAuthTest");
	}

	@Test
	public void testCreateDirectPermissionOnResourceForRock() throws PermissionGrantException {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_ROCK);
		List<String> permissionList = new ArrayList<>();
		permissionList.add(OaccTestConstant.TEST_VIEW);
		permissionList.add(OaccTestConstant.TEST_DELETE);
		permissionList.add(OaccTestConstant.TEST_EDIT);
		authGenericService.grantResourcePermissionsForUser(user1, permissionList, "Item.1", "RoiAuthTest");
	}

	*//**
	 * Test case to get all the items which is accessible for user kim.
	 * 
	 * @throws ResourceAccessException
	 *//*
	@Test
	public void testGetAllItemsForUser() throws ResourceAccessException {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_KIM);
		List<String> permissionList = new ArrayList<>();
		permissionList.add(OaccTestConstant.TEST_VIEW);
		permissionList.add(OaccTestConstant.TEST_DELETE);

		List<String> setOfItems = authGenericService.getAllUserPermittedResource(OaccTestConstant.TEST_RESRC_CLASS_ITEM,
				user1, OaccTestConstant.TEST_TENANT, permissionList);
		logger.debug("Size of list for user is: " + setOfItems.size());
		logger.debug("list of resources for user is: " + setOfItems);

	}

	*//**
	 * Test case to get all the items which is accessible for user rony to check
	 * the empty set.
	 * 
	 * @throws ResourceAccessException
	 *//*
	@Test
	public void testGetAllItemsForUserRony() throws ResourceAccessException {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_RONY);
		List<String> permissionList = new ArrayList<>();
		permissionList.add(OaccTestConstant.TEST_VIEW);
		permissionList.add(OaccTestConstant.TEST_DELETE);

		List<String> setOfItems = authGenericService.getAllUserPermittedResource(OaccTestConstant.TEST_RESRC_CLASS_ITEM,
				user1, OaccTestConstant.TEST_TENANT, permissionList);
		Assert.assertTrue(setOfItems.size() == 1);
	}

	*//**
	 * Test case to create permission for role on any specific resource.
	 * 
	 * @throws PermissionGrantException
	 *//*
	@Test
	public void testCreateDirectPermissionOnResourceRole() throws PermissionGrantException {
		authGenericService.grantPermissionOnRole(OaccTestConstant.TEST_TENANT, OaccTestConstant.TEST_ROLE_JAVA_DEVELOPER,
				OaccTestConstant.TEST_VIEW, "Mobile");
	}

	*//**
	 * Test case to get items which is accessible for role java developer.
	 * 
	 * @throws ResourceAccessException
	 *//*
	@Test
	public void testGetAllItemsForRole() throws ResourceAccessException {
		List<String> permissionList = new ArrayList<>();
		permissionList.add(OaccTestConstant.TEST_VIEW);
		List<String> listOfItems = authGenericService.getAllRolePermittedResource(OaccTestConstant.TEST_RESRC_CLASS_ITEM,
				OaccTestConstant.TEST_ROLE_JAVA_DEVELOPER, OaccTestConstant.TEST_TENANT, permissionList);
		Assert.assertTrue(listOfItems.size() == 0);
	}

	*//**
	 * Test case to get items which is accessible for role ui developer and list
	 * size will be zero.
	 * 
	 * @throws ResourceAccessException
	 *//*
	@Test
	public void testGetAllItemsForRoleUI() throws ResourceAccessException {
		List<String> permissionList = new ArrayList<>();
		permissionList.add(OaccTestConstant.TEST_VIEW);
		permissionList.add(OaccTestConstant.TEST_DELETE);
		List<String> listOfItems = authGenericService.getAllRolePermittedResource(OaccTestConstant.TEST_RESRC_CLASS_ITEM,
				OaccTestConstant.TEST_ROLE_UI_DEVELOPER, OaccTestConstant.TEST_TENANT, permissionList);
		Assert.assertTrue(listOfItems.size() == 0);
	}

	*//**
	 * Test case to check whether user is having given permission on any
	 * specific resource or not.
	 * 
	 * @throws ResourceAuthorizeException
	 *//*
	@Test
	public void testHasPermissionForUserKIM() throws ResourceAuthorizeException {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_KIM);
		Boolean result = authGenericService.hasPermissionForUser(user1, "Item.1", OaccTestConstant.TEST_VIEW,
				OaccTestConstant.TEST_TENANT);
		Assert.assertEquals(Boolean.parseBoolean("true"), result);
		// logger.debug("Has permission: " + result);
	}

	@Test
	public void testHasPermissionForUserRONY() throws ResourceAuthorizeException {
		User user1 = new User();
		user1.setUserName(OaccTestConstant.TEST_USER_RONY);
		Boolean result = authGenericService.hasPermissionForUser(user1, OaccTestConstant.TEST_ITME_1,
				OaccTestConstant.TEST_EDIT, OaccTestConstant.TEST_TENANT);
		Assert.assertEquals(Boolean.parseBoolean("false"), result);
		logger.debug("Has permission: " + result);
	}

	*//**
	 * Test case to check whether role is having given permission on any
	 * specific resource or not.
	 * 
	 * @throws ResourceAuthorizeException
	 *//*
	@Test
	public void testHasPermissionForRole() throws ResourceAuthorizeException {

		Boolean result = authGenericService.hasPermissionForRole(OaccTestConstant.TEST_ROLE_JAVA_DEVELOPER, "Mobile",
				OaccTestConstant.TEST_VIEW, OaccTestConstant.TEST_TENANT);
		Assert.assertEquals(Boolean.parseBoolean("true"), result);
		logger.debug("Has permission for role: " + result);
	}*/
}