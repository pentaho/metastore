package org.pentaho.metastore.test;

import java.io.File;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.util.FileUtil;

public class XmlMetaStoreTest extends MetaStoreTestBase {
  
  public void test() throws Exception {
    // First make sure to delete the meta store content, from a possible previous failed execution of this test.
    // This is the only XmlMetaStore specific code of the unit test.
    //
    File storeFolder = new File(new XmlMetaStore().getRootFolder()); 
    FileUtil.cleanFolder(storeFolder, false);

    // Run the test against the XML metadata store.
    //
    IMetaStore metaStore = new XmlMetaStore();
    super.testFunctionality(metaStore);
  }
}
