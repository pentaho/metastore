package org.pentaho.metastore.test;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;
import org.pentaho.metastore.api.security.MetaStoreObjectPermission;
import org.pentaho.metastore.api.security.MetaStoreOwnerPermissions;

public class BaseMetaStoreTest extends TestCase {

  // Namespace: Pentaho
  //
  protected static final String NS_PENTAHO = "Pentaho";

  // Element type: Shared Dimension
  //
  protected static final String SHARED_DIMENSION_ID = "SharedDimensionId";
  protected static final String SHARED_DIMENSION_NAME = "Shared Dimension";
  protected static final String SHARED_DIMENSION_DESCRIPTION = "Star modeler shared dimension";
  
  // Element: customer dimension
  //
  protected static final String CUSTOMER_DIMENSION_ID = "CustomerDimensionId";
  protected static final String CUSTOMER_DIMENSION_NAME = "Customer dimension";
  
  public void testNothing() throws Exception {
  }
  
  protected void testFunctionality(IMetaStore metaStore) throws MetaStoreException {
    metaStore.createNamespace(NS_PENTAHO);
    List<String> namespaces = metaStore.getNamespaces();
    assertEquals(1, namespaces.size());
    
    IMetaStoreElementType elementType = metaStore.newElementType(NS_PENTAHO);
    elementType.setId(SHARED_DIMENSION_ID);
    elementType.setName(SHARED_DIMENSION_NAME);
    elementType.setDescription(SHARED_DIMENSION_DESCRIPTION);
    metaStore.createElementType(NS_PENTAHO, elementType);
    
    List<IMetaStoreElementType> elementTypes = metaStore.getElementTypes(NS_PENTAHO);
    assertEquals(1, elementTypes.size());
    
    try {
      metaStore.createElementType(NS_PENTAHO, elementType);
      fail("Duplicate creation error expected!");
    } catch(MetaStoreElementTypeExistsException e) {
      // OK!
    } catch(MetaStoreException e) {
      fail("Create exception needs to be MetaStoreDataTypesExistException");
    }
    
    // Try to delete the namespace, should error out
    //
    try {
      metaStore.deleteNamespace(NS_PENTAHO);
      fail("Expected error while deleting namespace with content!");
    } catch(MetaStoreDependenciesExistsException e) {
      // OK!
      List<String> dependencies = e.getDependencies();
      assertNotNull(dependencies);
      assertEquals(1, dependencies.size());
      assertEquals(SHARED_DIMENSION_ID, dependencies.get(0));
    }
    
    IMetaStoreElement customerDimension = generateCustomerDimensionElement(metaStore, NS_PENTAHO, CUSTOMER_DIMENSION_ID);
    IMetaStoreElementOwner elementOwner = customerDimension.getOwner();
    assertNotNull(elementOwner);
    assertEquals("joe", elementOwner.getName());
    assertEquals(MetaStoreElementOwnerType.USER, elementOwner.getOwnerType());
    
    metaStore.createElement(NS_PENTAHO, SHARED_DIMENSION_ID, customerDimension);
    List<IMetaStoreElement> elements = metaStore.getElements(NS_PENTAHO, SHARED_DIMENSION_ID);
    assertEquals(1, elements.size());
    assertNotNull(elements.get(0));
    assertEquals(CUSTOMER_DIMENSION_ID, elements.get(0).getId());
    assertEquals(CUSTOMER_DIMENSION_NAME, elements.get(0).getName());
    
    // Try to delete the data type, should error out
    //
    try {
      metaStore.deleteElementType(NS_PENTAHO, SHARED_DIMENSION_ID);
      fail("Expected error while deleting data type with content!");
    } catch(MetaStoreDependenciesExistsException e) {
      // OK!
      List<String> dependencies = e.getDependencies();
      assertNotNull(dependencies);
      assertEquals(1, dependencies.size());
      assertEquals(CUSTOMER_DIMENSION_ID, dependencies.get(0));
    }
    
    // Some lookup-by-name tests...
    //
    assertNotNull(metaStore.getElementTypeByName(NS_PENTAHO, SHARED_DIMENSION_NAME));
    assertNotNull(metaStore.getElementByName(NS_PENTAHO, elementType, CUSTOMER_DIMENSION_NAME));
    
    // Clean up shop!
    //
    metaStore.deleteElement(NS_PENTAHO, SHARED_DIMENSION_ID, CUSTOMER_DIMENSION_ID);
    elements = metaStore.getElements(NS_PENTAHO, SHARED_DIMENSION_ID);
    assertEquals(0, elements.size());
    
    metaStore.deleteElementType(NS_PENTAHO, elementType.getId());
    elementTypes = metaStore.getElementTypes(NS_PENTAHO);
    assertEquals(0, elementTypes.size());
    
    metaStore.deleteNamespace(NS_PENTAHO);
    namespaces = metaStore.getNamespaces();
    assertEquals(0, namespaces.size());
  }

