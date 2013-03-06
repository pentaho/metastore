package org.pentaho.metastore.stores.xml;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.metastore.api.BaseMetaStore;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementExistException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.api.listeners.MetaStoreElementTypeListener;
import org.pentaho.metastore.api.listeners.MetaStoreElementListener;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;

public class XmlMetaStore extends BaseMetaStore implements IMetaStore {
  
  private String rootFolder;

  private File rootFile;

  public XmlMetaStore() throws MetaStoreException {
    this(System.getProperty("java.io.tmpdir"));
  }
  
  public XmlMetaStore(String rootFolder) throws MetaStoreException {
    this.rootFolder = rootFolder+File.separator+XmlUtil.META_FOLDER_NAME;
    
    rootFile = new File(this.rootFolder);
    if (!rootFile.exists()) {
      if (!rootFile.mkdir()) {
        throw new MetaStoreException("Unable to create XML meta store root folder: "+this.rootFolder);
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this==obj) return true;
    if (!(obj instanceof XmlMetaStore)) {
      return false;
    }
    return ((XmlMetaStore)obj).name.equalsIgnoreCase(name);
  }
  
  @Override
  public List<String> getNamespaces() throws MetaStoreException {
    
    File[] files = listFolders(rootFile);
    List<String> namespaces = new ArrayList<String>();
    for (File file : files) {
      namespaces.add(file.getName());
    }
    return namespaces;
  }


  @Override
  public void createNamespace(String namespace) throws MetaStoreException, MetaStoreNamespaceExistsException {
    String spaceFolder = XmlUtil.getNamespaceFolder(rootFolder, namespace);
    File spaceFile = new File(spaceFolder);
    if (spaceFile.exists()) {
      throw new MetaStoreNamespaceExistsException("The namespace with name '"+namespace+"' already exists.");
    }
    if (!spaceFile.mkdir()) {
      throw new MetaStoreException("Unable to create XML meta store namespace folder: "+spaceFolder);
    }
  }

  @Override
  public void deleteNamespace(String namespace) throws MetaStoreException, MetaStoreElementTypeExistsException {
    String spaceFolder = XmlUtil.getNamespaceFolder(rootFolder, namespace);
    File spaceFile = new File(spaceFolder);
    if (!spaceFile.exists()) {
      return; // Should we throw an exception?
    }
    List<IMetaStoreElementType> dataTypes = getElementTypes(namespace);
    
    if (!dataTypes.isEmpty()) {
      List<String> dependencies = new ArrayList<String>();
      for (IMetaStoreElementType dataType : dataTypes) {
        dependencies.add(dataType.getId());
      }
      throw new MetaStoreDependenciesExistsException(dependencies, "Unable to delete the XML meta store namespace with name '"+namespace+"' as it still contains dependencies");
    }
    
    if (!spaceFile.delete()) {
      throw new MetaStoreException("Unable to delete XML meta store namespace folder, check to see if it's empty");
    }
  }

  @Override
  public List<IMetaStoreElementType> getElementTypes(String namespace) throws MetaStoreException {
    List<IMetaStoreElementType> dataTypes = new ArrayList<IMetaStoreElementType>();
    
    String spaceFolder = XmlUtil.getNamespaceFolder(rootFolder, namespace);
    File spaceFolderFile = new File(spaceFolder);
    File[] dataTypeFolders = listFolders(spaceFolderFile);
    for (File dataTypeFolder : dataTypeFolders) {
      String dataTypeId = dataTypeFolder.getName();
      IMetaStoreElementType dataType = getElementType(namespace, dataTypeId);
      dataTypes.add(dataType);
    }
    
    return dataTypes;
  }
  
  @Override
  public List<String> getElementTypeIds(String namespace) throws MetaStoreException {
    List<String> ids = new ArrayList<String>();
    
    String spaceFolder = XmlUtil.getNamespaceFolder(rootFolder, namespace);
    File spaceFolderFile = new File(spaceFolder);
    File[] dataTypeFolders = listFolders(spaceFolderFile);
    for (File dataTypeFolder : dataTypeFolders) {
      String dataTypeId = dataTypeFolder.getName();
      ids.add(dataTypeId);
    }
    
    return ids;
  }
  
  @Override
  public IMetaStoreElementType getElementType(String namespace, String dataTypeId) throws MetaStoreException {
    String dataTypeFile = XmlUtil.getElementTypeFile(rootFolder, namespace, dataTypeId);
    XmlMetaStoreElementType dataType = new XmlMetaStoreElementType(namespace, dataTypeFile);
    return dataType;
  }

  @Override
  public void createElementType(String namespace, IMetaStoreElementType dataType) throws MetaStoreException, MetaStoreElementTypeExistsException {
    String dataTypeFolder = XmlUtil.getElementTypeFolder(rootFolder, namespace, dataType.getId());
    File dataTypeFolderFile = new File(dataTypeFolder);
    if (dataTypeFolderFile.exists()) {
      throw new MetaStoreElementTypeExistsException(getElementTypes(namespace), "The specified data type already exists with the same ID");
    }
    if (!dataTypeFolderFile.mkdir()) {
      throw new MetaStoreException("Unable to create XML meta store data type folder '"+dataTypeFolder+"'");
    }
    
    String dataTypeFilename = XmlUtil.getElementTypeFile(rootFolder, namespace, dataType.getId());
    
    // Copy the data type information to the XML meta store
    //
    XmlMetaStoreElementType xmlType = new XmlMetaStoreElementType(namespace, dataType.getId(), dataType.getName(), dataType.getDescription());
    xmlType.setFilename(dataTypeFilename);
    xmlType.save();
    
    // Inform the data type listeners of the creation
    //
    for (MetaStoreElementTypeListener listener : getElementTypeListeners()) {
      listener.dataTypeCreated(namespace, xmlType);
    }
  }
  
  @Override
  public void updateElementType(String namespace, IMetaStoreElementType dataType) throws MetaStoreException {
    String dataTypeFolder = XmlUtil.getElementTypeFolder(rootFolder, namespace, dataType.getId());
    File dataTypeFolderFile = new File(dataTypeFolder);
    if (!dataTypeFolderFile.exists()) {
      throw new MetaStoreException("The specified data type with ID '"+dataType.getId()+"' doesn't exists so we can't update it.");
    }
    
    String dataTypeFilename = XmlUtil.getElementTypeFile(rootFolder, namespace, dataType.getId());
    IMetaStoreElementType oldDataType = getElementType(namespace, dataType.getId());
    
    // Save the data type information to the XML meta store
    //
    XmlMetaStoreElementType xmlType = new XmlMetaStoreElementType(namespace, dataType.getId(), dataType.getName(), dataType.getDescription());
    xmlType.setFilename(dataTypeFilename);
    xmlType.save();
    
    // Inform the data type listeners of the update
    //
    for (MetaStoreElementTypeListener listener : getElementTypeListeners()) {
      listener.dataTypeUpdated(namespace, oldDataType, xmlType);
    }
  }


  @Override
  public void deleteElementType(String namespace, String dataTypeId) throws MetaStoreException, MetaStoreDependenciesExistsException {
    IMetaStoreElementType dataType = getElementType(namespace, dataTypeId);
    
    String dataTypeFilename = XmlUtil.getElementTypeFile(rootFolder, namespace, dataTypeId);
    File dataTypeFile = new File(dataTypeFilename);
    if (!dataTypeFile.exists()) {
      return;
    }
    // Check if the data type has no remaining entities
    List<IMetaStoreElement> entities = getElements(namespace, dataTypeId);
    if (!entities.isEmpty()) {
      List<String> dependencies = new ArrayList<String>();
      for (IMetaStoreElement entity : entities) {
        dependencies.add(entity.getId());
      }
      throw new MetaStoreDependenciesExistsException(dependencies, "Unable to delete data type with ID '"+dataTypeId+"' in namespace '"+namespace+"' because there are still entities present");
    }
    
    // Remove the datatype.xml file
    //
    if (!dataTypeFile.delete()) {
      throw new MetaStoreException("Unable to delete data type XML file '"+dataTypeFilename+"'");
    }
    
    // Remove the folder too, should be empty by now.
    //
    String dataTypeFolder = XmlUtil.getElementTypeFolder(rootFolder, namespace, dataTypeId);
    File dataTypeFolderFile = new File(dataTypeFolder);
    if (!dataTypeFolderFile.delete()) {
      throw new MetaStoreException("Unable to delete data type XML folder '"+dataTypeFolder+"'");
    }

    // Inform the data type listeners of the deletion
    //
    for (MetaStoreElementTypeListener listener : getElementTypeListeners()) {
      listener.dataTypeDeleted(namespace, dataType);
    }
  }
  
  @Override
  public List<IMetaStoreElement> getElements(String namespace, String dataTypeId) throws MetaStoreException {
    List<IMetaStoreElement> entities = new ArrayList<IMetaStoreElement>();
    
    String dataTypeFolder = XmlUtil.getElementTypeFolder(rootFolder, namespace, dataTypeId);
    File dataTypeFolderFile = new File(dataTypeFolder);
    File[] dataTypeFiles = listFiles(dataTypeFolderFile);
    for (File dataTypeFile : dataTypeFiles) {
      String entityId = dataTypeFile.getName();
      entityId=entityId.substring(0, entityId.length()-4); // remove .xml to get the ID
      entities.add(getElement(namespace, dataTypeId, entityId));
    }
    
    return entities;
  }
  
  @Override
  public List<String> getElementIds(String namespace, String dataTypeId) throws MetaStoreException {
    List<String> entityIds = new ArrayList<String>();
    
    String dataTypeFolder = XmlUtil.getElementTypeFolder(rootFolder, namespace, dataTypeId);
    File dataTypeFolderFile = new File(dataTypeFolder);
    File[] dataTypeFiles = listFiles(dataTypeFolderFile);
    for (File dataTypeFile : dataTypeFiles) {
      String entityId = dataTypeFile.getName();
      entityId=entityId.substring(0, entityId.length()-4); // remove .xml to get the ID
      entityIds.add(entityId);
    }
    
    return entityIds;
  }

  
  @Override
  public IMetaStoreElement getElement(String namespace, String dataTypeId, String entityId) throws MetaStoreException {
    String entityFilename = XmlUtil.getElementFile(rootFolder, namespace, dataTypeId, entityId);
    File entityFile = new File(entityFilename);
    if (!entityFile.exists()) {
      return null;
    }
    return new XmlMetaStoreElement(entityFilename);
  }
  
  public void createElement(String namespace, String dataTypeId, IMetaStoreElement entity) throws MetaStoreException, MetaStoreElementExistException {
    String entityFilename = XmlUtil.getElementFile(rootFolder, namespace, dataTypeId, entity.getId());
    File entityFile = new File(entityFilename);
    if (entityFile.exists()) {
      throw new MetaStoreElementExistException(getElements(namespace, dataTypeId), "The specified entity already exists with the same ID: '"+entity.getId()+"'");
    }
    XmlMetaStoreElement xmlEntity = new XmlMetaStoreElement(entity);
    xmlEntity.setFilename(entityFilename);
    xmlEntity.save();    
    
    // inform the listeners
    for (MetaStoreElementListener entityListener : getElementListeners()) {
      entityListener.elementCreated(namespace, dataTypeId, xmlEntity);
    }
  }
  
  public void updateElement(String namespace, String dataTypeId, IMetaStoreElement element) throws MetaStoreException {
    String elementFilename = XmlUtil.getElementFile(rootFolder, namespace, dataTypeId, element.getId());
    File elementFile = new File(elementFilename);
    if (!elementFile.exists()) {
      throw new MetaStoreException("The specified element to update doesn't exist with ID: '"+element.getId()+"'");
    }
    
    IMetaStoreElement oldElement = getElement(namespace, dataTypeId, element.getId());

    XmlMetaStoreElement xmlElement = new XmlMetaStoreElement(element);
    xmlElement.setFilename(elementFilename);
    xmlElement.save();    

    // inform the listeners after saving
    //
    for (MetaStoreElementListener entityListener : getElementListeners()) {
      entityListener.elementUpdated(namespace, dataTypeId, oldElement, element);
    }
  }
  

  @Override
  public void deleteElement(String namespace, String dataTypeId, String entityId) throws MetaStoreException {
    IMetaStoreElement entity = getElement(namespace, dataTypeId, entityId);
    
    String entityFilename = XmlUtil.getElementFile(rootFolder, namespace, dataTypeId, entityId);
    File entityFile = new File(entityFilename);
    if (!entityFile.exists()) {
      return;
    }
    
    if (!entityFile.delete()) {
      throw new MetaStoreException("Unable to delete entity with ID '"+entityId+"' in filename '"+entityFilename+"'");
    }

    // inform the listeners
    for (MetaStoreElementListener entityListener : getElementListeners()) {
      entityListener.elementDeleted(namespace, dataTypeId, entity);
    }
  }
  
  
  

  /**
   * @return the rootFolder
   */
  public String getRootFolder() {
    return rootFolder;
  }

  /**
   * @param rootFolder the rootFolder to set
   */
  public void setRootFolder(String rootFolder) {
    this.rootFolder = rootFolder;
  }

  /**
   * @param folder
   * @return the non-hidden folders in the specified folder
   */
  protected File[] listFolders(File folder) {
    return folder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return !file.isHidden() && file.isDirectory();
      }
    });  
  }

  /**
   * @param folder
   * @return the non-hidden files in the specified folder
   */
  protected File[] listFiles(File folder) {
    return folder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return !file.isHidden() && file.isFile();
      }
    });  
  }


  @Override
  public IMetaStoreElementType newElementType(String namespace) throws MetaStoreException {
    return new XmlMetaStoreElementType(namespace, null, null, null);
  }

  @Override
  public IMetaStoreElement newElement() throws MetaStoreException {
    return new XmlMetaStoreElement();
  }

  @Override
  public IMetaStoreElement newElement(String id, Object value) throws MetaStoreException {
    return new XmlMetaStoreElement(id, value);
  }
  
  @Override
  public IMetaStoreElementOwner newElementOwner(String name, MetaStoreElementOwnerType ownerType) throws MetaStoreException {
    return new XmlMetaStoreElementOwner(name, ownerType);
  }
}
