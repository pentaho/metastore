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
 * Copyright (c) 2002-2023 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.metastore.stores.xml;

import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class XmlMetaStore extends BaseXmlMetaStore<File> {

  private File rootFile;

  public XmlMetaStore() throws MetaStoreException {
    this( defaultCache() );
  }

  public XmlMetaStore( XmlMetaStoreCache metaStoreCacheImpl ) throws MetaStoreException {
    this( System.getProperty( "java.io.tmpdir" ) + File.separator + UUID.randomUUID(), metaStoreCacheImpl );
  }

  /**
   *
   * @param rootFolder
   *          absolute path to the root folder of the metastore
   */
  public XmlMetaStore( String rootFolder ) throws MetaStoreException {
    this( rootFolder, defaultCache() );
  }

  /**
   *
   *
   *
   * @param rootFolder
   *          absolute path to the root folder of the metastore
   * @param metaStoreCacheImpl
   */
  public XmlMetaStore( String rootFolder, XmlMetaStoreCache metaStoreCacheImpl ) throws MetaStoreException {
    super( rootFolder, metaStoreCacheImpl );

    rootFile = new File( getRootFolder() );
    if ( !rootFile.exists() ) {
      if ( !rootFile.mkdirs() ) {
        throw new MetaStoreException( "Unable to create XML meta store root folder: " + getRootFolder() );
      }
    }

    // Give the MetaStore a default name
    //
    setName( getRootFolder() );
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
  protected List<String> listFolders( String folder ) {
    File folderFile = new File( folder );

    File[] folders = folderFile.listFiles( file -> !file.isHidden() && file.isDirectory() );
    if ( folders == null ) {
      return Collections.emptyList();
    }
    List<String> folderNames = new ArrayList<>( folders.length );
    for ( File curFolder : folders ) {
      folderNames.add( curFolder.getName() );
    }

    return folderNames;
  }

  @Override
  protected boolean pathExists( String path ) {
    File pathFile = new File( path );
    return pathFile.exists();
  }

  @Override
  protected boolean createDirectory( String path ) throws MetaStoreException {
    File pathFile = new File( path );
    return pathFile.mkdir();
  }

  @Override
  protected boolean deletePath( String path ) throws MetaStoreException {
    File pathFile = new File( path );
    return pathFile.delete();
  }

  @Override
  protected long lastModified( String path ) throws MetaStoreException {
    File pathFile = new File( path );
    return pathFile.lastModified();
  }

  @Override
  protected List<File> listFiles( String folder, Map<String, Long> processedFiles ) {
    File folderFile = new File( folder );
    File[] files = folderFile.listFiles( file -> {
      Long fileLastModified = processedFiles.get( file.getPath() );
      if ( fileLastModified != null && fileLastModified.equals( file.lastModified() ) ) {
        return false;
      }
      return !file.isHidden() && file.isFile();
    } );
    if ( files == null ) {
      files = new File[] {};
    }
    return Arrays.asList( files );
  }

  @Override
  protected String getFilename( File file ) {
    return file.getName();
  }

  @Override
  public XmlMetaStoreElementType newElementTypeFromFile( String namespace, String filename ) throws MetaStoreException {
    return new XmlMetaStoreElementType( namespace, filename );
  }

  @Override
  protected XmlMetaStoreElementType newElementType( String namespace, String id, String name, String description,
      String filename ) {
    XmlMetaStoreElementType type = new XmlMetaStoreElementType( namespace, id, name, description );
    type.setFilename( filename );
    return type;
  }

  // this is from IMetaStore
  @Override
  public XmlMetaStoreElementType newElementType( String namespace ) {
    XmlMetaStoreElementType type = new XmlMetaStoreElementType( namespace, null, null, null );
    return type;
  }

  @Override
  public XmlMetaStoreElement newElement() throws MetaStoreException {
    return new XmlMetaStoreElement();
  }

  @Override
  public XmlMetaStoreElement newElement( IMetaStoreElementType elementType, String id, Object value )
    throws MetaStoreException {
    return new XmlMetaStoreElement( elementType, id, value );
  }

  @Override
  protected XmlMetaStoreElement newElement( String filename ) throws MetaStoreException {
    return new XmlMetaStoreElement( filename );
  }

  protected XmlMetaStoreElement newElement( IMetaStoreElement element ) throws MetaStoreException {
    return new XmlMetaStoreElement( element );
  }

  @Override
  public XmlMetaStoreElementOwner newElementOwner( String name, MetaStoreElementOwnerType ownerType )
    throws MetaStoreException {
    return new XmlMetaStoreElementOwner( name, ownerType );
  }

  /**
   * Create a .lock file in the store root folder. If it already exists, wait until it becomes available.
   * 
   * @throws MetaStoreException
   *           in case we have to wait more than 10 seconds to acquire a lock
   */
  @Override
  protected void lockStore() throws MetaStoreException {
    boolean waiting = true;
    long totalTime = 0L;
    while ( waiting ) {
      File lockFile = new File( rootFile, ".lock" );
      try {
        // PDI-19756: make fewer calls to createNewFile() to prevent random Windows error
        if ( !lockFile.exists() && lockFile.createNewFile() ) {
          return;
        }
      } catch ( IOException e ) {
        // PDI-19756: Due to known issue with createNewFile()
        // we are trying to understand if the exception is due to lack of permissions or just a random fail
        if ( e.getMessage().contains( "Access is denied" ) && Files.isWritable( Paths.get( rootFile.getPath() ) ) ) {
          continue;
        }
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

  @Override
  protected void unlockStore() {
    File lockFile = new File( rootFile, ".lock" );
    lockFile.delete();
  }
}
