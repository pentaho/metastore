package org.pentaho.metastore.test.testclasses.my;

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rowell Belen
 */

@MetaStoreElementType(
    name = "Level2Element",
    description = "My Level 2 Element" )
public class Level2Element {

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private List<Level3Element> level3Elements = new ArrayList<Level3Element>(  );

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public List<Level3Element> getLevel3Elements() {
    return level3Elements;
  }

  public void setLevel3Elements( List<Level3Element> level3Elements ) {
    this.level3Elements = level3Elements;
  }
}