  private IMetaStoreElement generateCustomerDimensionElement(IMetaStore metaStore, String nsPentaho, String dtSharedDimension) throws MetaStoreException {
    IMetaStoreElement element = metaStore.newElement();
    element.setId(CUSTOMER_DIMENSION_ID);
    element.setName(CUSTOMER_DIMENSION_NAME);
    
    element.addChild(metaStore.newElement("description", "This is the shared customer dimension"));
    element.addChild(metaStore.newElement("physical_table", "DIM_CUSTOMER"));
    IMetaStoreElement fieldsElement = metaStore.newElement("fields", null);
    element.addChild(fieldsElement);
  
    // A technical key
    //
    IMetaStoreElement fieldElement = metaStore.newElement("field_0", null);
    fieldsElement.addChild(fieldElement);
    fieldElement.addChild(metaStore.newElement("field_name", "Customer TK"));
    fieldElement.addChild(metaStore.newElement("field_description", "Customer Technical key"));
    fieldElement.addChild(metaStore.newElement("field_phyiscal_name", "customer_tk"));
    fieldElement.addChild(metaStore.newElement("field_kettle_type", "Integer"));
  
    // A version field
    //
    fieldElement = metaStore.newElement("field_1", null);
    fieldsElement.addChild(fieldElement);
    fieldElement.addChild(metaStore.newElement("field_name", "version field"));
    fieldElement.addChild(metaStore.newElement("field_description", "dimension version field (1..N)"));
    fieldElement.addChild(metaStore.newElement("field_phyiscal_name", "version"));
    fieldElement.addChild(metaStore.newElement("field_kettle_type", "Integer"));
  
    // Natural key
    //
    fieldElement = metaStore.newElement("field_2", null);
    fieldsElement.addChild(fieldElement);
    fieldElement.addChild(metaStore.newElement("field_name", "Customer ID"));
    fieldElement.addChild(metaStore.newElement("field_description", "Customer ID as a natural key of this dimension"));
    fieldElement.addChild(metaStore.newElement("field_phyiscal_name", "customer_id"));
    fieldElement.addChild(metaStore.newElement("field_kettle_type", "Integer"));
  
    // Start date
    //
    fieldElement = metaStore.newElement("field_3", null);
    fieldsElement.addChild(fieldElement);
    fieldElement.addChild(metaStore.newElement("field_name", "Start date"));
    fieldElement.addChild(metaStore.newElement("field_description", "Start of validity of this dimension entry"));
    fieldElement.addChild(metaStore.newElement("field_phyiscal_name", "start_date"));
    fieldElement.addChild(metaStore.newElement("field_kettle_type", "Date"));
  
    // End date
    //
    fieldElement = metaStore.newElement("field_4", null);
    fieldsElement.addChild(fieldElement);
    fieldElement.addChild(metaStore.newElement("field_name", "End date"));
    fieldElement.addChild(metaStore.newElement("field_description", "End of validity of this dimension entry"));
    fieldElement.addChild(metaStore.newElement("field_phyiscal_name", "end_date"));
    fieldElement.addChild(metaStore.newElement("field_kettle_type", "Date"));
    
    // A few columns...
    //
    for (int i=5;i<=10;i++) {
      fieldElement = metaStore.newElement("field_"+i, null);
      fieldsElement.addChild(fieldElement);
      fieldElement.addChild(metaStore.newElement("field_name", "Field name "+i));
      fieldElement.addChild(metaStore.newElement("field_description", "Field description "+i));
      fieldElement.addChild(metaStore.newElement("field_phyiscal_name", "physical_name_"+i));
      fieldElement.addChild(metaStore.newElement("field_kettle_type", "String"));
    }
    
    // Some security
    //
    element.setOwner(metaStore.newElementOwner("joe", MetaStoreElementOwnerType.USER));
    
    // The "users" role has read/write permissions
    //
    IMetaStoreElementOwner usersRole = metaStore.newElementOwner("users", MetaStoreElementOwnerType.ROLE);
    MetaStoreOwnerPermissions usersRoleOwnerPermissions = new MetaStoreOwnerPermissions(usersRole, MetaStoreObjectPermission.READ, MetaStoreObjectPermission.UPDATE);
    element.getOwnerPermissionsList().add( usersRoleOwnerPermissions );
    
    return element;
  }
}
