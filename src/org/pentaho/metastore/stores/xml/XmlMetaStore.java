/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.metastore.stores.xml;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.pentaho.metastore.api.BaseMetaStore;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementExistException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;

public class XmlMetaStore extends BaseMetaStore implements IMetaStore {

  private String rootFolder;

  private File rootFile;

  public XmlMetaStore() throws MetaStoreException {
    this( System.getProperty( "java.io.tmpdir" ) + File.separator + UUID.randomUUID() );
  }

  public XmlMetaStore( String rootFolder ) throws MetaStoreException {
    this.rootFolder = rootFolder + File.separator + XmlUtil.META_FOLDER_NAME;

    rootFile = new File( this.rootFolder );
    if ( !rootFile.exists() ) {
      if ( !rootFile.mkdirs() ) {
        throw new MetaStoreException( "Unable to create XML meta store root folder: " + this.rootFolder );
      }
    }

    // Give the MetaStore a default name
    //
    setName( this.rootFolder );
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( !( obj instanceof XmlMetaStore ) ) {
      return false;
    }
    return ( (XmlMetaStore) obj ).name.equalsIgnoreCase( name );
  }

  @Override
  public synchronized List<String> getNamespaces() throws MetaStoreException {
    lockStore();
    try {
      File[] files = listFolders( rootFile );
      List<String> namespaces = new ArrayList<String>();
      for ( File file : files ) {
        namespaces.add( file.getName() );
      }
      return namespaces;
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized boolean namespaceExists( String namespace ) throws MetaStoreException {
    lockStore();
    try {
      String spaceFolder = XmlUtil.getNamespaceFolder( rootFolder, namespace );
      File spaceFile = new File( spaceFolder );
      return spaceFile.exists();
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void createNamespace( String namespace ) throws MetaStoreException,
    MetaStoreNamespaceExistsException {
    lockStore();
    try {
      String spaceFolder = XmlUtil.getNamespaceFolder( rootFolder, namespace );
      File spaceFile = new File( spaceFolder );
      if ( spaceFile.exists() ) {
        throw new MetaStoreNamespaceExistsException( "The namespace with name '" + namespace + "' already exists." );
      }
      if ( !spaceFile.mkdir() ) {
        throw new MetaStoreException( "Unable to create XML meta store namespace folder: " + spaceFolder );
      }
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void deleteNamespace( String namespace ) throws MetaStoreException,
    MetaStoreElementTypeExistsException {
    lockStore();
    try {
      String spaceFolder = XmlUtil.getNamespaceFolder( rootFolder, namespace );
      File spaceFile = new File( spaceFolder );
      if ( !spaceFile.exists() ) {
        return; // Should we throw an exception?
      }
      List<IMetaStoreElementType> elementTypes = getElementTypes( namespace, false );

      if ( !elementTypes.isEmpty() ) {
        List<String> dependencies = new ArrayList<String>();
        for ( IMetaStoreElementType elementType : elementTypes ) {
          dependencies.add( elementType.getId() );
        }
        throw new MetaStoreDependenciesExistsException( dependencies,
            "Unable to delete the XML meta store namespace with name '" + namespace
                + "' as it still contains dependencies" );
      }

      if ( !spaceFile.delete() ) {
        throw new MetaStoreException( "Unable to delete XML meta store namespace folder, check to see if it's empty" );
      }
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized List<IMetaStoreElementType> getElementTypes( String namespace ) throws MetaStoreException {
    return getElementTypes( namespace, true );
  }

  protected synchronized List<IMetaStoreElementType> getElementTypes( String namespace, boolean lock )
    throws MetaStoreException {
    if ( lock ) {
      lockStore();
    }
    try {
      List<IMetaStoreElementType> elementTypes = new ArrayList<IMetaStoreElementType>();

      String spaceFolder = XmlUtil.getNamespaceFolder( rootFolder, namespace );
      File spaceFolderFile = new File( spaceFolder );
      File[] elementTypeFolders = listFolders( spaceFolderFile );
      for ( File elementTypeFolder : elementTypeFolders ) {
        String elementTypeId = elementTypeFolder.getName();
        IMetaStoreElementType elementType = getElementType( namespace, elementTypeId, false );
        elementTypes.add( elementType );
      }

      return elementTypes;
    } finally {
      if ( lock ) {
        unlockStore();
      }
    }
  }

  @Override
  public synchronized List<String> getElementTypeIds( String namespace ) throws MetaStoreException {
    lockStore();
    try {
      List<String> ids = new ArrayList<String>();

      String spaceFolder = XmlUtil.getNamespaceFolder( rootFolder, namespace );
      File spaceFolderFile = new File( spaceFolder );
      File[] elementTypeFolders = listFolders( spaceFolderFile );
      for ( File elementTypeFolder : elementTypeFolders ) {
        String elementTypeId = elementTypeFolder.getName();
        ids.add( elementTypeId );
      }

      return ids;
    } finally {
      unlockStore();
    }
  }

  protected synchronized XmlMetaStoreElementType getElementType( String namespace, String elementTypeId, boolean lock )
    throws MetaStoreException {
    if ( lock ) {
      lockStore();
    }
    try {
      String elementTypeFile = XmlUtil.getElementTypeFile( rootFolder, namespace, elementTypeId );
      XmlMetaStoreElementType elementType = new XmlMetaStoreElementType( namespace, elementTypeFile );
      elementType.setMetaStoreName( getName() );
      return elementType;
    } finally {
      if ( lock ) {
        unlockStore();
      }
    }
  }

  public synchronized XmlMetaStoreElementType getElementType( String namespace, String elementTypeId )
    throws MetaStoreException {
    return getElementType( namespace, elementTypeId, true );
  }

  @Override
  public synchronized XmlMetaStoreElementType getElementTypeByName( String namespace, String elementTypeName )
    throws MetaStoreException {
    for ( IMetaStoreElementType elementType : getElementTypes( namespace ) ) {
      if ( elementType.getName() != null && elementType.getName().equalsIgnoreCase( elementTypeName ) ) {
        return (XmlMetaStoreElementType) elementType;
      }
    }
    return null;
  }

  public IMetaStoreAttribute newAttribute( String id, Object value ) throws MetaStoreException {
    return new XmlMetaStoreAttribute( id, value );
  }

  @Override
  public synchronized void createElementType( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException, MetaStoreElementTypeExistsException {
    lockStore();
    try {
      // In the case of a file, the ID is the name
      //
      if ( elementType.getId() == null ) {
        elementType.setId( elementType.getName() );
      }

      String elementTypeFolder = XmlUtil.getElementTypeFolder( rootFolder, namespace, elementType.getId() );
      File elementTypeFolderFile = new File( elementTypeFolder );
      if ( elementTypeFolderFile.exists() ) {
        throw new MetaStoreElementTypeExistsException( getElementTypes( namespace, false ),
            "The specified element type already exists with the same ID" );
      }
      if ( !elementTypeFolderFile.mkdir() ) {
        throw new MetaStoreException( "Unable to create XML meta store element type folder '" + elementTypeFolder + "'" );
      }

      String elementTypeFilename = XmlUtil.getElementTypeFile( rootFolder, namespace, elementType.getId() );

      // Copy the element type information to the XML meta store
      //
      XmlMetaStoreElementType xmlType =
          new XmlMetaStoreElementType( namespace, elementType.getId(), elementType.getName(), elementType
              .getDescription() );
      xmlType.setFilename( elementTypeFilename );
      xmlType.save();
      xmlType.setMetaStoreName( getName() );
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void updateElementType( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    lockStore();
    try {
      String elementTypeFolder = XmlUtil.getElementTypeFolder( rootFolder, namespace, elementType.getId() );
      File elementTypeFolderFile = new File( elementTypeFolder );
      if ( !elementTypeFolderFile.exists() ) {
        throw new MetaStoreException( "The specified element type with ID '" + elementType.getId()
            + "' doesn't exists so we can't update it." );
      }

      String elementTypeFilename = XmlUtil.getElementTypeFile( rootFolder, namespace, elementType.getId() );

      // Save the element type information to the XML meta store
      //
      XmlMetaStoreElementType xmlType =
          new XmlMetaStoreElementType( namespace, elementType.getId(), elementType.getName(), elementType
              .getDescription() );
      xmlType.setFilename( elementTypeFilename );
      xmlType.save();
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void deleteElementType( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException, MetaStoreDependenciesExistsException {
    lockStore();
    try {
      String elementTypeFilename = XmlUtil.getElementTypeFile( rootFolder, namespace, elementType.getName() );
      File elementTypeFile = new File( elementTypeFilename );
      if ( !elementTypeFile.exists() ) {
        return;
      }
      // Check if the element type has no remaining elements
      List<IMetaStoreElement> elements = getElements( namespace, elementType, false );
      if ( !elements.isEmpty() ) {
        List<String> dependencies = new ArrayList<String>();
        for ( IMetaStoreElement element : elements ) {
          dependencies.add( element.getId() );
        }
        throw new MetaStoreDependenciesExistsException( dependencies, "Unable to delete element type with name '"
            + elementType.getName() + "' in namespace '" + namespace + "' because there are still elements present" );
      }

      // Remove the elementType.xml file
      //
      if ( !elementTypeFile.delete() ) {
        throw new MetaStoreException( "Unable to delete element type XML file '" + elementTypeFilename + "'" );
      }

      // Remove the folder too, should be empty by now.
      //
      String elementTypeFolder = XmlUtil.getElementTypeFolder( rootFolder, namespace, elementType.getName() );
      File elementTypeFolderFile = new File( elementTypeFolder );
      if ( !elementTypeFolderFile.delete() ) {
        throw new MetaStoreException( "Unable to delete element type XML folder '" + elementTypeFolder + "'" );
      }
    } finally {
      unlockStore();
    }
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    return getElements( namespace, elementType, true );
  }

  protected synchronized List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType,
      boolean lock ) throws MetaStoreException {
    if ( lock ) {
      lockStore();
    }
    try {
      List<IMetaStoreElement> elements = new ArrayList<IMetaStoreElement>();

      String elementTypeFolder = XmlUtil.getElementTypeFolder( rootFolder, namespace, elementType.getName() );
      File elementTypeFolderFile = new File( elementTypeFolder );
      File[] elementTypeFiles = listFiles( elementTypeFolderFile );
      for ( File elementTypeFile : elementTypeFiles ) {
        String elementId = elementTypeFile.getName();
        elementId = elementId.substring( 0, elementId.length() - 4 ); // remove .xml to get the ID
        elements.add( getElement( namespace, elementType, elementId, false ) );
      }

      return elements;
    } finally {
      if ( lock ) {
        unlockStore();
      }
    }
  }

  @Override
  public synchronized List<String> getElementIds( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    lockStore();
    try {
      List<String> elementIds = new ArrayList<String>();

      String elementTypeFolder = XmlUtil.getElementTypeFolder( rootFolder, namespace, elementType.getName() );
      File elementTypeFolderFile = new File( elementTypeFolder );
      File[] elementTypeFiles = listFiles( elementTypeFolderFile );
      for ( File elementTypeFile : elementTypeFiles ) {
        String elementId = elementTypeFile.getName();
        elementId = elementId.substring( 0, elementId.length() - 4 ); // remove .xml to get the ID
        elementIds.add( elementId );
      }

      return elementIds;
    } finally {
      unlockStore();
    }
  }

  @Override
  public IMetaStoreElement getElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    return getElement( namespace, elementType, elementId, true );
  }

  protected synchronized IMetaStoreElement getElement( String namespace, IMetaStoreElementType elementType,
      String elementId, boolean lock ) throws MetaStoreException {
    if ( lock ) {
      lockStore();
    }
    try {
      String elementFilename = XmlUtil.getElementFile( rootFolder, namespace, elementType.getName(), elementId );
      File elementFile = new File( elementFilename );
      if ( !elementFile.exists() ) {
        return null;
      }
      return new XmlMetaStoreElement( elementFilename );
    } finally {
      if ( lock ) {
        unlockStore();
      }
    }
  }

  @Override
  public IMetaStoreElement getElementByName( String namespace, IMetaStoreElementType elementType, String name )
    throws MetaStoreException {
    for ( IMetaStoreElement element : getElements( namespace, elementType ) ) {
      if ( element.getName() != null && element.getName().equalsIgnoreCase( name ) ) {
        return element;
      }
    }
    return null;
  }

  public synchronized void
    createElement( String namespace, IMetaStoreElementType elementType, IMetaStoreElement element )
      throws MetaStoreException, MetaStoreElementExistException {
    lockStore();
    try {
      // In the case of a file, the ID is the name
      //
      if ( element.getId() == null ) {
        element.setId( element.getName() );
      }

      String elementFilename = XmlUtil.getElementFile( rootFolder, namespace, elementType.getName(), element.getId() );
      File elementFile = new File( elementFilename );
      if ( elementFile.exists() ) {
        throw new MetaStoreElementExistException( getElements( namespace, elementType, false ),
            "The specified element already exists with the same ID: '" + element.getId() + "'" );
      }
      XmlMetaStoreElement xmlElement = new XmlMetaStoreElement( element );
      xmlElement.setFilename( elementFilename );
      xmlElement.save();

      // In the case of the XML store, the name is the same as the ID
      //
      element.setId( xmlElement.getName() );
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void updateElement( String namespace, IMetaStoreElementType elementType, String elementId,
      IMetaStoreElement element ) throws MetaStoreException {

    // verify that the element type belongs to this meta store
    //
    if ( elementType.getMetaStoreName() == null || !elementType.getMetaStoreName().equals( getName() ) ) {
      throw new MetaStoreException( "The element type '" + elementType.getName()
          + "' needs to explicitly belong to the meta store in which you are updating." );
    }

    lockStore();
    try {
      String elementFilename = XmlUtil.getElementFile( rootFolder, namespace, elementType.getName(), element.getName() );
      File elementFile = new File( elementFilename );
      if ( !elementFile.exists() ) {
        throw new MetaStoreException( "The specified element to update doesn't exist with ID: '" + elementId + "'" );
      }

      XmlMetaStoreElement xmlElement = new XmlMetaStoreElement( element );
      xmlElement.setFilename( elementFilename );
      xmlElement.setIdWithFilename( elementFilename );
      xmlElement.save();
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void deleteElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    lockStore();
    try {
      String elementFilename = XmlUtil.getElementFile( rootFolder, namespace, elementType.getName(), elementId );
      File elementFile = new File( elementFilename );
      if ( !elementFile.exists() ) {
        return;
      }

      if ( !elementFile.delete() ) {
        throw new MetaStoreException( "Unable to delete element with ID '" + elementId + "' in filename '"
            + elementFilename + "'" );
      }
    } finally {
      unlockStore();
    }
  }

  /**
   * @return the rootFolder
   */
  public String getRootFolder() {
    return rootFolder;
  }

  /**
   * @param rootFolder
   *          the rootFolder to set
   */
  public void setRootFolder( String rootFolder ) {
    this.rootFolder = rootFolder;
  }

  /**
   * @param folder
   * @return the non-hidden folders in the specified folder
   */
  protected File[] listFolders( File folder ) {
    File[] folders = folder.listFiles( new FileFilter() {
      @Override
      public boolean accept( File file ) {
        return !file.isHidden() && file.isDirectory();
      }
    } );
    if ( folders == null ) {
      folders = new File[] {};
    }
    return folders;
  }

  /**
   * @param folder
   * @return the non-hidden files in the specified folder
   */
  protected File[] listFiles( File folder ) {
    File[] files = folder.listFiles( new FileFilter() {
      @Override
      public boolean accept( File file ) {
        return !file.isHidden() && file.isFile();
      }
    } );
    if ( files == null ) {
      files = new File[] {};
    }
    return files;
  }

  @Override
  public IMetaStoreElementType newElementType( String namespace ) throws MetaStoreException {
    return new XmlMetaStoreElementType( namespace, null, null, null );
  }

  @Override
  public IMetaStoreElement newElement() throws MetaStoreException {
    return new XmlMetaStoreElement();
  }

  @Override
  public IMetaStoreElement newElement( IMetaStoreElementType elementType, String id, Object value )
    throws MetaStoreException {
    return new XmlMetaStoreElement( elementType, id, value );
  }

  @Override
  public IMetaStoreElementOwner newElementOwner( String name, MetaStoreElementOwnerType ownerType )
    throws MetaStoreException {
    return new XmlMetaStoreElementOwner( name, ownerType );
  }

  /**
   * Create a .lock file in the store root folder. If it already exists, wait until it becomes available.
   * 
   * @throws MetaStoreException
   *           in case we have to wait more than 10 seconds to acquire a lock
   */
  protected void lockStore() throws MetaStoreException {
    boolean waiting = true;
    long totalTime = 0L;
    while ( waiting ) {
      File lockFile = new File( rootFile, ".lock" );
      try {
        if ( lockFile.createNewFile() ) {
          return;
        }
      } catch ( IOException e ) {
        throw new MetaStoreException( "Unable to create lock file: " + lockFile.toString(), e );
      }
      try {
        Thread.sleep( 100 );
      } catch ( InterruptedException e ) {
        throw new RuntimeException( e );
      }
      totalTime += 100;
      if ( totalTime > 10000 ) {
        throw new MetaStoreException( "Maximum wait time of 10 seconds exceed while acquiring lock" );
      }
    }
  }

  protected void unlockStore() {
    File lockFile = new File( rootFile, ".lock" );
    lockFile.delete();
  }
}
