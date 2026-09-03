/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.metastore.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.IMetaStoreObjectFactory;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.pentaho.metastore.test.testclasses.cube.Cube;
import org.pentaho.metastore.test.testclasses.cube.Dimension;
import org.pentaho.metastore.test.testclasses.cube.DimensionAttribute;
import org.pentaho.metastore.test.testclasses.cube.DimensionType;
import org.pentaho.metastore.test.testclasses.cube.Kpi;
import org.pentaho.metastore.test.testclasses.factory.A;
import org.pentaho.metastore.test.testclasses.factory.B;
import org.pentaho.metastore.test.testclasses.factory_shared.X;
import org.pentaho.metastore.test.testclasses.factory_shared.Y;
import org.pentaho.metastore.test.testclasses.my.ChildElement;
import org.pentaho.metastore.test.testclasses.my.InheritedElement;
import org.pentaho.metastore.test.testclasses.my.MyElement;
import org.pentaho.metastore.test.testclasses.my.MyElementAttr;
import org.pentaho.metastore.test.testclasses.my.MyFilenameElement;
import org.pentaho.metastore.test.testclasses.my.MyMigrationElement;
import org.pentaho.metastore.test.testclasses.my.MyNameElement;
import org.pentaho.metastore.test.testclasses.my.MyOtherElement;
import org.pentaho.metastore.util.MetaStoreUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


import static org.mockito.Mockito.*;

public class MetaStoreFactoryTest extends TestCase {

  public static final String PASSWORD = "my secret password";
  public static final String ANOTHER = "2222222";
  public static final String ATTR = "11111111";
  public static final String NAME = "one";
  public static final int INT = 3;
  public static final long LONG = 4;
  public static final boolean BOOL = true;
  public static final LocalDateTime DATE = LocalDateTime.of( 2024, 1, 2, 3, 4, 5, 678000000 );
  public static final int NR_ATTR = 10;
  public static final int NR_NAME = 5;
  public static final int NR_FILENAME = 5;

  @Test
  public void testIDMigration() throws Exception {

    String namespace = "custom";
    String stepName = "Step Name";
    String elementName = "migration";
    String hostName = "Host Name";
    String fieldMappings = "Field Mappings";
    String sourceFieldName = "Source Field Name";
    String targetFieldName = "Target Field Name";
    String parameterName = "Parameter Name";

    MemoryMetaStore memoryMetaStore = new MemoryMetaStore();
    memoryMetaStore.setName( "memory" );
    IMetaStore metaStore = memoryMetaStore;

    MetaStoreFactory<MyMigrationElement> factory =
      new MetaStoreFactory<MyMigrationElement>( MyMigrationElement.class, metaStore, namespace );

    if ( !metaStore.namespaceExists( namespace ) ) {
      metaStore.createNamespace( namespace );
    }

    MetaStoreElementType elementTypeAnnotation = MyMigrationElement.class.getAnnotation( MetaStoreElementType.class );

    // Make sure the element type exists...
    IMetaStoreElementType elementType = metaStore.getElementTypeByName( namespace, elementTypeAnnotation.name() );
    if ( elementType == null ) {
      elementType = metaStore.newElementType( namespace );
      elementType.setName( elementTypeAnnotation.name() );
      elementType.setDescription( elementTypeAnnotation.description() );
      metaStore.createElementType( namespace, elementType );
    }

    // Create an element with the old keys we want to migrate
    IMetaStoreElement element = metaStore.newElement();
    element.setName( elementName );
    element.setElementType( elementType );

    element.addChild( metaStore.newAttribute( "stepName", stepName ) );
    element.addChild( metaStore.newAttribute( "hostname", hostName ) );
    element.addChild( metaStore.newAttribute( "fieldMappings", fieldMappings ) );
    element.addChild( metaStore.newAttribute( "sourceFieldName", sourceFieldName ) );
    element.addChild( metaStore.newAttribute( "targetFieldName", targetFieldName ) );
    element.addChild( metaStore.newAttribute( "parameterName", parameterName ) );

    metaStore.createElement( namespace, elementType, element );

    MyMigrationElement loadedElement = factory.loadElement( elementName );

    assertNotNull( loadedElement );
    assertEquals( loadedElement.getStepName(), stepName );
    assertEquals( loadedElement.getHostname(), hostName );
    assertEquals( loadedElement.getFieldMappings(), fieldMappings );
    assertEquals( loadedElement.getSourceFieldName(), sourceFieldName );
    assertEquals( loadedElement.getTargetFieldName(), targetFieldName );
    assertEquals( loadedElement.getParameterName(), parameterName );

    // Test the variation of the step name id
    String existingElementId = element.getId();
    element = metaStore.newElement();
    element.setId( existingElementId );
    element.setName( elementName );
    element.setElementType( elementType );

    element.addChild( metaStore.newAttribute( "stepname", stepName ) );

    metaStore.updateElement( namespace, elementType, existingElementId, element );

    loadedElement = factory.loadElement( elementName );

    assertNotNull( loadedElement );
    assertEquals( loadedElement.getStepname(), stepName );
  }

