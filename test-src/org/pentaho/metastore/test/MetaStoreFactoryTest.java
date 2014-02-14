package org.pentaho.metastore.test;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.pentaho.metastore.test.testclasses.MyElement;
import org.pentaho.metastore.util.MetaStoreUtil;

public class MetaStoreFactoryTest extends TestCase {

  public void testMyElement() throws Exception {

    String NAME = "one";
    String ATTR = "11111111";
    String ANOTHER = "2222222";
    int INT = 3;
    long LONG = 4;
    boolean BOOL = true;
    Date DATE = new Date();

    MyElement me = new MyElement( NAME, ATTR, ANOTHER, INT, LONG, BOOL, DATE );

    IMetaStore metaStore = new MemoryMetaStore();

    MetaStoreFactory<MyElement> factory = new MetaStoreFactory<MyElement>( MyElement.class, metaStore, "custom" );
    factory.saveElement( me );

    MyElement verify = factory.loadElement( NAME );
    assertNotNull( verify );
    assertEquals( ATTR, verify.getMyAttribute() );
    assertEquals( ANOTHER, verify.getAnotherAttribute() );
    assertEquals( INT, verify.getIntAttribute() );
    assertEquals( LONG, verify.getLongAttribute() );
    assertEquals( BOOL, verify.isBoolAttribute() );
    assertEquals( DATE, verify.getDateAttribute() );

    // verify the details...
    //
    assertTrue( metaStore.namespaceExists( "custom" ) );
    IMetaStoreElementType elementType = metaStore.getElementTypeByName( "custom", "My element type" );
    assertNotNull( elementType );
    assertEquals( "This is my element type", elementType.getDescription() );

    IMetaStoreElement element = metaStore.getElementByName( "custom", elementType, NAME );
    assertNotNull( element );
    IMetaStoreAttribute child = element.getChild( "my_attribute" );
    assertNotNull( child );
    assertEquals( ATTR, MetaStoreUtil.getAttributeString( child ) );

    child = element.getChild( "anotherAttribute" );
    assertNotNull( child );
    assertEquals( ANOTHER, MetaStoreUtil.getAttributeString( child ) );

    List<String> names = factory.getElementNames();
    assertEquals( 1, names.size() );
    assertEquals( NAME, names.get( 0 ) );
  }
}
