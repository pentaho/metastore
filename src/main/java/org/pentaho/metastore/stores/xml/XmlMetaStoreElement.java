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

import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Stores a metastore element in an XML file.
 */
public class XmlMetaStoreElement extends BaseXmlMetaStoreElement {

  /**
   * Creates an empty XML element.
   */
  public XmlMetaStoreElement() {
    super();
  }

  /**
   * Creates an XML element with its type, ID, and value.
   *
   * @param elementType the element type
   * @param id the element ID
   * @param value the element value
   */
  public XmlMetaStoreElement( IMetaStoreElementType elementType, String id, Object value ) {
    super( elementType, id, value );
  }

  /**
   * Load element data recursively from an XML file...
   * 
   * @param filename
   *          The absolute path to the file to load the element (with children) from.
   * @throws MetaStoreException
   *           In case there is a problem reading the file.
   */
  public XmlMetaStoreElement( String filename ) throws MetaStoreException {
    this();
    Path filePath = Paths.get( filename ).normalize();
    setFilename( filename );
    setIdWithFilename( filename );

    try ( InputStream in = Files.newInputStream( filePath ) ) {
      loadFromStream( in );
    } catch ( IOException ex ) {
      throw new MetaStoreException( "Unable to load XML metastore attribute from file '" + filePath + "'", ex );
    }
  }

  /**
   * Copies an element into XML form.
   *
   * @param element the element to copy
   */
  public XmlMetaStoreElement( IMetaStoreElement element ) {
    super( element );
  }

  /**
   * Compares XML elements by ID.
   *
   * @param obj the object to compare
   * @return {@code true} when both elements have the same ID
   */
  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( !( obj instanceof XmlMetaStoreElement ) ) {
      return false;
    }
    String otherId = ( (XmlMetaStoreElement) obj ).id;
    return id == null ? otherId == null : id.equals( otherId );
  }

  /**
   * Returns a hash based on the element ID.
   *
   * @return the element ID hash
   */
  @Override
  public int hashCode() {
    return id == null ? 0 : id.hashCode();
  }

  /**
   * Saves this element to its configured XML file.
   *
   * @throws MetaStoreException if the element cannot be saved
   */
  @Override
  public void save() throws MetaStoreException {
    try ( OutputStream out = Files.newOutputStream( getFilenamePath() ) ) {
      save( out );
    } catch ( IOException ex ) {
      throw new MetaStoreException( "Unable to save XML metastore element to file '" + getFilenamePath() + "'", ex );
    }
  }
}
