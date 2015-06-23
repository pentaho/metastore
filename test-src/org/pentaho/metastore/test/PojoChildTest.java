package org.pentaho.metastore.test;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.test.testclasses.my.ChildElement;
import org.pentaho.metastore.test.testclasses.my.ParentElement;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;


public class PojoChildTest {

  private String tempDir = null;
  private IMetaStore metaStore = null;

  private static String XML_METASTORE = "test-res/metastore_test";

  @Before
  public void before() throws IOException, MetaStoreException {
    File f = File.createTempFile( "MultiLevelListTest", "before" );
    f.deleteOnExit();

    tempDir = f.getParent();
    metaStore = new XmlMetaStore( tempDir );
  }

  @After
  public void after() throws IOException {
    FileUtils.deleteDirectory( new File( ( (XmlMetaStore) metaStore ).getRootFolder() ) );
  }

  public MetaStoreFactory<ParentElement> getMetaStoreFactory( IMetaStore metaStore ) {

    MetaStoreFactory<ParentElement>
        factory = new MetaStoreFactory( ParentElement.class, metaStore, "pentaho" );
    return factory;
  }

  @Test
  public void testSaveAndLoad() throws Exception {
    ParentElement p = createSample();
    getMetaStoreFactory( this.metaStore ).saveElement( p );
    ParentElement lp = getMetaStoreFactory( this.metaStore ).loadElement( "test" );
    verify( p, lp );
  }

  @Test
  public void testLoadFromFile() throws Exception {
    ParentElement p = createSample();
    XmlMetaStore xmlMetaStore = new XmlMetaStore( XML_METASTORE );
    ParentElement lp = getMetaStoreFactory( xmlMetaStore ).loadElement( "test" );
    verify( p, lp );
  }

  @Test
  public void testLoadFromFileLegacy() throws Exception {
    ParentElement p = createSample();
    XmlMetaStore xmlMetaStore = new XmlMetaStore( XML_METASTORE );
    ParentElement lp = getMetaStoreFactory( xmlMetaStore ).loadElement( "test_legacy" );
    verify( p, lp );
  }

  private ParentElement createSample() {
    ChildElement c = new ChildElement();
    c.setProperty1( "p1" );
    c.setProperty2( "p2" );
    ParentElement p = new ParentElement();
    p.setName( "test" );
    p.setChildElement( c );
    return p;
  }

  private void verify( ParentElement original, ParentElement loaded ) {
    assertNotNull( original );
    assertNotNull( loaded );
    assertEquals( original.getChildElement().getProperty1(), loaded.getChildElement().getProperty1() );
    assertEquals( original.getChildElement().getProperty2(), loaded.getChildElement().getProperty2() );
  }
}
