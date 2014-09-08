package org.pentaho.metastore.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.persist.IMetaStoreObjectFactory;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.pentaho.metastore.test.testclasses.cube.Cube;
import org.pentaho.metastore.test.testclasses.cube.Dimension;
import org.pentaho.metastore.test.testclasses.cube.DimensionAttribute;
import org.pentaho.metastore.test.testclasses.cube.DimensionType;
import org.pentaho.metastore.test.testclasses.cube.Kpi;
import org.pentaho.metastore.test.testclasses.my.MyElement;
import org.pentaho.metastore.test.testclasses.my.MyElementAttr;
import org.pentaho.metastore.test.testclasses.my.MyFilenameElement;
import org.pentaho.metastore.test.testclasses.my.MyNameElement;
import org.pentaho.metastore.test.testclasses.my.MyOtherElement;
import org.pentaho.metastore.util.MetaStoreUtil;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetaStoreFactoryTest extends TestCase {

  public void testMyElement() throws Exception {

    IMetaStore metaStore = new MemoryMetaStore();

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
    List<String> stringList = Arrays.asList( "a", "b", "c", "d" );
    me.setStringList( stringList );
    MyOtherElement myOtherElement = new MyOtherElement( "other", "other attribute" );
    me.setMyOtherElement( myOtherElement );

    MetaStoreFactory<MyOtherElement> otherFactory = new MetaStoreFactory<MyOtherElement>( MyOtherElement.class, metaStore, "custom" );
    MetaStoreFactory<MyElement> factory = new MetaStoreFactory<MyElement>( MyElement.class, metaStore, "custom" );

    // For loading, specify the name, filename lists or factory that we're referencing...
    //
    factory.addNameList( MyElement.LIST_KEY_MY_NAMES, nameList );
    factory.addFilenameList( MyElement.LIST_KEY_MY_FILENAMES, filenameList );
    factory.addNameFactory( MyElement.FACTORY_OTHER_ELEMENT, otherFactory );

    // Store the class in the meta store
    //
    factory.saveElement( me );

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

    // Verify the referenced MyOtherElement
    //
    MyOtherElement verifyOtherElement = verify.getMyOtherElement();
    assertNotNull( verifyOtherElement );
    assertEquals( myOtherElement.getName(), verifyOtherElement.getName() );
    assertEquals( myOtherElement.getSomeAttribute(), verifyOtherElement.getSomeAttribute() );

    // verify that the String list is loaded...
    List<String> verifyList = verify.getStringList();
    assertEquals( stringList.size(), verifyList.size() );
    for ( int i = 0; i < stringList.size(); i++ ) {
      assertEquals( stringList.get( i ), verifyList.get( i ) );
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

  public void testCube() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<Cube> factoryCube = new MetaStoreFactory<Cube>( Cube.class, metaStore, "pentaho" );
    MetaStoreFactory<Dimension> factoryDimension = new MetaStoreFactory<Dimension>( Dimension.class, metaStore, "pentaho" );
    factoryCube.addNameFactory( Cube.DIMENSION_FACTORY_KEY, factoryDimension );
    IMetaStoreObjectFactory objectFactory = mock( IMetaStoreObjectFactory.class );
    factoryCube.setObjectFactory( objectFactory );
    factoryDimension.setObjectFactory( objectFactory );

    final AtomicInteger contextCount = new AtomicInteger( 0 );
    when( objectFactory.getContext( anyObject() ) ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        Map context = new HashMap();
        context.put( "context-num", String.valueOf( contextCount.getAndIncrement() ) );
        return context;
      }
    } );
    when( objectFactory.instantiateClass( anyString(), anyMap() ) ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        String className = (String) invocation.getArguments()[0];
        return Class.forName( className ).newInstance();
      }
    } );

    Cube cube = generateCube();
    factoryCube.saveElement( cube );

    // Now load back and verify...
    Cube verify = factoryCube.loadElement( cube.getName() );

    assertEquals( cube.getName(), verify.getName() );
    assertEquals( cube.getDimensions().size(), verify.getDimensions().size() );
    for ( int i = 0; i < cube.getDimensions().size(); i++ ) {
      Dimension dimension = cube.getDimensions().get( i );
      Dimension verifyDimension = verify.getDimensions().get( i );
      assertEquals( dimension.getName(), verifyDimension.getName() );
      assertEquals( dimension.getDimensionType(), verifyDimension.getDimensionType() );
      assertEquals( dimension.getAttributes().size(), verifyDimension.getAttributes().size() );
      for ( int x = 0; x < dimension.getAttributes().size(); x++ ) {
        DimensionAttribute attr = dimension.getAttributes().get( i );
        DimensionAttribute attrVerify = verifyDimension.getAttributes().get( i );
        assertEquals( attr.getName(), attrVerify.getName() );
        assertEquals( attr.getDescription(), attrVerify.getDescription() );
        assertEquals( attr.getSomeOtherStuff(), attrVerify.getSomeOtherStuff() );
      }
    }

    assertEquals( cube.getKpis().size(), verify.getKpis().size() );
    for ( int i = 0; i < cube.getKpis().size(); i++ ) {
      Kpi kpi = cube.getKpis().get( i );
      Kpi verifyKpi = verify.getKpis().get( i );
      assertEquals( kpi.getName(), verifyKpi.getName() );
      assertEquals( kpi.getDescription(), verifyKpi.getDescription() );
      assertEquals( kpi.getOtherDetails(), verifyKpi.getOtherDetails() );
    }

    assertNotNull( verify.getJunkDimension() );
    Dimension junk = cube.getJunkDimension();
    Dimension junkVerify = verify.getJunkDimension();
    assertEquals( junk.getName(), junkVerify.getName() );
    assertEquals( junk.getAttributes().size(), junkVerify.getAttributes().size() );
    for ( int i = 0; i < junk.getAttributes().size(); i++ ) {
      DimensionAttribute attr = junk.getAttributes().get( i );
      DimensionAttribute attrVerify = junkVerify.getAttributes().get( i );
      assertEquals( attr.getName(), attrVerify.getName() );
      assertEquals( attr.getDescription(), attrVerify.getDescription() );
      assertEquals( attr.getSomeOtherStuff(), attrVerify.getSomeOtherStuff() );
    }

    assertNotNull( verify.getMainKpi() );
    assertEquals( cube.getMainKpi().getName(), verify.getMainKpi().getName() );
    assertEquals( cube.getMainKpi().getDescription(), verify.getMainKpi().getDescription() );
    assertEquals( cube.getMainKpi().getOtherDetails(), verify.getMainKpi().getOtherDetails() );

    for ( int i = 0; i < contextCount.get(); i++ ) {
      Map context = new HashMap();
      context.put( "context-num", String.valueOf( i ) );
      verify( objectFactory ).instantiateClass( anyString(), eq( context ) );
    }

  }

  private Cube generateCube() {
    Cube cube = new Cube();
    cube.setName( "Fact" );

    Dimension customer = new Dimension();
    customer.setName( "customer" );
    customer.setAttributes( generateAttributes() );
    customer.setDimensionType( DimensionType.SCD );
    cube.getDimensions().add( customer );

    Dimension product = new Dimension();
    product.setName( "product" );
    product.setAttributes( generateAttributes() );
    product.setDimensionType( null );
    cube.getDimensions().add( product );

    Dimension date = new Dimension();
    date.setName( "date" );
    date.setAttributes( generateAttributes() );
    date.setDimensionType( DimensionType.DATE );
    cube.getDimensions().add( date );

    Dimension junk = new Dimension();
    junk.setName( "junk" );
    junk.setAttributes( generateAttributes() );
    junk.setDimensionType( DimensionType.JUNK );
    cube.setJunkDimension( junk );

    cube.setKpis( generateKpis() );

    Kpi mainKpi = new Kpi();
    mainKpi.setName( "mainKpi-name" );
    mainKpi.setDescription( "mainKpi-description" );
    mainKpi.setOtherDetails( "mainKpi-otherDetails" );
    cube.setMainKpi( mainKpi );

    return cube;
  }

  private List<Kpi> generateKpis() {
    List<Kpi> list = new ArrayList<Kpi>();
    for ( int i = 0; i < 5; i++ ) {
      Kpi kpi = new Kpi();
      kpi.setName( "kpi-" + ( i + 1 ) );
      kpi.setDescription( "desc-" + ( i + 1 ) );
      kpi.setOtherDetails( "othd-" + ( i + 1 ) );
    }
    return list;
  }

  private List<DimensionAttribute> generateAttributes() {
    List<DimensionAttribute> list = new ArrayList<DimensionAttribute>();
    for ( int i = 0; i < 10; i++ ) {
      DimensionAttribute attribute = new DimensionAttribute();
      attribute.setName( "attr-" + ( i + 1 ) );
      attribute.setDescription( "desc-" + ( i + 1 ) );
      attribute.setSomeOtherStuff( "other" + ( i + 1 ) );
      list.add( attribute );
    }
    return list;
  }

}