  @Test
  public void testMyElement() throws Exception {

    IMetaStore metaStore = new MemoryMetaStore();

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
    assertLoadedMyElement( me, verify );
    assertStoredMyElement( metaStore, element, factory );
    assertSubAttributes( verify );
    assertReferencedMyOtherElement( myOtherElement, verify );
    assertStringList( stringList, verify );
    assertFactoryContents( factory );
  }

  private void assertLoadedMyElement( MyElement expected, MyElement actual ) {
    assertNotNull( actual );
    assertEquals( expected.getMyAttribute(), actual.getMyAttribute() );
    assertEquals( expected.getAnotherAttribute(), actual.getAnotherAttribute() );
    assertEquals( expected.getPasswordAttribute(), actual.getPasswordAttribute() );
    assertEquals( expected.getIntAttribute(), actual.getIntAttribute() );
    assertEquals( expected.getLongAttribute(), actual.getLongAttribute() );
    assertEquals( expected.isBoolAttribute(), actual.isBoolAttribute() );
    assertEquals( expected.getDateAttribute(), actual.getDateAttribute() );
    assertEquals( expected.getSubAttributes().size(), actual.getSubAttributes().size() );
    assertEquals( expected.getNameElement(), actual.getNameElement() );
    assertEquals( expected.getFilenameElement(), actual.getFilenameElement() );
  }

  private void assertStoredMyElement( IMetaStore metaStore, IMetaStoreElement element,
                                      MetaStoreFactory<MyElement> factory ) throws MetaStoreException {
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
  }

  private void assertSubAttributes( MyElement element ) {
    for ( int i = 0; i < NR_ATTR; i++ ) {
      MyElementAttr attribute = element.getSubAttributes().get( i );
      assertEquals( "key" + i, attribute.getKey() );
      assertEquals( "value" + i, attribute.getValue() );
      assertEquals( "desc" + i, attribute.getDescription() );
    }
  }

  private void assertReferencedMyOtherElement( MyOtherElement expected, MyElement actual ) {
    MyOtherElement actualOtherElement = actual.getMyOtherElement();
    assertNotNull( actualOtherElement );
    assertEquals( expected.getName(), actualOtherElement.getName() );
    assertEquals( expected.getSomeAttribute(), actualOtherElement.getSomeAttribute() );
  }

  private void assertStringList( List<String> expected, MyElement actual ) {
    List<String> actualList = actual.getStringList();
    assertEquals( expected.size(), actualList.size() );
    for ( int i = 0; i < expected.size(); i++ ) {
      assertEquals( expected.get( i ), actualList.get( i ) );
    }
  }

  private void assertFactoryContents( MetaStoreFactory<MyElement> factory ) throws MetaStoreException {
    List<String> names = factory.getElementNames();
    assertEquals( 1, names.size() );
    assertEquals( NAME, names.get( 0 ) );

    List<MyElement> elements = factory.getElements();
    assertEquals( 1, elements.size() );
    assertEquals( NAME, elements.get( 0 ).getName() );

    factory.deleteElement( NAME );
    assertEquals( 0, factory.getElementNames().size() );
    assertEquals( 0, factory.getElements().size() );
  }

  @Test
  public void testFactoryShared() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<A> factoryA = new MetaStoreFactory<A>( A.class, metaStore, "pentaho" );
    MetaStoreFactory<B> factoryB = new MetaStoreFactory<B>( B.class, metaStore, "pentaho" );
    factoryA.addNameFactory( A.FACTORY_B, factoryB );

    // Construct test-class
    A a = new A( "a" );
    a.getBees().add( new B( "1", true ) );
    a.getBees().add( new B( "2", true ) );
    a.getBees().add( new B( "3", false ) );
    a.getBees().add( new B( "4", true ) );
    a.setB( new B( "b", false ) );

