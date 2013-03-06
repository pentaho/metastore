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

  protected static final String ET_CUSTOMER_DIMENSION_ID = "a1c3-2341-fe1a-3442-1234";
  protected static final String DT_SHARED_DIMENSION_ID = "SharedDimension";
  protected static final String NS_PENTAHO = "Pentaho";
  
  protected void testFunctionality(IMetaStore metaStore) throws MetaStoreException {
    metaStore.createNamespace(NS_PENTAHO);
    List<String> namespaces = metaStore.getNamespaces();
    assertEquals(1, namespaces.size());
    
    IMetaStoreElementType dataType = metaStore.newElementType(NS_PENTAHO);
    dataType.setId(DT_SHARED_DIMENSION_ID);
    dataType.setName("Shared dimension");
    dataType.setDescription("Star modeler shared dimension");
    metaStore.createElementType(NS_PENTAHO, dataType);
    
    List<IMetaStoreElementType> dataTypes = metaStore.getElementTypes(NS_PENTAHO);
    assertEquals(1, dataTypes.size());
    
    try {
      metaStore.createElementType(NS_PENTAHO, dataType);
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
      assertEquals(DT_SHARED_DIMENSION_ID, dependencies.get(0));
    }
    
    IMetaStoreElement customerDimension = generateCustomerDimensionElement(metaStore, NS_PENTAHO, DT_SHARED_DIMENSION_ID);
    IMetaStoreElementOwner elementOwner = customerDimension.getOwner();
    assertNotNull(elementOwner);
    assertEquals("joe", elementOwner.getName());
    assertEquals(MetaStoreElementOwnerType.USER, elementOwner.getOwnerType());
    
    metaStore.createElement(NS_PENTAHO, DT_SHARED_DIMENSION_ID, customerDimension);
    List<IMetaStoreElement> entities = metaStore.getElements(NS_PENTAHO, DT_SHARED_DIMENSION_ID);
    assertEquals(1, entities.size());
    assertNotNull(entities.get(0));
    assertEquals(ET_CUSTOMER_DIMENSION_ID, entities.get(0).getId());
    
    // Try to delete the data type, should error out
    //
    try {
      metaStore.deleteElementType(NS_PENTAHO, DT_SHARED_DIMENSION_ID);
      fail("Expected error while deleting data type with content!");
    } catch(MetaStoreDependenciesExistsException e) {
      // OK!
      List<String> dependencies = e.getDependencies();
      assertNotNull(dependencies);
      assertEquals(1, dependencies.size());
      assertEquals(ET_CUSTOMER_DIMENSION_ID, dependencies.get(0));
    }
    
    // Clean up shop!
    //
    metaStore.deleteElement(NS_PENTAHO, DT_SHARED_DIMENSION_ID, ET_CUSTOMER_DIMENSION_ID);
    entities = metaStore.getElements(NS_PENTAHO, DT_SHARED_DIMENSION_ID);
    assertEquals(0, entities.size());
    
    metaStore.deleteElementType(NS_PENTAHO, dataType.getId());
    dataTypes = metaStore.getElementTypes(NS_PENTAHO);
    assertEquals(0, dataTypes.size());
    
    metaStore.deleteNamespace(NS_PENTAHO);
    namespaces = metaStore.getNamespaces();
    assertEquals(0, namespaces.size());
  }

  private IMetaStoreElement generateCustomerDimensionElement(IMetaStore metaStore, String nsPentaho, String dtSharedDimension) throws MetaStoreException {
    IMetaStoreElement element = metaStore.newElement();
    element.setId(ET_CUSTOMER_DIMENSION_ID);
    element.setValue("Top element");
    
    element.addChild(metaStore.newElement("name", "Customer dimension"));
    element.addChild(metaStore.newElement("description", "This is the shared customer dimension"));
    element.addChild(metaStore.newElement("physical_table", "DIM_CUSTOMER"));
    IMetaStoreElement fieldsEntity = metaStore.newElement("fields", null);
    element.addChild(fieldsEntity);
  
    // A technical key
    //
    IMetaStoreElement fieldEntity = metaStore.newElement("field_0", null);
    fieldsEntity.addChild(fieldEntity);
    fieldEntity.addChild(metaStore.newElement("field_name", "Customer TK"));
    fieldEntity.addChild(metaStore.newElement("field_description", "Customer Technical key"));
    fieldEntity.addChild(metaStore.newElement("field_phyiscal_name", "customer_tk"));
    fieldEntity.addChild(metaStore.newElement("field_kettle_type", "Integer"));
  
    // A version field
    //
    fieldEntity = metaStore.newElement("field_1", null);
    fieldsEntity.addChild(fieldEntity);
    fieldEntity.addChild(metaStore.newElement("field_name", "version field"));
    fieldEntity.addChild(metaStore.newElement("field_description", "dimension version field (1..N)"));
    fieldEntity.addChild(metaStore.newElement("field_phyiscal_name", "version"));
    fieldEntity.addChild(metaStore.newElement("field_kettle_type", "Integer"));
  
    // Natural key
    //
    fieldEntity = metaStore.newElement("field_2", null);
    fieldsEntity.addChild(fieldEntity);
    fieldEntity.addChild(metaStore.newElement("field_name", "Customer ID"));
    fieldEntity.addChild(metaStore.newElement("field_description", "Customer ID as a natural key of this dimension"));
    fieldEntity.addChild(metaStore.newElement("field_phyiscal_name", "customer_id"));
    fieldEntity.addChild(metaStore.newElement("field_kettle_type", "Integer"));
  
    // Start date
    //
    fieldEntity = metaStore.newElement("field_3", null);
    fieldsEntity.addChild(fieldEntity);
    fieldEntity.addChild(metaStore.newElement("field_name", "Start date"));
    fieldEntity.addChild(metaStore.newElement("field_description", "Start of validity of this dimension entry"));
    fieldEntity.addChild(metaStore.newElement("field_phyiscal_name", "start_date"));
    fieldEntity.addChild(metaStore.newElement("field_kettle_type", "Date"));
  
    // End date
    //
    fieldEntity = metaStore.newElement("field_4", null);
    fieldsEntity.addChild(fieldEntity);
    fieldEntity.addChild(metaStore.newElement("field_name", "End date"));
    fieldEntity.addChild(metaStore.newElement("field_description", "End of validity of this dimension entry"));
    fieldEntity.addChild(metaStore.newElement("field_phyiscal_name", "end_date"));
    fieldEntity.addChild(metaStore.newElement("field_kettle_type", "Date"));
    
    // A few columns...
    //
    for (int i=5;i<=10;i++) {
      fieldEntity = metaStore.newElement("field_"+i, null);
      fieldsEntity.addChild(fieldEntity);
      fieldEntity.addChild(metaStore.newElement("field_name", "Field name "+i));
      fieldEntity.addChild(metaStore.newElement("field_description", "Field description "+i));
      fieldEntity.addChild(metaStore.newElement("field_phyiscal_name", "physical_name_"+i));
      fieldEntity.addChild(metaStore.newElement("field_kettle_type", "String"));
    }
    
    // Some security
    //
    element.setOwner(metaStore.newElementOwner("joe", MetaStoreElementOwnerType.USER));
    
    // The users role has read/write permissions
    //
    IMetaStoreElementOwner usersRole = metaStore.newElementOwner("users", MetaStoreElementOwnerType.ROLE);
    MetaStoreOwnerPermissions usersRoleOwnerPermissions = new MetaStoreOwnerPermissions(usersRole, MetaStoreObjectPermission.READ, MetaStoreObjectPermission.UPDATE);
    element.getOwnerPermissionsList().add( usersRoleOwnerPermissions );
    
    return element;
  }

}
