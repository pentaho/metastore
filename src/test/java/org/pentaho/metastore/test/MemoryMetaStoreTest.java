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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

public class MemoryMetaStoreTest extends MetaStoreTestBase {

  private MemoryMetaStore metaStore;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    metaStore = new MemoryMetaStore();
    metaStore.setName( META_STORE_NAME );
  }

  public void test() throws Exception {
    super.testFunctionality( metaStore );
  }

  public void testHashCodeMatchesCaseInsensitiveEquals() {
    MemoryMetaStore first = new MemoryMetaStore();
    MemoryMetaStore second = new MemoryMetaStore();
    first.setName( "MetaStore" );
    second.setName( "metastore" );

    assertEquals( first, second );
    assertEquals( first.hashCode(), second.hashCode() );
  }

  public void testParrallelRetrive() throws Exception {
    super.testParallelOneStore( metaStore );
  }

  public void testCreateElementOverwritesDuplicateId() throws Exception {
    String namespace = "duplicate-elements";
    metaStore.createNamespace( namespace );
    IMetaStoreElementType elementType = metaStore.newElementType( namespace );
    elementType.setName( "duplicate-element-type" );
    metaStore.createElementType( namespace, elementType );

    IMetaStoreElement firstElement = metaStore.newElement();
    firstElement.setId( "same-id" );
    firstElement.setName( "first" );
    metaStore.createElement( namespace, elementType, firstElement );

    IMetaStoreElement duplicateElement = metaStore.newElement();
    duplicateElement.setId( "same-id" );
    duplicateElement.setName( "duplicate" );
    metaStore.createElement( namespace, elementType, duplicateElement );

    assertEquals( "duplicate", metaStore.getElement( namespace, elementType, "same-id" ).getName() );
  }

  public void testParallelMutationsSameElementType() throws Exception {
    final String concurrentNamespace = "concurrent";
    final String elementTypeName = "concurrent-type";
    final int threadCount = 16;
    final int elementsPerThread = 25;

    metaStore.createNamespace( concurrentNamespace );
    IMetaStoreElementType elementType = metaStore.newElementType( concurrentNamespace );
    elementType.setName( elementTypeName );
    metaStore.createElementType( concurrentNamespace, elementType );

    final CountDownLatch ready = new CountDownLatch( threadCount );
    final CountDownLatch start = new CountDownLatch( 1 );
    final List<Exception> exceptions = Collections.synchronizedList( new ArrayList<Exception>() );
    List<Thread> threads = new ArrayList<Thread>();

    for ( int threadIndex = 0; threadIndex < threadCount; threadIndex++ ) {
      final int currentThreadIndex = threadIndex;
      Thread thread = new Thread() {
        @Override
        public void run() {
          ready.countDown();
          try {
            start.await();
            for ( int elementIndex = 0; elementIndex < elementsPerThread; elementIndex++ ) {
              String elementName = "element-" + currentThreadIndex + "-" + elementIndex;
              IMetaStoreElement element = metaStore.newElement();
              element.setName( elementName );
              metaStore.createElement( concurrentNamespace, elementType, element );
              element.setValue( "updated" );
              metaStore.updateElement( concurrentNamespace, elementType, element.getId(), element );
            }
          } catch ( Exception e ) {
            exceptions.add( e );
          }
        }
      };
      threads.add( thread );
      thread.start();
    }

    boolean allThreadsReady = ready.await( 10, TimeUnit.SECONDS );
    start.countDown();
    assertTrue( "Worker threads did not become ready", allThreadsReady );

    for ( Thread thread : threads ) {
      thread.join( 10_000 );
      assertFalse( "Worker thread did not finish", thread.isAlive() );
    }

    assertTrue( exceptions.toString(), exceptions.isEmpty() );
    assertEquals( threadCount * elementsPerThread, metaStore.getElements( concurrentNamespace, elementType ).size() );

    for ( int threadIndex = 0; threadIndex < threadCount; threadIndex++ ) {
      for ( int elementIndex = 0; elementIndex < elementsPerThread; elementIndex++ ) {
        String elementName = "element-" + threadIndex + "-" + elementIndex;
        IMetaStoreElement element = metaStore.getElementByName( concurrentNamespace, elementType, elementName );
        assertNotNull( element );
        assertEquals( "updated", element.getValue() );
      }
    }
  }

}
