package org.pentaho.metastore.stores.xml;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;
import static org.junit.Assert.fail;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

public class XmlMetaStoreElementTest {
  
    @Test
    public void testLongName() {
      String pattern ="1234567890";
      StringBuilder fileName = new StringBuilder( 310 );
      for ( int i = 0; i<30; i++ ){
        fileName.append( pattern );
      }
      String tempDir = System.getProperty( "java.io.tmpdir" );
      XmlMetaStoreElement xmse = new XmlMetaStoreElement();
      xmse.setFilename( tempDir + File.separator + fileName );
      try {
        xmse.save();
        fail();
      } catch ( MetaStoreException ex ) {
        if ( !( ex.getCause() instanceof FileNotFoundException ) || !ex.getMessage().contains( "too long" ) ) {
          fail();
        }
      }
    }

}
