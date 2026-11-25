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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

/**
 * Tests the .readonly marker file: no locking on reads, and writes rejected.
 */
public class XmlMetaStoreReadOnlyTest {

  private static final String NAMESPACE = "ns1";
  private static final String TYPE_NAME = "type1";
  private static final String ELEMENT_NAME = "elem1";

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private XmlMetaStore store;
  private Path root;
  private Path readOnlyFile;
  private Path lockFile;

  private IMetaStoreElementType elementType;
  private IMetaStoreElement element;

  @Before
  public void setUp() throws Exception {
    store = new XmlMetaStore( tmp.getRoot().getAbsolutePath() );
    root = Paths.get( store.getRootFolder() );
    readOnlyFile = root.resolve( BaseXmlMetaStore.READONLY_FILE_NAME );
    lockFile = root.resolve( ".lock" );

    // Seed some data while the store is still writable
    store.createNamespace( NAMESPACE );
    elementType = store.newElementType( NAMESPACE );
    elementType.setName( TYPE_NAME );
    store.createElementType( NAMESPACE, elementType );

    element = store.newElement();
    element.setName( ELEMENT_NAME );
    store.createElement( NAMESPACE, elementType, element );
  }

  private void makeReadOnly() throws Exception {
    Files.createFile( readOnlyFile );
    store = new XmlMetaStore( tmp.getRoot().getAbsolutePath() );   // mode is fixed per instance
  }

  /** Simulates another process holding the lock. A locking read would wait the full 10s and fail. */
  private void simulateHeldLock() throws Exception {
    Files.createFile( lockFile );
  }

  /** Counts element IDs via a fresh instance (cold cache), so the result is the same on every branch. */
  private int elementIdCount() throws Exception {
    return new XmlMetaStore( tmp.getRoot().getAbsolutePath() ).getElementIds( NAMESPACE, elementType ).size();
  }

  @FunctionalInterface
  private interface Op {
    void run() throws Exception;
  }

  private void assertReadOnlyError( String label, Op op ) throws Exception {
    try {
      op.run();
      fail( label + " should have thrown in read-only mode" );
    } catch ( MetaStoreException e ) {
      assertTrue( label + ": message should mention the marker file, was: " + e.getMessage(),
          e.getMessage().contains( BaseXmlMetaStore.READONLY_FILE_NAME ) );
    }
  }

  // ---- reads ----------------------------------------------------------------------------------

  @Test( timeout = 5000 )
  public void readsDoNotWaitForLockWhenReadOnly() throws Exception {
    makeReadOnly();
    simulateHeldLock();

    // fresh instance = cold cache (on 11.0.0.2, getElementIds skips files the cache already processed)
    XmlMetaStore reader = new XmlMetaStore( tmp.getRoot().getAbsolutePath() );

    assertTrue( reader.getNamespaces().contains( NAMESPACE ) );
    assertTrue( reader.namespaceExists( NAMESPACE ) );
    assertNotNull( reader.getElementTypeByName( NAMESPACE, TYPE_NAME ) );
    assertEquals( 1, reader.getElementTypes( NAMESPACE ).size() );
    assertEquals( 1, reader.getElementIds( NAMESPACE, elementType ).size() );
    assertNotNull( reader.getElement( NAMESPACE, elementType, ELEMENT_NAME ) );
    assertNotNull( reader.getElementByName( NAMESPACE, elementType, ELEMENT_NAME ) );
    assertEquals( 1, reader.getElements( NAMESPACE, elementType ).size() );
  }

  @Test( timeout = 5000 )
  public void readsDoNotTouchExistingLockFileWhenReadOnly() throws Exception {
    makeReadOnly();
    simulateHeldLock();

    store.getElementTypeByName( NAMESPACE, TYPE_NAME );

    assertTrue( "unlock must not delete a lock it never took", Files.exists( lockFile ) );
  }

  @Test( timeout = 5000 )
  public void readsDoNotCreateLockFileWhenReadOnly() throws Exception {
    makeReadOnly();

    store.getElementTypeByName( NAMESPACE, TYPE_NAME );
    store.getElementByName( NAMESPACE, elementType, ELEMENT_NAME );

    assertFalse( Files.exists( lockFile ) );
  }

