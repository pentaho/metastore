package org.pentaho.metastore.stores.xml;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.pentaho.metastore.api.IMetaStoreElementType;

public class SimpleXmlMetaStoreCacheTest {

  private SimpleXmlMetaStoreCache simpleXmlMetaStoreCache;
  
  @Before
  public void before() {
    simpleXmlMetaStoreCache = new SimpleXmlMetaStoreCache();
  }
  
  @Test
  public void registerElementTypeIdForName() {
    simpleXmlMetaStoreCache.registerElementTypeIdForName( "testNamespace", "testElementTypeName", "testElementTypeId" );
    String actualElementId = simpleXmlMetaStoreCache.getElementTypeIdByName( "testNamespace", "testElementTypeName" );
    assertEquals( "testElementTypeId", actualElementId );
  }
  
  @Test
  public void unregisterElementTypeIdForName() {
    simpleXmlMetaStoreCache.registerElementTypeIdForName( "testNamespace", "testElementTypeName", "testElementTypeId" );
    simpleXmlMetaStoreCache.unregisterElementTypeId( "testNamespace", "testElementTypeId" );
    String actualElementId = simpleXmlMetaStoreCache.getElementTypeIdByName( "testNamespace", "testElementTypeName" );
    assertNull( actualElementId );
  }
  
  @Test
  public void registerElementIdForName() {
    IMetaStoreElementType testElementType = createTestElementType( "testElementTypeName", "testElementTypeId" );
    simpleXmlMetaStoreCache.registerElementTypeIdForName( "testNamespace", testElementType.getName(), testElementType.getId() );
    simpleXmlMetaStoreCache.registerElementIdForName( "testNamespace", testElementType, "testElementName", "testElementId" );
    String actualElementId = simpleXmlMetaStoreCache.getElementIdByName( "testNamespace", testElementType, "testElementName" );
    assertEquals( "testElementId", actualElementId );
  }
  
  @Test
  public void registerElementIdForName_for_non_registered_type() {
    IMetaStoreElementType testElementType = createTestElementType( "testElementTypeName", "testElementTypeId" );
    simpleXmlMetaStoreCache.registerElementIdForName( "testNamespace", testElementType, "testElementName", "testElementId" );
    String actualElementId = simpleXmlMetaStoreCache.getElementIdByName( "testNamespace", testElementType, "testElementName" );
    assertEquals( "testElementId", actualElementId );
  }
  
  @Test
  public void unregisterElementIdForName() {
    IMetaStoreElementType testElementType = createTestElementType( "testElementTypeName", "testElementTypeId" );
    simpleXmlMetaStoreCache.registerElementTypeIdForName( "testNamespace", testElementType.getName(), testElementType.getId() );
    simpleXmlMetaStoreCache.registerElementIdForName( "testNamespace", testElementType, "testElementName", "testElementId" );
    simpleXmlMetaStoreCache.unregisterElementId( "testNamespace", testElementType, "testElementId" );
    String actualElementId = simpleXmlMetaStoreCache.getElementIdByName( "testNamespace", testElementType, "testElementName" );
    assertNull( "testElementId", actualElementId );
  }
  
  @Test
  public void registerProcessedFile() {
    simpleXmlMetaStoreCache.registerProcessedFile( "/test/full/Path", 1L );
    Map<String, Long> actualProcessedFiles = simpleXmlMetaStoreCache.getProcessedFiles();
    assertThat( actualProcessedFiles.size(), equalTo( 1 ) );
    assertThat( actualProcessedFiles.containsKey( "/test/full/Path" ), is( true ));
  }
  
  @Test
  public void unregisterProcessedFile() {
    simpleXmlMetaStoreCache.registerProcessedFile( "/test/full/Path", 1L );
    simpleXmlMetaStoreCache.unregisterProcessedFile( "/test/full/Path" );
    Map<String, Long> actualProcessedFiles = simpleXmlMetaStoreCache.getProcessedFiles();
    assertThat( actualProcessedFiles.size(), equalTo( 0 ) );
  }
  
  private static IMetaStoreElementType createTestElementType( String typeName, String typeId ) {
    IMetaStoreElementType testElementType = mock( IMetaStoreElementType.class );
    when( testElementType.getName() ).thenReturn( typeName );
    when( testElementType.getId() ).thenReturn( typeId );
    return testElementType;
  }
}
