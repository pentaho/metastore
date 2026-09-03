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
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stores metastore data in XML files.
 */
public class XmlMetaStore extends BaseXmlMetaStore<File> {

  /**
   * Creates an XML metastore in a temporary folder.
   *
   * @throws MetaStoreException if the temporary metastore folder cannot be created
   */
  public XmlMetaStore() throws MetaStoreException {
    this( defaultCache() );
  }

  /**
   * Creates an XML metastore in a temporary folder with a cache.
   *
   * @param metaStoreCacheImpl the cache implementation
   * @throws MetaStoreException if the temporary metastore folder cannot be created
   */
  public XmlMetaStore( XmlMetaStoreCache metaStoreCacheImpl ) throws MetaStoreException {
    this( Paths.get( System.getProperty( "java.io.tmpdir" ) ).resolve( UUID.randomUUID().toString() ).toString(),
        metaStoreCacheImpl );
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

    Path rootPath = getRootFolderPath();
    try {
      if ( !Files.exists( rootPath ) ) {
        Files.createDirectories( rootPath );
      }
    } catch ( IOException e ) {
      throw new MetaStoreException( "Unable to create XML meta store root folder: " + getRootFolder(), e );
    }

    // Give the MetaStore a default name
    //
    setName( getRootFolder() );
  }

  /**
   * Compares stores by name without case sensitivity.
   *
   * @param obj the object to compare
   * @return {@code true} when both stores have the same name
   */
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

  /**
   * Returns a hash based on the store name without case sensitivity.
   *
   * @return the store name hash
   */
  @Override
  public int hashCode() {
    if ( name == null ) {
      return 0;
    }

    int hash = 0;
    for ( int index = 0; index < name.length(); index++ ) {
      char character = name.charAt( index );
      hash = 31 * hash + Character.toUpperCase( Character.toLowerCase( character ) );
    }
    return hash;
  }

  @Override
  protected List<String> listFolders( String folder ) {
    Path folderPath = Paths.get( folder );
    List<String> folderNames = new ArrayList<>();
    try ( DirectoryStream<Path> paths = Files.newDirectoryStream( folderPath ) ) {
      for ( Path path : paths ) {
        if ( isVisibleDirectory( path ) ) {
          folderNames.add( path.getFileName().toString() );
        }
      }
    } catch ( IOException e ) {
      return Collections.emptyList();
    }
    return folderNames;
  }

  private boolean isVisibleDirectory( Path path ) {
    try {
      return Files.isDirectory( path ) && !Files.isHidden( path );
    } catch ( IOException e ) {
      return false;
    }
  }

  @Override
  protected boolean pathExists( String path ) {
    return Files.exists( Paths.get( path ) );
  }

  @Override
  protected boolean createDirectory( String path ) throws MetaStoreException {
    try {
      Files.createDirectory( Paths.get( path ) );
      return true;
    } catch ( IOException e ) {
      return false;
    }
  }

  @Override
  protected boolean deletePath( String path ) throws MetaStoreException {
    try {
      return Files.deleteIfExists( Paths.get( path ) );
    } catch ( IOException e ) {
      return false;
    }
  }

  @Override
  protected long lastModified( String path ) throws MetaStoreException {
    try {
      return Files.getLastModifiedTime( Paths.get( path ) ).toMillis();
    } catch ( IOException e ) {
      return 0L;
    }
  }

  @Override
  protected List<File> listFiles( String folder, Map<String, Long> processedFiles ) {
    Path folderPath = Paths.get( folder );
    List<File> files = new ArrayList<>();
    try ( DirectoryStream<Path> paths = Files.newDirectoryStream( folderPath ) ) {
      for ( Path path : paths ) {
        if ( isUnprocessedFile( path, processedFiles ) ) {
          files.add( path.toFile() );
        }
      }
    } catch ( IOException e ) {
      return files;
    }
    return files;
  }

  private boolean isUnprocessedFile( Path path, Map<String, Long> processedFiles ) {
    try {
      if ( Files.isHidden( path ) || !Files.isRegularFile( path )
          || !path.getFileName().toString().endsWith( ".xml" ) ) {
        return false;
      }
      Long fileLastModified = processedFiles.get( path.toString() );
      return fileLastModified == null
          || !fileLastModified.equals( Files.getLastModifiedTime( path ).toMillis() );
    } catch ( IOException e ) {
      return false;
    }
  }

  @Override
  protected String getFilename( File file ) {
    return file.getName();
  }

  /**
   * Loads an element type from an XML file.
   *
   * @param namespace the element type namespace
   * @param filename the XML file path
   * @return the loaded element type
   * @throws MetaStoreException if the file cannot be loaded
   */
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

  @Override
  public XmlMetaStoreElementType newElementType( String namespace ) {
    return new XmlMetaStoreElementType( namespace, null, null, null );
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
    long totalTime = 0L;
    while ( true ) {
      Path lockFile = getRootFolderPath().resolve( ".lock" );
      try {
        // PDI-19756: make fewer calls to createNewFile() to prevent random Windows error
        if ( Files.notExists( lockFile ) ) {
          Files.createFile( lockFile );
          return;
        }
      } catch ( IOException e ) {
        // PDI-19756: Due to known issue with createNewFile()
        // we are trying to understand if the exception is due to lack of permissions or just a random fail
        if ( e.getMessage() != null && e.getMessage().contains( "Access is denied" )
            && Files.isWritable( lockFile.getParent() ) ) {
          continue;
        }
      }
      try {
        Thread.sleep( 100 );
      } catch ( InterruptedException e ) {
        Thread.currentThread().interrupt();
        throw new RuntimeException( e );
      }
      totalTime += 100;
      if ( totalTime > 10000 ) {
        throw new MetaStoreException( "Maximum wait time of 10 seconds exceed while acquiring lock. "
          + "If there is only one instance of this application running, "
          + "try deleting the '.lock' file in the metastore folder if the problem reoccurs on startup" );
      }
    }
  }

  @Override
  protected void unlockStore() {
    try {
      Files.deleteIfExists( getRootFolderPath().resolve( ".lock" ) );
    } catch ( IOException ignored ) {
      // Preserve legacy behavior: ignore lock cleanup failures.
    }
  }
}
