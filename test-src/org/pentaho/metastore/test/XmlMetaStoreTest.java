package org.pentaho.metastore.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.stores.xml.XmlMetaStoreElementType;
import org.pentaho.metastore.util.FileUtil;

public class XmlMetaStoreTest extends MetaStoreTestBase {
  
  public void test() throws Exception {
    // Run the test against the XML metadata store.
    //
    XmlMetaStore metaStore = new XmlMetaStore();
    try {
      super.testFunctionality(metaStore);
    } finally {
      FileUtil.cleanFolder(new File(metaStore.getRootFolder()).getParentFile(), true);
    }
  }
  
  public void testParallelDifferentStores() throws Exception {
    List<XmlMetaStore> stores = new ArrayList<XmlMetaStore>();
    
    try {
      // Run the test against the XML metadata store.
      //
      for (int i=0;i<50;i++) {
        String folder = System.getProperty("java.io.tmpdir")+File.separator+UUID.randomUUID();
        new File(folder).mkdirs();
        stores.add(new XmlMetaStore(folder));
      }
      
      List<Thread> threads = new ArrayList<Thread>();
      for (final IMetaStore store : stores) {
        Thread thread = new Thread() {
          public void run() {
            try {
              XmlMetaStoreTest.super.testFunctionality(store);
            } catch(MetaStoreException e) {
              throw new RuntimeException(e);
            }
          }
        };
        threads.add(thread);
        thread.start();
      }
      
      for (Thread thread : threads) {
        thread.join();
      }
    } finally {

    }

    for (XmlMetaStore store : stores) {
      FileUtil.cleanFolder(new File(store.getRootFolder()).getParentFile(), true);
    }
  }

  public void testParallelOneStore() throws Exception {

    final XmlMetaStore metaStore = new XmlMetaStore();
    final List<Exception> exceptions = new ArrayList<Exception>();
    
    try {
      // Run the test against the XML metadata store.
      //
      
      List<Thread> threads = new ArrayList<Thread>();
      
      for (int i=9000;i<9020;i++) {
        final int index=i;
        Thread thread = new Thread() {
          public void run() {
            try {
              XmlMetaStoreTest.parallelStoreRetrieve(metaStore, index);
            } catch(Exception e) {
              exceptions.add(e);
            }
          }
        };
        threads.add(thread);
        thread.start();
      }
        
      for (Thread thread : threads) {
        thread.join();
      }
    } finally {
      FileUtil.cleanFolder(new File(metaStore.getRootFolder()).getParentFile(), true);
    }
    
    if (!exceptions.isEmpty()) {
      fail(exceptions.size()+" exceptions encountered during parallel store/retrieve");
      for (Exception e : exceptions) {
        e.printStackTrace(System.err);
      }
    }
  }

  protected static void parallelStoreRetrieve(final XmlMetaStore metaStore, final int index) throws MetaStoreException {
    String namespace = "ns-"+index;
    metaStore.createNamespace(namespace);
    
    int nrTypes=5;
    int nrElements=20;
    
    for (int typeNr=50;typeNr<50+nrTypes;typeNr++) {
      IMetaStoreElementType elementType = metaStore.newElementType(namespace);
      String typeName = "type-name-"+index+"-"+typeNr;
      String typeDescription = "type-description-"+index+"-"+typeNr;
      elementType.setName(typeName);
      elementType.setDescription(typeDescription);
      metaStore.createElementType(namespace, elementType);
      
      assertNotNull(elementType.getId());
      
      XmlMetaStoreElementType verifyType = metaStore.getElementType(namespace, elementType.getId());
      assertEquals(typeName, verifyType.getName());
      assertEquals(typeDescription, verifyType.getDescription());
      assertNotNull(verifyType.getId());
      
      verifyType = metaStore.getElementTypeByName(namespace, elementType.getName());
      assertEquals(typeName, verifyType.getName());
      assertEquals(typeDescription, verifyType.getDescription());
      assertNotNull(verifyType.getId());
      
      // Populate
      List<IMetaStoreElement> elements = new ArrayList<IMetaStoreElement>();
      for (int i=100;i<100+nrElements;i++) {
        IMetaStoreElement element = populateElement(metaStore, "element-"+index+"-"+i);
        elements.add(element);
        metaStore.createElement(namespace, elementType, element);
        assertNotNull(element.getId());
      }
      
      try {
        metaStore.deleteElementType(namespace, elementType);
        fail("Unable to detect dependencies");
      } catch(MetaStoreDependenciesExistsException e) {
        // OK
        assertEquals(e.getDependencies().size(), elements.size());
      }      
    }

    for (int typeNr=50;typeNr<50+nrTypes;typeNr++) {
      String typeName = "type-name-"+index+"-"+typeNr;
      IMetaStoreElementType elementType = metaStore.getElementTypeByName(namespace, typeName);
      assertNotNull(elementType);

      List<IMetaStoreElement> verifyElements = metaStore.getElements(namespace, elementType);
      assertEquals(nrElements, verifyElements.size());
    
      // the elements come back in an unpredictable order
      // sort by name
      //
      Collections.sort(verifyElements, new Comparator<IMetaStoreElement>() {
        @Override
        public int compare(IMetaStoreElement o1, IMetaStoreElement o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
    
      // Validate
      for (int i=0;i<verifyElements.size();i++) {
        IMetaStoreElement element = verifyElements.get(i);
        validateElement(element, "element-"+index+"-"+(100+i));
        metaStore.deleteElement(namespace, elementType, element.getId());
      }
    
      verifyElements = metaStore.getElements(namespace, elementType);
      assertEquals(0, verifyElements.size());
            
      metaStore.deleteElementType(namespace, elementType);
    }
    

  }

  protected static IMetaStoreElement populateElement(IMetaStore metaStore, String name) throws MetaStoreException {
    IMetaStoreElement element = metaStore.newElement();
    element.setName(name);
    for (int i=1;i<=5;i++) {
      element.addChild(metaStore.newAttribute("id "+i, "value "+i));
    }
    IMetaStoreAttribute subAttr = metaStore.newAttribute("sub-attr", null);
    for (int i=101;i<=110;i++) {
      subAttr.addChild(metaStore.newAttribute("sub-id "+i, "sub-value "+i));
    }
    element.addChild(subAttr);
    
    return element;
  }
  
  protected static void validateElement(IMetaStoreElement element, String name) throws MetaStoreException {
    assertEquals(name, element.getName());
    assertEquals(6, element.getChildren().size());
    for (int i=1;i<=5;i++) {
      IMetaStoreAttribute child = element.getChild("id "+i);
      assertEquals("value "+i, child.getValue());
    }
    IMetaStoreAttribute subAttr = element.getChild("sub-attr");
    assertNotNull(subAttr);
    assertEquals(10, subAttr.getChildren().size());
    for (int i=101;i<=110;i++) {
      IMetaStoreAttribute child = subAttr.getChild("sub-id "+i);
      assertNotNull(child);
      assertEquals("sub-value "+i, child.getValue());
    }
  }
}
