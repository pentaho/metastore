package org.pentaho.metastore.test;

import java.util.ArrayList;
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
import org.pentaho.metastore.test.testclasses.MyElementAttr;
import org.pentaho.metastore.test.testclasses.MyFilenameElement;
import org.pentaho.metastore.test.testclasses.MyNameElement;
import org.pentaho.metastore.util.MetaStoreUtil;

public class MetaStoreFactoryTest extends TestCase {

  public void testMyElement() throws Exception {

    String NAME = "one";
    String ATTR = "11111111";
    String ANOTHER = "2222222";
    String PASSWORD = "my secret password";
    int INT = 3;
    long LONG = 4;
    boolean BOOL = true;
    Date DATE = new Date();
    final int NR_ATTR = 10;
    final int NR_NAME = 5;
    final int NR_FILENAME = 5;

    // List of named elements...
    //
    List<MyNameElement> nameList = new ArrayList<MyNameElement>();
    for ( int i = 0; i < NR_NAME; i++ ) {
      nameList.add( new MyNameElement( "name" + i, "description" + i, "color" + i ) );
    }
    List<MyFilenameElement> filenameList = new ArrayList<MyFilenameElement>();
    for ( int i = 0; i < NR_FILENAME; i++ ) {
      filenameList.add( new MyFilenameElement( "filename" + i, "size" + i, "gender" + i ) );
    }

    // Construct our test element...
    //
    MyElement me = new MyElement( NAME, ATTR, ANOTHER, PASSWORD, INT, LONG, BOOL, DATE );
    for ( int i = 0; i < NR_ATTR; i++ ) {
      me.getSubAttributes().add( new MyElementAttr( "key" + i, "value" + i, "desc" + i ) );
    }
    me.setNameElement( nameList.get( NR_NAME - 1 ) );
    me.setFilenameElement( filenameList.get( NR_FILENAME - 1 ) );

    IMetaStore metaStore = new MemoryMetaStore();

    MetaStoreFactory<MyElement> factory = new MetaStoreFactory<MyElement>( MyElement.class, metaStore, "custom" );

    // Store the class in the meta store
    //
    factory.saveElement( me );

    // For loading, specify the name and filename lists we're referencing...
    //
    factory.addNameList( MyElement.LIST_KEY_MY_NAMES, nameList );
    factory.addFilenameList( MyElement.LIST_KEY_MY_FILENAMES, filenameList );

    // Load the class from the meta store
    //
    MyElement verify = factory.loadElement( NAME );

    // Verify list element details...
    //
    IMetaStoreElement element = metaStore.getElementByName( "custom", factory.getElementType(), NAME );
    assertNotNull( element );

    // Verify the general idea
    //
    assertNotNull( verify );
    assertEquals( ATTR, verify.getMyAttribute() );
    assertEquals( ANOTHER, verify.getAnotherAttribute() );
    assertEquals( PASSWORD, verify.getPasswordAttribute() );
    assertEquals( INT, verify.getIntAttribute() );
    assertEquals( LONG, verify.getLongAttribute() );
    assertEquals( BOOL, verify.isBoolAttribute() );
    assertEquals( DATE, verify.getDateAttribute() );
    assertEquals( me.getSubAttributes().size(), verify.getSubAttributes().size() );
    assertEquals( me.getNameElement(), verify.getNameElement() );
    assertEquals( me.getFilenameElement(), verify.getFilenameElement() );

    // verify the details...
    //
    assertTrue( metaStore.namespaceExists( "custom" ) );
    IMetaStoreElementType elementType = factory.getElementType();
    assertNotNull( elementType );
    assertEquals( "My element type", elementType.getName() );
    assertEquals( "This is my element type", elementType.getDescription() );

    assertNotNull( element );
    IMetaStoreAttribute child = element.getChild( "my_attribute" );
    assertNotNull( child );
    assertEquals( ATTR, MetaStoreUtil.getAttributeString( child ) );
    child = element.getChild( "passwordAttribute" );
    assertNotNull( child );
    assertNotSame( "Password needs to be encoded", PASSWORD, MetaStoreUtil.getAttributeString( child ) );

    child = element.getChild( "anotherAttribute" );
    assertNotNull( child );
    assertEquals( ANOTHER, MetaStoreUtil.getAttributeString( child ) );

    // Verify the child attributes as well...
    // This also verifies that the attributes are in the right order.
    // The list can't be re-ordered after loading.
    //
    for ( int i = 0; i < NR_ATTR; i++ ) {
      MyElementAttr attr = verify.getSubAttributes().get( i );
      assertEquals( "key" + i, attr.getKey() );
      assertEquals( "value" + i, attr.getValue() );
      assertEquals( "desc" + i, attr.getDescription() );
    }

    List<String> names = factory.getElementNames();
    assertEquals( 1, names.size() );
    assertEquals( NAME, names.get( 0 ) );

    List<MyElement> list = factory.getElements();
    assertEquals( 1, list.size() );
    assertEquals( NAME, list.get( 0 ).getName() );

    factory.deleteElement( NAME );
    assertEquals( 0, factory.getElementNames().size() );
    assertEquals( 0, factory.getElements().size() );
  }
}
