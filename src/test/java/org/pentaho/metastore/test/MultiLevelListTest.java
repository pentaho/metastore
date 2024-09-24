/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.metastore.test;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.test.testclasses.my.Level1Element;
import org.pentaho.metastore.test.testclasses.my.Level2Element;
import org.pentaho.metastore.test.testclasses.my.Level3Element;
import org.pentaho.metastore.test.testclasses.my.Level4Element;
import org.pentaho.metastore.test.testclasses.my.MyOtherElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rowell Belen
 */
public class MultiLevelListTest {

  private String tempDir = null;
  private IMetaStore metaStore = null;

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

  public MetaStoreFactory<Level1Element> getMetaStoreFactory() {

    MetaStoreFactory<Level1Element>
        factory =
        new MetaStoreFactory( Level1Element.class, this.metaStore, "pentaho" );
    return factory;
  }

  @Test
  public void test() throws Exception {

    MyOtherElement myOtherElement1 = new MyOtherElement();
    myOtherElement1.setName( "myOtherElement1" );

    MyOtherElement myOtherElement2 = new MyOtherElement();
    myOtherElement2.setName( "myOtherElement2" );

    Level3Element level3Element = new Level3Element();
    level3Element.setName( "level3" );

    Level2Element level2Element = new Level2Element();
    level2Element.setName( "level2" );
    level2Element.getLevel3Elements().add( level3Element );
    level2Element.getLevel3Elements().add( level3Element );
    level2Element.getLevel3Elements().add( level3Element );

    Level1Element level1Element = new Level1Element();
    level1Element.setName( "level1" );
    level1Element.getLevel2Elements().add( level2Element );
    level1Element.getLevel2Elements().add( level2Element );

    level1Element.getOtherElements().add( myOtherElement1 );
    level1Element.getOtherElements().add( myOtherElement2 );

    MetaStoreFactory<Level1Element> factory = getMetaStoreFactory();
    factory.saveElement( level1Element );

    Level1Element level1ElementLoaded = factory.loadElement( "level1" );
    Assert.assertNotNull( level1ElementLoaded );
    Assert.assertEquals( 2, level1ElementLoaded.getOtherElements().size() );
    Assert.assertEquals( 2, level1ElementLoaded.getLevel2Elements().size() );
    Assert.assertEquals( 3, level1ElementLoaded.getLevel2Elements().get( 0 ).getLevel3Elements().size() );
  }

  @Test( expected = MetaStoreException.class )
  public void testError() throws Exception {
    MetaStoreFactory<Level4Element> factory =
        new MetaStoreFactory( Level4Element.class, this.metaStore, "pentaho" );

    MyOtherElement myElement = new MyOtherElement();
    myElement.setName( "myElementName" );

    List<MyOtherElement> elements = new ArrayList<MyOtherElement>();
    elements.add( myElement );

    Level4Element element = new Level4Element();
    element.setName( "level4" );
    element.setMyElements( elements );

    factory.saveElement( element ); // this is fine

    factory.loadElement( element.getName() ); // this should fail (list in Level4Element is not pre-instantiated)
  }
}
