package org.pentaho.metastore.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.pentaho.metastore.test.testclasses.cube.Cube;
import org.pentaho.metastore.test.testclasses.cube.CubeObjectFactory;
import org.pentaho.metastore.test.testclasses.cube.Dimension;
import org.pentaho.metastore.test.testclasses.cube.DimensionAttribute;
import org.pentaho.metastore.test.testclasses.cube.DimensionType;
import org.pentaho.metastore.test.testclasses.cube.Kpi;
import org.pentaho.metastore.test.testclasses.factory.A;
import org.pentaho.metastore.test.testclasses.factory.B;
import org.pentaho.metastore.test.testclasses.factory_shared.X;
import org.pentaho.metastore.test.testclasses.factory_shared.Y;
import org.pentaho.metastore.test.testclasses.my.MyElement;
import org.pentaho.metastore.test.testclasses.my.MyElementAttr;
import org.pentaho.metastore.test.testclasses.my.MyFilenameElement;
import org.pentaho.metastore.test.testclasses.my.MyNameElement;
import org.pentaho.metastore.test.testclasses.my.MyOtherElement;
import org.pentaho.metastore.util.MetaStoreUtil;

public class MetaStoreFactoryTest extends TestCase {

  @Test
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
    assertEquals( "1", a.getBees().get( 0 ).getName() );
    assertEquals( true, a.getBees().get( 0 ).isShared() );
    assertEquals( "2", a.getBees().get( 1 ).getName() );
    assertEquals( true, a.getBees().get( 1 ).isShared() );
    assertEquals( "3", a.getBees().get( 2 ).getName() );
    assertEquals( false, a.getBees().get( 2 ).isShared() );
    assertEquals( "4", a.getBees().get( 3 ).getName() );
    assertEquals( true, a.getBees().get( 3 ).isShared() );

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
    assertEquals( "1", x.getYs().get( 0 ).getName() );
    assertEquals( "desc1", x.getYs().get( 0 ).getDescription() );
    assertEquals( "2", x.getYs().get( 1 ).getName() );
    assertEquals( "desc2", x.getYs().get( 1 ).getDescription() );
    assertEquals( "3", x.getYs().get( 2 ).getName() );
    assertEquals( "desc3", x.getYs().get( 2 ).getDescription() );
    assertEquals( "4", x.getYs().get( 3 ).getName() );
    assertEquals( "desc4", x.getYs().get( 3 ).getDescription() );

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
    CubeObjectFactory objectFactory = new CubeObjectFactory();
    factoryCube.setObjectFactory( objectFactory );
    factoryDimension.setObjectFactory( objectFactory );

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

    assertNotNull( verify.getNonSharedDimension() );
    Dimension nonShared = cube.getNonSharedDimension();
    Dimension nonSharedVerify = verify.getNonSharedDimension();
    assertEquals( nonShared.getName(), nonSharedVerify.getName() );
    assertEquals( nonShared.getAttributes().size(), nonSharedVerify.getAttributes().size() );
    for ( int i = 0; i < junk.getAttributes().size(); i++ ) {
      DimensionAttribute attr = nonShared.getAttributes().get( i );
      DimensionAttribute attrVerify = nonSharedVerify.getAttributes().get( i );
      assertEquals( attr.getName(), attrVerify.getName() );
      assertEquals( attr.getDescription(), attrVerify.getDescription() );
      assertEquals( attr.getSomeOtherStuff(), attrVerify.getSomeOtherStuff() );
    }

    // Make sure that nonShared and product are not shared.
    // We can load them with the dimension factory and they should not come back.
    //
    assertNull( factoryDimension.loadElement( "analyticalDim" ) );
    assertNull( factoryDimension.loadElement( "product" ) );

    assertNotNull( verify.getMainKpi() );
    assertEquals( cube.getMainKpi().getName(), verify.getMainKpi().getName() );
    assertEquals( cube.getMainKpi().getDescription(), verify.getMainKpi().getDescription() );
    assertEquals( cube.getMainKpi().getOtherDetails(), verify.getMainKpi().getOtherDetails() );

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
