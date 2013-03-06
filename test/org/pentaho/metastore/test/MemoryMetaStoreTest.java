package org.pentaho.metastore.test;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

public class MemoryMetaStoreTest extends BaseMetaStoreTest {
  
  public void test() throws Exception {
    // Run the test against the XML metadata store.
    IMetaStore metaStore = new MemoryMetaStore();
    super.testFunctionality(metaStore);
  }
}
