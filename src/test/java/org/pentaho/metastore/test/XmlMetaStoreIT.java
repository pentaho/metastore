/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metastore.test;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.stores.xml.XmlMetaStoreElementType;
import org.pentaho.metastore.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class XmlMetaStoreIT extends MetaStoreTestBase {

  private XmlMetaStore metaStore;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    metaStore = createMetaStore();
  }

  protected XmlMetaStore createMetaStore() throws MetaStoreException {
    return new XmlMetaStore();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    FileUtil.cleanFolder( new File( metaStore.getRootFolder() ).getParentFile(), true );
  }

  public void test() throws Exception {
    super.testFunctionality( metaStore );
  }

  public void testParallelDifferentStores() throws Exception {
    List<XmlMetaStore> stores = new ArrayList<>();
    final List<Throwable> exceptions = new ArrayList<>();
    // Run the test against the XML metadata store.
    //
    try {
      for ( int i = 0; i < 10; i++ ) {
        stores.add( new XmlMetaStore() );
      }

      List<Thread> threads = new ArrayList<Thread>();
      for ( final IMetaStore store : stores ) {
        Thread thread = new Thread() {
          public void run() {
            try {
              testFunctionality( store );
            } catch ( Throwable e ) {
              exceptions.add( e );
            }
          }
        };
        threads.add( thread );
        thread.start();
      }

      for ( Thread thread : threads ) {
        thread.join();
      }
    } finally {
      for ( XmlMetaStore store : stores ) {
        FileUtil.cleanFolder( new File( store.getRootFolder() ).getParentFile(), true );
      }
    }

    if ( !exceptions.isEmpty() ) {
      for ( Throwable e : exceptions ) {
        e.printStackTrace( System.err );
      }
      fail( exceptions.size() + " exceptions encountered during parallel store/retrieve" );
    }

  }

  public void testUnmanagedFoldersAreAllowed() throws IOException, MetaStoreException {
    Path rootPath = Files.createTempDirectory( "XmlMetaStoreIT" );
    Path metastorePath = rootPath.resolve( "metastore" ).resolve( "pentaho" ).resolve( "NamedCluster" );
    Files.createDirectories( metastorePath );
    XmlMetaStore metaStore = new XmlMetaStore( rootPath.toString() );
    assertTrue( metaStore.getElementTypes( "pentaho" ).isEmpty() );

    IMetaStoreElementType elementType =
      new XmlMetaStoreElementType( "pentaho", "NamedCluster", "NamedCluster", "A Named Cluster" );
    metaStore.createElementType( "pentaho", elementType );  //throws an exception before change

    assertEquals( 1, metaStore.getElementTypes( "pentaho" ).size() );
  }

  public void testParallelOneStore() throws Exception {
    final List<Exception> exceptions = new ArrayList<>();

    List<Thread> threads = new ArrayList<>();

    for ( int i = 9000; i < 9020; i++ ) {
      final int index = i;
      Thread thread = new Thread( () -> {
        try {
          parallelStoreRetrieve( metaStore, index );
        } catch ( Exception e ) {
          exceptions.add( e );
        }
      } );
      threads.add( thread );
      thread.start();
    }

    for ( Thread thread : threads ) {
      thread.join();
    }

    if ( !exceptions.isEmpty() ) {
      for ( Exception e : exceptions ) {
        e.printStackTrace( System.err );
      }
      fail( exceptions.size() + " exceptions encountered during parallel store/retrieve" );

    }
  }

}

