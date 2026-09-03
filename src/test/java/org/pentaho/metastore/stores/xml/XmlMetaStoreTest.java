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


package org.pentaho.metastore.stores.xml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.util.FileUtil;

public class XmlMetaStoreTest {

  @Test
  public void getRootFolderDoesNotWaitForStoreMonitor() throws Exception {
    Path root = Files.createTempDirectory( "XmlMetaStoreTest" );
    try {
      XmlMetaStore store = new XmlMetaStore( root.toString() );
      assertRootOperationDoesNotWaitForStoreMonitor( store, store::getRootFolder );
    } finally {
      FileUtil.cleanFolder( root.toFile(), true );
    }
  }

  @Test
  public void hashCodeMatchesCaseInsensitiveEquals() throws Exception {
    Path root = Files.createTempDirectory( "XmlMetaStoreTest" );
    try {
      XmlMetaStore first = new XmlMetaStore( root.toString() );
      XmlMetaStore second = new XmlMetaStore( root.toString() );
      first.setName( "MetaStore" );
      second.setName( "metastore" );

      assertEquals( first, second );
      assertEquals( first.hashCode(), second.hashCode() );
    } finally {
      FileUtil.cleanFolder( root.toFile(), true );
    }
  }

  @Test
  public void setRootFolderDoesNotWaitForStoreMonitor() throws Exception {
    Path initialRoot = Files.createTempDirectory( "XmlMetaStoreTest" );
    Path newRoot = Files.createTempDirectory( "XmlMetaStoreTest" );
    try {
      XmlMetaStore store = new XmlMetaStore( initialRoot.toString() );
      assertRootOperationDoesNotWaitForStoreMonitor( store, () -> store.setRootFolder( newRoot.toString() ) );
      assertEquals( newRoot.toString(), store.getRootFolder() );
    } finally {
      FileUtil.cleanFolder( initialRoot.toFile(), true );
      FileUtil.cleanFolder( newRoot.toFile(), true );
    }
  }

  @Test
  public void updateElementUsesElementIdWhenElementNameChanges() throws Exception {
    Path root = Files.createTempDirectory( "XmlMetaStoreTest" );
    try {
      XmlMetaStore store = new XmlMetaStore( root.toString() );
      String namespace = "test";
      store.createNamespace( namespace );

      IMetaStoreElementType elementType = store.newElementType( namespace );
      elementType.setName( "type" );
      store.createElementType( namespace, elementType );

      IMetaStoreElement original = store.newElement();
      original.setId( "original-id" );
      original.setName( "original-name" );
      store.createElement( namespace, elementType, original );

      IMetaStoreElement updated = store.newElement();
      updated.setId( "original-id" );
      updated.setName( "updated-name" );
      store.updateElement( namespace, elementType, "original-id", updated );

      IMetaStoreElement loaded = store.getElement( namespace, elementType, "original-id" );
      assertEquals( "original-id", loaded.getId() );
      assertEquals( "updated-name", loaded.getName() );
      assertFalse( Files.exists( root.resolve( "metastore" ).resolve( namespace ).resolve( "type" )
          .resolve( "updated-name.xml" ) ) );
    } finally {
      FileUtil.cleanFolder( root.toFile(), true );
    }
  }

  @Test
  public void deleteNamespaceIgnoresMissingNamespace() throws Exception {
    Path root = Files.createTempDirectory( "XmlMetaStoreTest" );
    try {
      XmlMetaStore store = new XmlMetaStore( root.toString() );
      String namespace = "missing";
      store.deleteNamespace( namespace );
      assertFalse( store.namespaceExists( namespace ) );
    } finally {
      FileUtil.cleanFolder( root.toFile(), true );
    }
  }

  @Test
  public void ignoresNonXmlFilesWhenListingElements() throws Exception {
    Path root = Files.createTempDirectory( "XmlMetaStoreTest" );
    try {
      XmlMetaStore store = new XmlMetaStore( root.toString() );
      String namespace = "test";
      store.createNamespace( namespace );

      IMetaStoreElementType elementType = store.newElementType( namespace );
      elementType.setName( "type" );
      store.createElementType( namespace, elementType );
      Files.createFile( root.resolve( "metastore" ).resolve( namespace ).resolve( "type" ).resolve( "notes.txt" ) );

      assertTrue( store.getElementIds( namespace, elementType ).isEmpty() );
      assertTrue( store.getElements( namespace, elementType ).isEmpty() );
    } finally {
      FileUtil.cleanFolder( root.toFile(), true );
    }
  }

  private void assertRootOperationDoesNotWaitForStoreMonitor( XmlMetaStore store, Runnable operation ) throws Exception {
    CountDownLatch operationStarted = new CountDownLatch( 1 );
    CountDownLatch operationFinished = new CountDownLatch( 1 );
    Thread operationThread = new Thread( () -> {
      operationStarted.countDown();
      operation.run();
      operationFinished.countDown();
    } );

    synchronized ( store ) {
      operationThread.start();
      assertTrue( operationStarted.await( 1, TimeUnit.SECONDS ) );
      assertTrue( operationFinished.await( 1, TimeUnit.SECONDS ) );
    }
    operationThread.join( 1_000 );
    assertFalse( operationThread.isAlive() );
  }
}
