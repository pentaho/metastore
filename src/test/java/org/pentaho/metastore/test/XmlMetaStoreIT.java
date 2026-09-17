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

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElement;
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

  protected XmlMetaStore createMetaStore( String rootFolder ) throws MetaStoreException {
    return new XmlMetaStore( rootFolder );
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    FileUtil.cleanFolder( new File( metaStore.getRootFolder() ).getParentFile(), true );
  }

  public void test() throws Exception {
    super.testFunctionality( metaStore );
  }

  public void testElementIdsIncludesProcessedFiles() throws Exception {
    Path rootPath = Files.createTempDirectory( "XmlMetaStoreIT" );
    try {
      XmlMetaStore store = createMetaStore( rootPath.toString() );
      store.createNamespace( namespace );

      IMetaStoreElementType elementType = store.newElementType( namespace );
      elementType.setName( SHARED_DIMENSION_NAME );
      store.createElementType( namespace, elementType );

      IMetaStoreElement element = store.newElement();
      element.setName( CUSTOMER_DIMENSION_NAME );
      store.createElement( namespace, elementType, element );

      assertEquals( 1, store.getElementIds( namespace, elementType ).size() );
    } finally {
      FileUtil.cleanFolder( rootPath.toFile(), true );
    }
  }

  public void testRootFolderNormalizesTrailingSeparator() throws Exception {
    Path rootPath = Files.createTempDirectory( "XmlMetaStoreIT" );
    try {
      XmlMetaStore store = createMetaStore( rootPath.toString() + File.separator );
      assertEquals( rootPath.resolve( "metastore" ).toString(), store.getRootFolder() );
    } finally {
      FileUtil.cleanFolder( rootPath.toFile(), true );
    }
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
          @Override
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
    XmlMetaStore xmlMetaStore = new XmlMetaStore( rootPath.toString() );
    assertTrue( xmlMetaStore.getElementTypes( "pentaho" ).isEmpty() );

    IMetaStoreElementType elementType =
      new XmlMetaStoreElementType( "pentaho", "NamedCluster", "NamedCluster", "A Named Cluster" );
    xmlMetaStore.createElementType( "pentaho", elementType );  //throws an exception before change

    assertEquals( 1, xmlMetaStore.getElementTypes( "pentaho" ).size() );
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

