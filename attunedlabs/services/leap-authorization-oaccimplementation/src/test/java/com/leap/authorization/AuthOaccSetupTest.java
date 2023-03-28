package com.leap.authorization;

public class AuthOaccSetupTest {
	/*private Logger logger = LoggerFactory.getLogger(getClass());
	private IAuthorizationSetup setupService;
	private List<String> permissionList;
	private Properties properties;

	@Before
	public void init() throws InvalidConfiguration, IOException {
		permissionList = new ArrayList<>();
		permissionList.add(OaccTestConstant.TEST_VIEW);
		permissionList.add(OaccTestConstant.TEST_DELETE);
		permissionList.add(OaccTestConstant.TEST_EDIT);
		//setupService = new AuthorizationSetupImpl();
		properties = new Properties();
		properties.load(getClass().getClassLoader().getResourceAsStream(OaccTestConstant.RESOURCE_FILE_NAME));
	}

	*//**
	 * Test case to create new Tenant.
	 * 
	 * @throws AuthorizationSetupException
	 *//*
	// @Test
	public void testCreateTenant() throws AuthorizationSetupException {
		setupService.addNewTenant(OaccTestConstant.TEST_TENANT);
	}

	*//**
	 * Test case to create tenant which will give exception.
	 * 
	 * @throws AuthorizationSetupException
	 *//*
	@Test(expected = AuthorizationSetupException.class)
	public void testExistingTenantException() throws AuthorizationSetupException {
		setupService.addNewTenant(OaccTestConstant.TEST_TENANT);
	}

	@Test(expected = AuthorizationSetupException.class)
	public void testNullTenantException() throws AuthorizationSetupException {
		setupService.addNewTenant(null);
	}

	@Test(expected = AuthorizationSetupException.class)
	public void testEmptyTenantException() throws AuthorizationSetupException {
		setupService.addNewTenant(OaccTestConstant.TEST_EMPTY);
	}

	*//**
	 * Test case to create new resource class.
	 *//*
	// @Test
	public void testCreateResourceClass() throws AuthorizationSetupException {
		setupService.addNewResourceTypeToAuthorize(OaccTestConstant.TEST_RESRC_CLASS_USER, permissionList);
	}

	*//**
	 * Test case to create resource class which will throw exception.
	 *//*
	@Test(expected = AuthorizationSetupException.class)
	public void testExistingResourceClassException() throws AuthorizationSetupException {
		setupService.addNewResourceTypeToAuthorize(OaccTestConstant.TEST_RESRC_CLASS_USER, permissionList);
	}

	@Test(expected = AuthorizationSetupException.class)
	public void testNullResourceClassException() throws AuthorizationSetupException {
		setupService.addNewResourceTypeToAuthorize(null, permissionList);
	}

	@Test(expected = AuthorizationSetupException.class)
	public void testEmptyResourceClassException() throws AuthorizationSetupException {
		setupService.addNewResourceTypeToAuthorize(OaccTestConstant.TEST_EMPTY, permissionList);
	}

	*//**
	 * Test case to add new permission on resource type
	 * 
	 * @throws AuthorizationSetupException
	 *//*
	// @Test
	public void addNewPermissionOnExistingResourceType() throws AuthorizationSetupException {
		setupService.addNewPermissionsOnResourceType(Arrays.asList("Fetch", "Modify"), "Users");
	}

	*//**
	 * Test case to create new role resource class.
	 * 
	 * @throws AuthorizationSetupException
	 *//*
	// @Test
	public void testCreateRolesRC() throws AuthorizationSetupException {
		setupService.addNewResourceTypeToAuthorize(OaccTestConstant.TEST_ROLE_RESRC_CLASS, permissionList);
	}

	*//**
	 * Test case to create list of roles.
	 * 
	 * @throws InvalidConfiguration
	 *//*
	// @Test
	public void testCreateRoles() throws ResourceRegistryException, InvalidConfiguration {
		List<String> roleList = new ArrayList<>();
		roleList.add(OaccTestConstant.TEST_ROLE_JAVA_DEVELOPER);
		roleList.add(OaccTestConstant.TEST_ROLE_SF_DEVELOPER);
		setupService.registerRoles(roleList, OaccTestConstant.TEST_TENANT);
	}

	*//**
	 * Test case to create existing role which will throw exception.
	 * 
	 * @throws ResourceRegistryException
	 *//*
	@Test(expected = ResourceRegistryException.class)
	public void testCreateEmptyRoles() throws ResourceRegistryException, InvalidConfiguration {
		List<String> roleList = new ArrayList<>();
		roleList.add(OaccTestConstant.TEST_ROLE_JAVA_DEVELOPER);
		roleList.add(OaccTestConstant.TEST_ROLE_SF_DEVELOPER);
		setupService.registerRoles(roleList, OaccTestConstant.TEST_TENANT);
	}

	*//**
	 * Test case to create single role.
	 * 
	 * @throws ResourceRegistryException
	 *//*
	// @Test
	public void testCreateRole() throws ResourceRegistryException, InvalidConfiguration {
		setupService.registerRole(OaccTestConstant.TEST_ROLE_UI_DEVELOPER, OaccTestConstant.TEST_TENANT);
	}

	*//**
	 * Test case to create role for existing, null, empty role.
	 * 
	 * @throws ResourceRegistryException
	 *//*
	@Test(expected = ResourceRegistryException.class)
	public void testExistingRolesException() throws ResourceRegistryException, InvalidConfiguration {
		setupService.registerRole(OaccTestConstant.TEST_ROLE_UI_DEVELOPER, OaccTestConstant.TEST_TENANT);
	}

	@Test(expected = ResourceRegistryException.class)
	public void testNullRoleException() throws ResourceRegistryException, InvalidConfiguration {
		setupService.registerRole(null, OaccTestConstant.TEST_TENANT);
	}

	@Test(expected = ResourceRegistryException.class)
	public void testEmptyRoleException() throws ResourceRegistryException, InvalidConfiguration {
		setupService.registerRole(OaccTestConstant.TEST_EMPTY, OaccTestConstant.TEST_TENANT);
	}

	*//**
	 * Test case to create new items.
	 * 
	 * @throws AuthorizationSetupException
	 *//*
	// @Test
	public void testCreateResourceClassForItem() throws AuthorizationSetupException {
		setupService.addNewResourceTypeToAuthorize(OaccTestConstant.TEST_RESRC_CLASS_ITEM, permissionList);
	}

	// @Test
	public void testCreateallItems() throws IOException, ResourceRegistryException, InvalidConfiguration {
		String[] itemsToBeadded = properties.getProperty("menu").split(",");
		for (String item : itemsToBeadded) {
			logger.debug(item);
		}
		List<String> addables = Arrays.asList(itemsToBeadded);
		setupService.addNewResources(addables, OaccTestConstant.TEST_RESRC_CLASS_ITEM, OaccTestConstant.TEST_TENANT);
	}

	@Test(expected = ResourceRegistryException.class)
	public void testAlreadyExistItemsException() throws IOException, ResourceRegistryException, InvalidConfiguration {
		String[] itemsToBeadded = properties.getProperty("menu").split(",");
		List<String> addables = Arrays.asList(itemsToBeadded);
		setupService.addNewResources(addables, OaccTestConstant.TEST_RESRC_CLASS_ITEM, OaccTestConstant.TEST_TENANT);
	}

	// @Test
	public void testCreateSingleItem() throws ResourceRegistryException, InvalidConfiguration {
		setupService.addNewResource("Mobile", OaccTestConstant.TEST_RESRC_CLASS_ITEM, OaccTestConstant.TEST_TENANT);
	}

	*//**
	 * Test case to get permissions for resource class.
	 * 
	 * @throws PermissionAccessException
	 *//*
	@Test
	public void testPermissionsForResourcecClass() throws PermissionAccessException {
		List<String> permission = setupService.getAllPermissions(OaccTestConstant.TEST_RESRC_CLASS_USER);
		Assert.assertArrayEquals(
				new String[] { "Delete", "Edit", "Fetch", "Modify", "View", "*INHERIT", "*DELETE", "*QUERY" },
				permission.toArray());
		logger.debug("permission for resource class:" + permission);
	}

	*//**
	 * Test case to get permissions for user on any specific resource.
	 * 
	 * @throws PermissionAccessException
	 *//*
	@Test
	public void testGetAllUserPermissionsOnResource() throws PermissionAccessException {
		List<String> permissionList = setupService.getAllUserPermissionsOnResource(OaccTestConstant.TEST_USER_KIM,
				OaccTestConstant.TEST_ITME_1, "");
		List<String> expectedPermission = new ArrayList<>();
		expectedPermission.add("Delete");
		expectedPermission.add("Edit");
		expectedPermission.add("View");
		Assert.assertTrue(permissionList.containsAll(expectedPermission));
		logger.debug("permission for user kim on resource item1:" + permissionList);
	}*/
}