  @Test( timeout = 30000 )
  public void manyThreadsReadWithoutTimeoutWhenReadOnly() throws Exception {
    makeReadOnly();
    simulateHeldLock();   // would starve every thread if reads still locked

    int threads = 50;
    ExecutorService pool = Executors.newFixedThreadPool( threads );
    try {
      List<Future<Boolean>> results = new ArrayList<>();
      for ( int i = 0; i < threads; i++ ) {
        // separate store instances, as separate jobs would have
        final XmlMetaStore own = new XmlMetaStore( tmp.getRoot().getAbsolutePath() );
        results.add( pool.submit( new Callable<Boolean>() {
          @Override
          public Boolean call() throws Exception {
            IMetaStoreElementType type = own.getElementTypeByName( NAMESPACE, TYPE_NAME );
            return type != null && own.getElementByName( NAMESPACE, type, ELEMENT_NAME ) != null;
          }
        } ) );
      }
      for ( Future<Boolean> f : results ) {
        assertTrue( f.get( 20, TimeUnit.SECONDS ) );
      }
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  public void unlockStillReleasesOwnLockIfReadOnlyAppearsMidOperation() throws Exception {
    store.lockStore();                                   // takes a real lock, mode cached as writable
    assertTrue( Files.exists( lockFile ) );
    Files.createFile( readOnlyFile );
    store.unlockStore();
    assertFalse( Files.exists( lockFile ) );
  }

  @Test
  public void unlockDoesNotDeleteAnotherHoldersLockIfReadOnlyIsRemovedMidOperation() throws Exception {
    makeReadOnly();
    store.lockStore();                                   // skipped, mode cached as read-only
    Files.delete( readOnlyFile );
    simulateHeldLock();
    store.unlockStore();
    assertTrue( Files.exists( lockFile ) );
  }

  // ---- writes ---------------------------------------------------------------------------------

  @Test
  public void allWritesAreRejectedWhenReadOnly() throws Exception {
    makeReadOnly();

    final IMetaStoreElementType newType = store.newElementType( NAMESPACE );
    newType.setName( "type2" );
    final IMetaStoreElement newElement = store.newElement();
    newElement.setName( "elem2" );

    assertReadOnlyError( "createNamespace", () -> store.createNamespace( "ns2" ) );
    assertReadOnlyError( "deleteNamespace", () -> store.deleteNamespace( NAMESPACE ) );
    assertReadOnlyError( "createElementType", () -> store.createElementType( NAMESPACE, newType ) );
    assertReadOnlyError( "updateElementType", () -> store.updateElementType( NAMESPACE, elementType ) );
    assertReadOnlyError( "deleteElementType", () -> store.deleteElementType( NAMESPACE, elementType ) );
    assertReadOnlyError( "createElement", () -> store.createElement( NAMESPACE, elementType, newElement ) );
    assertReadOnlyError( "updateElement",
        () -> store.updateElement( NAMESPACE, elementType, ELEMENT_NAME, element ) );
    assertReadOnlyError( "deleteElement", () -> store.deleteElement( NAMESPACE, elementType, ELEMENT_NAME ) );
  }

  @Test
  public void rejectedWritesChangeNothingAndLeaveNoLock() throws Exception {
    makeReadOnly();

    try {
      store.deleteElement( NAMESPACE, elementType, ELEMENT_NAME );
      fail( "expected read-only error" );
    } catch ( MetaStoreException expected ) {
      // expected
    }

    assertNotNull( store.getElement( NAMESPACE, elementType, ELEMENT_NAME ) );
    assertFalse( Files.exists( lockFile ) );
  }

  // ---- normal (not read-only) behavior --------------------------------------------------------

  @Test
  public void writesWorkAndLeaveNoLockWhenNotReadOnly() throws Exception {
    IMetaStoreElement second = store.newElement();
    second.setName( "elem2" );
    store.createElement( NAMESPACE, elementType, second );

    assertEquals( 2, elementIdCount() );
    assertFalse( "lock must be released after each operation", Files.exists( lockFile ) );

    store.deleteElement( NAMESPACE, elementType, "elem2" );
    assertEquals( 1, elementIdCount() );
    assertFalse( Files.exists( lockFile ) );
  }

  @Test
  public void readsStillLockWhenNotReadOnly() throws Exception {
    // A read must create and then remove .lock, so it leaves nothing behind
    store.getElementTypeByName( NAMESPACE, TYPE_NAME );
    assertFalse( Files.exists( lockFile ) );
  }

  // ---- toggling -------------------------------------------------------------------------------

  @Test
  public void modeIsFixedPerInstanceAndNewInstancesSeeTheChange() throws Exception {
    store.createNamespace( "ns2" );                      // first use: writable, now cached
    Files.createFile( readOnlyFile );                    // marker appears while the instance is live
    store.createNamespace( "ns3" );                      // existing instance is unaffected
    assertTrue( store.namespaceExists( "ns3" ) );

    final XmlMetaStore later = new XmlMetaStore( tmp.getRoot().getAbsolutePath() );
    assertReadOnlyError( "new instance", () -> later.createNamespace( "ns4" ) );

    Files.delete( readOnlyFile );                        // removal also only affects new instances
    assertReadOnlyError( "same instance", () -> later.createNamespace( "ns4" ) );
    new XmlMetaStore( tmp.getRoot().getAbsolutePath() ).createNamespace( "ns4" );
  }

  @Test
  public void markerFileIsNotListedAsANamespace() throws Exception {
    makeReadOnly();
    assertFalse( store.getNamespaces().contains( BaseXmlMetaStore.READONLY_FILE_NAME ) );
    assertEquals( 1, store.getNamespaces().size() );
  }
}