    factoryA.saveElement( a );

    // 1, 2, 4
    //
    assertEquals( 3, factoryB.getElements().size() );

    A _a = factoryA.loadElement( "a" );
    assertNotNull( _a );
    assertEquals( 4, _a.getBees().size() );
    assertEquals( "1", _a.getBees().get( 0 ).getName() );
    assertEquals( true, _a.getBees().get( 0 ).isShared() );
    assertEquals( "2", _a.getBees().get( 1 ).getName() );
    assertEquals( true, _a.getBees().get( 1 ).isShared() );
    assertEquals( "3", _a.getBees().get( 2 ).getName() );
    assertEquals( false, _a.getBees().get( 2 ).isShared() );
    assertEquals( "4", _a.getBees().get( 3 ).getName() );
    assertEquals( true, _a.getBees().get( 3 ).isShared() );

    assertNotNull( _a.getB() );
    assertEquals( "b", _a.getB().getName() );
    assertEquals( false, _a.getB().isShared() );
  }

  @Test
  public void testFactory() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<X> factoryX = new MetaStoreFactory<X>( X.class, metaStore, "pentaho" );
    MetaStoreFactory<Y> factoryY = new MetaStoreFactory<Y>( Y.class, metaStore, "pentaho" );
    factoryX.addNameFactory( X.FACTORY_Y, factoryY );

    // Construct test-class
    X x = new X( "x" );
    x.getYs().add( new Y( "1", "desc1" ) );
    x.getYs().add( new Y( "2", "desc2" ) );
    x.getYs().add( new Y( "3", "desc3" ) );
    x.getYs().add( new Y( "4", "desc4" ) );
    x.setY( new Y( "y", "descY" ) );

    factoryX.saveElement( x );

    // 1, 2, 3, 4, y
    //
    assertEquals( 5, factoryY.getElements().size() );

    X _x = factoryX.loadElement( "x" );
    assertNotNull( _x );
    assertEquals( 4, _x.getYs().size() );
    assertEquals( "1", _x.getYs().get( 0 ).getName() );
    assertEquals( "desc1", _x.getYs().get( 0 ).getDescription() );
    assertEquals( "2", _x.getYs().get( 1 ).getName() );
    assertEquals( "desc2", _x.getYs().get( 1 ).getDescription() );
    assertEquals( "3", _x.getYs().get( 2 ).getName() );
    assertEquals( "desc3", _x.getYs().get( 2 ).getDescription() );
    assertEquals( "4", _x.getYs().get( 3 ).getName() );
    assertEquals( "desc4", _x.getYs().get( 3 ).getDescription() );

    assertNotNull( _x.getY() );
    assertEquals( "y", _x.getY().getName() );
    assertEquals( "descY", _x.getY().getDescription() );
  }

  /**
   * Save and load a complete Cube object in the IMetaStore through named references and factories.
   * Some object are saved through a factory with a name reference.  One dimension is embedded in the cube.
   * 
   * @throws Exception
   */
  @Test
  public void testCube() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<Cube> factoryCube = new MetaStoreFactory<Cube>( Cube.class, metaStore, "pentaho" );
    MetaStoreFactory<Dimension> factoryDimension = new MetaStoreFactory<Dimension>( Dimension.class, metaStore, "pentaho" );
    factoryCube.addNameFactory( Cube.DIMENSION_FACTORY_KEY, factoryDimension );
    IMetaStoreObjectFactory objectFactory = mock( IMetaStoreObjectFactory.class );
    factoryCube.setObjectFactory( objectFactory );
    factoryDimension.setObjectFactory( objectFactory );

    final AtomicInteger contextCount = new AtomicInteger( 0 );
    when( objectFactory.getContext( any() ) ).thenAnswer( new Answer<Object>() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        Map<String, String> context = new HashMap<String, String>();
        context.put( "context-num", String.valueOf( contextCount.getAndIncrement() ) );
        return context;
      }
    } );
    when( objectFactory.instantiateClass( anyString(), anyMap() ) ).thenAnswer( new Answer<Object>() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        String className = (String) invocation.getArguments()[0];
        return Class.forName( className ).getDeclaredConstructor().newInstance();
      }
    } );

    Cube cube = generateCube();
    factoryCube.saveElement( cube );

    // Now load back and verify...
    Cube verify = factoryCube.loadElement( cube.getName() );

    assertEquals( cube.getName(), verify.getName() );
    assertCubeDimensions( cube, verify );
    assertCubeKpis( cube, verify );
    assertDimension( cube.getJunkDimension(), verify.getJunkDimension() );
    assertDimension( cube.getNonSharedDimension(), verify.getNonSharedDimension() );

    // Make sure that nonShared and product are not shared.
    // We can load them with the dimension factory and they should not come back.
    //
    assertNull( factoryDimension.loadElement( "analyticalDim" ) );
    assertNull( factoryDimension.loadElement( "product" ) );

    assertKpi( cube.getMainKpi(), verify.getMainKpi() );
    assertObjectFactoryInteractions( objectFactory, contextCount.get() );
  }

  private void assertCubeDimensions( Cube expected, Cube actual ) {
    assertEquals( expected.getDimensions().size(), actual.getDimensions().size() );
    for ( int i = 0; i < expected.getDimensions().size(); i++ ) {
      assertDimension( expected.getDimensions().get( i ), actual.getDimensions().get( i ) );
    }
  }

  private void assertDimension( Dimension expected, Dimension actual ) {
    assertNotNull( actual );
    assertEquals( expected.getName(), actual.getName() );
    assertEquals( expected.getDimensionType(), actual.getDimensionType() );
    assertEquals( expected.getAttributes().size(), actual.getAttributes().size() );
    for ( int i = 0; i < expected.getAttributes().size(); i++ ) {
      assertDimensionAttribute( expected.getAttributes().get( i ), actual.getAttributes().get( i ) );
    }
  }

  private void assertDimensionAttribute( DimensionAttribute expected, DimensionAttribute actual ) {
    assertEquals( expected.getName(), actual.getName() );
    assertEquals( expected.getDescription(), actual.getDescription() );
    assertEquals( expected.getSomeOtherStuff(), actual.getSomeOtherStuff() );
  }

  private void assertCubeKpis( Cube expected, Cube actual ) {
    assertEquals( expected.getKpis().size(), actual.getKpis().size() );
    for ( int i = 0; i < expected.getKpis().size(); i++ ) {
      assertKpi( expected.getKpis().get( i ), actual.getKpis().get( i ) );
    }
  }

  private void assertKpi( Kpi expected, Kpi actual ) {
    assertNotNull( actual );
    assertEquals( expected.getName(), actual.getName() );
    assertEquals( expected.getDescription(), actual.getDescription() );
    assertEquals( expected.getOtherDetails(), actual.getOtherDetails() );
  }

  private void assertObjectFactoryInteractions( IMetaStoreObjectFactory objectFactory, int contextCount )
    throws MetaStoreException {
    for ( int i = 0; i < contextCount; i++ ) {
      Map<String, String> context = new HashMap<String, String>();
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
    product.setShared( false );
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

    Dimension nonShared = new Dimension();
    nonShared.setName( "analyticalDim" );
    nonShared.setAttributes( generateAttributes() );
    nonShared.setDimensionType( DimensionType.JUNK );
    nonShared.setShared( false );
    cube.setNonSharedDimension( nonShared );

    cube.setKpis( generateKpis() );

    Kpi mainKpi = new Kpi();
    mainKpi.setName( "mainKpi-name" );
    mainKpi.setDescription( "mainKpi-description" );
    mainKpi.setOtherDetails( "mainKpi-otherDetails" );
    cube.setMainKpi( mainKpi );

    return cube;
  }

  public void testSanitizeName() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<MyOtherElement> factory = new MetaStoreFactory<MyOtherElement>( MyOtherElement.class, metaStore, "custom" );
    MyOtherElement element = new MyOtherElement( null, ATTR );

    try {
      factory.saveElement( element );
      fail( "Saved illegal element (name == null)" );
    } catch ( MetaStoreException e ) {
      assertNotNull( e );
    }

    try {
      element.setName( "" );
      factory.saveElement( element );
      fail( "Saved illegal element (name.isEmpty())" );
    } catch ( MetaStoreException e ) {
      assertNotNull( e );
    }

    try {
      element.setName( " " );
      factory.saveElement( element );
      fail( "Saved illegal element (name.isEmpty())" );
    } catch ( MetaStoreException e ) {
      assertNotNull( e );
    }

    element.setName( NAME );
    factory.saveElement( element );
    assertEquals( Arrays.asList( NAME ), factory.getElementNames() );
  }

  @Test
  public void testLoadElementWithInvalidIntegerAttribute() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<MyElement> factory = new MetaStoreFactory<>( MyElement.class, metaStore, "custom" );
    MyElement element = new MyElement( NAME, ATTR, ANOTHER, PASSWORD, INT, LONG, BOOL, DATE );

    factory.saveElement( element );
    getStoredElement( metaStore, factory, NAME ).getChild( "intAttribute" ).setValue( "invalid" );

    try {
      factory.loadElement( NAME );
      fail( "Expected invalid integer error" );
    } catch ( NumberFormatException exception ) {
      assertTrue( exception.getMessage().contains( "invalid" ) );
    }
  }

  @Test
  public void testLoadElementWithInvalidLongAttribute() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<MyElement> factory = new MetaStoreFactory<>( MyElement.class, metaStore, "custom" );
    MyElement element = new MyElement( NAME, ATTR, ANOTHER, PASSWORD, INT, LONG, BOOL, DATE );

    factory.saveElement( element );
    getStoredElement( metaStore, factory, NAME ).getChild( "longAttribute" ).setValue( "invalid" );

    try {
      factory.loadElement( NAME );
      fail( "Expected invalid long error" );
    } catch ( NumberFormatException exception ) {
      assertTrue( exception.getMessage().contains( "invalid" ) );
    }
  }

  @Test
  public void testLoadElementWithInvalidEnumAttribute() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<Dimension> factory = new MetaStoreFactory<>( Dimension.class, metaStore, "custom" );
    Dimension dimension = new Dimension();
    dimension.setName( NAME );
    dimension.setDimensionType( DimensionType.SCD );

    factory.saveElement( dimension );
    getStoredElement( metaStore, factory, NAME ).getChild( "dimension_type" ).setValue( "invalid" );

    try {
      factory.loadElement( NAME );
      fail( "Expected invalid enum error" );
    } catch ( IllegalArgumentException exception ) {
      assertTrue( exception.getMessage().contains( "invalid" ) );
    }
  }

  @Test
  public void testLoadElementWithInvalidDateAttribute() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<MyElement> factory = new MetaStoreFactory<>( MyElement.class, metaStore, "custom" );
    MyElement element = new MyElement( NAME, ATTR, ANOTHER, PASSWORD, INT, LONG, BOOL, DATE );

    factory.saveElement( element );
    getStoredElement( metaStore, factory, NAME ).getChild( "dateAttribute" ).setValue( "invalid" );

    try {
      factory.loadElement( NAME );
      fail( "Expected invalid date error" );
    } catch ( MetaStoreException exception ) {
      assertTrue( exception.getMessage().contains( "Unexpected date parsing problem" ) );
    }
  }

  @Test
  public void testLegacyDateAttributeRoundTrip() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<LegacyDateElement> factory =
      new MetaStoreFactory<>( LegacyDateElement.class, metaStore, "custom" );
    Date date = new Date( 1704164645678L );
    LegacyDateElement element = new LegacyDateElement();
    element.setName( NAME );
    element.setDate( date );

    factory.saveElement( element );

    LegacyDateElement loaded = factory.loadElement( NAME );
    assertEquals( date, loaded.getDate() );
  }

  @Test
  public void testLoadElementWithoutNameReferenceList() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<MyElement> factory = new MetaStoreFactory<>( MyElement.class, metaStore, "custom" );
    MyElement element = new MyElement();
    element.setName( NAME );
    element.setNameElement( new MyNameElement( "name", "description", "color" ) );

    factory.saveElement( element );

    try {
      factory.loadElement( NAME );
      fail( "Expected missing name reference list error" );
    } catch ( MetaStoreException exception ) {
      assertTrue( exception.getCause().getMessage().contains( "Unable to find reference list" ) );
    }
  }

  @Test
  public void testLoadElementWithoutFilenameReferenceList() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<MyElement> factory = new MetaStoreFactory<>( MyElement.class, metaStore, "custom" );
    MyElement element = new MyElement();
    element.setName( NAME );
    element.setFilenameElement( new MyFilenameElement( "filename", "size", "gender" ) );

    factory.saveElement( element );

    try {
      factory.loadElement( NAME );
      fail( "Expected missing filename reference list error" );
    } catch ( MetaStoreException exception ) {
      assertTrue( exception.getCause().getMessage().contains( "Unable to find reference list" ) );
    }
  }

  @Test
  public void testLoadElementWithoutFactoryReference() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<MyOtherElement> otherFactory = new MetaStoreFactory<>( MyOtherElement.class, metaStore, "custom" );
    MetaStoreFactory<MyElement> sourceFactory = new MetaStoreFactory<>( MyElement.class, metaStore, "custom" );
    MyElement element = new MyElement();
    element.setName( NAME );
    element.setMyOtherElement( new MyOtherElement( "other", ATTR ) );
    sourceFactory.addNameFactory( MyElement.FACTORY_OTHER_ELEMENT, otherFactory );

    sourceFactory.saveElement( element );

    MetaStoreFactory<MyElement> targetFactory = new MetaStoreFactory<>( MyElement.class, metaStore, "custom" );
    try {
      targetFactory.loadElement( NAME );
      fail( "Expected missing factory reference error" );
    } catch ( MetaStoreException exception ) {
      assertTrue( exception.getCause().getMessage().contains( "Unable to find factory" ) );
    }
  }

  @Test
  public void testLoadElementWithoutSetter() throws Exception {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<MissingSetterElement> factory =
      new MetaStoreFactory<>( MissingSetterElement.class, metaStore, "custom" );
    MissingSetterElement element = new MissingSetterElement();
    element.setName( NAME );
    element.value = ATTR;

    factory.saveElement( element );

    try {
      factory.loadElement( NAME );
      fail( "Expected missing setter error" );
    } catch ( MetaStoreException exception ) {
      assertTrue( exception.getMessage().contains( "Unable to find setter" ) );
    }
  }

  private List<Kpi> generateKpis() {
    List<Kpi> list = new ArrayList<Kpi>();
    for ( int i = 0; i < 5; i++ ) {
      Kpi kpi = new Kpi();
      kpi.setName( "kpi-" + ( i + 1 ) );
      kpi.setDescription( "desc-" + ( i + 1 ) );
      kpi.setOtherDetails( "othd-" + ( i + 1 ) );
      list.add( kpi );
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

  @Test
  public void testElementsWithFieldsInParent() throws MetaStoreException {
    IMetaStore metaStore = new MemoryMetaStore();
    MetaStoreFactory<InheritedElement> factory =
      new MetaStoreFactory<>( InheritedElement.class, metaStore, "custom" );
    InheritedElement element = new InheritedElement( ANOTHER, ATTR );
    element.setName( "name" );
    ChildElement childElement = new ChildElement();
    childElement.setProperty1( ATTR );
    childElement.setProperty2( ANOTHER );
    element.setChildElement( childElement );

    assertEquals( 0, factory.getElements().size() );

    factory.saveElement( element );

    List<InheritedElement> elements = factory.getElements();
    assertNotNull( elements );
    assertEquals( 1, elements.size() );
    InheritedElement elementAfterSave = elements.get( 0 );
    assertEquals( "name", elementAfterSave.getName() );
    assertEquals( ANOTHER, elementAfterSave.getProperty1() );
    assertEquals( ATTR, elementAfterSave.getProperty2() );
    assertNotNull( element.getChildElement() );
    assertEquals( ATTR, elementAfterSave.getChildElement().getProperty1() );
    assertEquals( ANOTHER, elementAfterSave.getChildElement().getProperty2() );
  }

  private IMetaStoreElement getStoredElement( IMetaStore metaStore, MetaStoreFactory<?> factory, String name )
    throws MetaStoreException {
    return metaStore.getElementByName( factory.getNamespace(), factory.getElementType(), name );
  }

  @MetaStoreElementType( name = "Missing setter", description = "Test element without a setter" )
  public static class MissingSetterElement {
    private String name;

    @MetaStoreAttribute
    private String value;

    public String getName() {
      return name;
    }

    public void setName( String name ) {
      this.name = name;
    }

    public String getValue() {
      return value;
    }
  }

  @MetaStoreElementType( name = "Legacy date", description = "Test element with a legacy Date attribute" )
  public static class LegacyDateElement {
    private String name;

    @MetaStoreAttribute
    private Date date;

    public LegacyDateElement() {
    }

    public String getName() {
      return name;
    }

    public void setName( String name ) {
      this.name = name;
    }

    public Date getDate() {
      return date;
    }

    public void setDate( Date date ) {
      this.date = date;
    }
  }
}
