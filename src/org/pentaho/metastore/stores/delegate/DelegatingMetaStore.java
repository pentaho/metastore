package org.pentaho.metastore.stores.delegate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

/**
 * This class can be used as a wrapper around one or more meta stores.
 * For example, if you have a local XML metastore, a workgroup database metastore and an enterprise EE metastore, 
 * you can put them in reverse order in the meta stores list.
 * 
 * There are 2 ways to work with the delegating meta store.  The first is if you set an active meta store.
 * That way, it works as if you're working with the specified meta store.
 * 
 * If you didn't specify an active store, all namespaces and elements in all listed meta stores are considered. 
 * 
 * That way, if you ask for the list of elements, you will get a unique list (by element ID) based on all stores.
 * 
 * @author matt
 *
 */
public class DelegatingMetaStore implements IMetaStore {
  
  /** Maps the name of the metastore to the physical implementation */
  private List<IMetaStore> metaStoreList;
  
  /** The active metastore */
  private String activeMetaStoreName;
  
  public DelegatingMetaStore() {
    metaStoreList = new ArrayList<IMetaStore>();
  }

  public DelegatingMetaStore(IMetaStore...stores) {
    metaStoreList = new ArrayList<IMetaStore>(Arrays.asList(stores));
  }

  public void addMetaStore(IMetaStore metaStore) {
    metaStoreList.add(metaStore);
  }
  
  public void addMetaStore(int index, IMetaStore metaStore) {
    metaStoreList.add(index, metaStore);
  }
  
  public void removeMetaStore(IMetaStore metaStore) {
    metaStoreList.remove(metaStore);
  }
  
  public List<IMetaStore> getMetaStoreList() {
    return metaStoreList;
  }

  public void setMetaStoreList(List<IMetaStore> metaStoreList) {
    this.metaStoreList = metaStoreList;
  }
  
  public void removeMetaStore(String metaStoreName) throws MetaStoreException {
    for (Iterator<IMetaStore> it = metaStoreList.iterator(); it.hasNext();) {
      IMetaStore metaStore = it.next();
      if (metaStore.getName().equalsIgnoreCase(metaStoreName)) {
        it.remove();
        return;
      }
    }
    throw new MetaStoreException("Unable to find meta store with name '"+metaStoreName+"'");
  }
  
  
  public void setActiveMetaStoreName(String activeMetaStoreName) {
    this.activeMetaStoreName = activeMetaStoreName;
  }
  
  public String getActiveMetaStoreName() {
    return activeMetaStoreName;
  }
  
  public IMetaStore getActiveMetaStore() throws MetaStoreException {
    if (activeMetaStoreName==null) {
      throw new MetaStoreException("The active meta store has not been selected");
    }
    IMetaStore metaStore = getMetaStore(activeMetaStoreName);
    return metaStore;
  }

  public IMetaStore getMetaStore(String metaStoreName) throws MetaStoreException {
    for (IMetaStore metaStore : metaStoreList) {
      if (metaStore.getName().equalsIgnoreCase(metaStoreName)) {
        return metaStore;
      }
    }
    throw new MetaStoreException("Unable to find meta store with name '"+metaStoreName+"'");
  }

  @Override
  public List<String> getNamespaces() throws MetaStoreException {
    List<String> namespaces = new ArrayList<String>();
    for (IMetaStore metaStore : metaStoreList) {
      for (String namespace : metaStore.getNamespaces()) {
        if (!namespaces.contains(namespace)) {
          namespaces.add(namespace);
        }
      }
    }
    return namespaces;
  }

  @Override
  public void createNamespace(String namespace) throws MetaStoreException, MetaStoreNamespaceExistsException {
    IMetaStore metaStore = getActiveMetaStore();
    metaStore.createNamespace(namespace);
  }

  @Override
  public void deleteNamespace(String namespace) throws MetaStoreException {
    IMetaStore metaStore = getActiveMetaStore();
    metaStore.deleteNamespace(namespace);
  }

  @Override
  public List<IMetaStoreElementType> getElementTypes(String namespace) throws MetaStoreException {
    if (activeMetaStoreName==null) {
      List<IMetaStoreElementType> elementTypes = new ArrayList<IMetaStoreElementType>();
      for (IMetaStore metaStore : metaStoreList) {
        for (IMetaStoreElementType elementType :  metaStore.getElementTypes(namespace)) {
          if (!elementTypes.contains(elementType)) {
            elementTypes.add(elementType);
          }
        }
      }
      return elementTypes;
    } else {
      return getActiveMetaStore().getElementTypes(namespace);
    }
  }

  @Override
  public List<String> getElementTypeIds(String namespace) throws MetaStoreException {
    if (activeMetaStoreName==null) {
      List<String> elementTypeIds = new ArrayList<String>();
      for (IMetaStore metaStore : metaStoreList) {
        for (IMetaStoreElementType elementType :  metaStore.getElementTypes(namespace)) {
          if (!elementTypeIds.contains(elementType.getId())) {
            elementTypeIds.add(elementType.getId());
          }
        }
      }
      return elementTypeIds;
    } else {
      return getActiveMetaStore().getElementTypeIds(namespace);
    }
  }

  @Override
  public IMetaStoreElementType getElementType(String namespace, String elementTypeId) throws MetaStoreException {
    if (activeMetaStoreName==null) {
      for (IMetaStore metaStore : metaStoreList) {
        IMetaStoreElementType elementType = metaStore.getElementType(namespace, elementTypeId);
        if (elementType!=null) {
          return elementType;
        }
      }
      return null;
    } else {
      IMetaStore metaStore = getActiveMetaStore();
      return metaStore.getElementType(namespace, elementTypeId);
    }
  }
  
  @Override
  public IMetaStoreElementType getElementTypeByName(String namespace, String elementTypeName) throws MetaStoreException {
    for (IMetaStoreElementType elementType : getElementTypes(namespace)) {
      if (elementType.getName().equalsIgnoreCase(elementTypeName)) {
        return elementType;
      }
    }
    return null;
  }

  @Override
  public void createElementType(String namespace, IMetaStoreElementType elementType) throws MetaStoreException, MetaStoreElementTypeExistsException {
    IMetaStore metaStore = getActiveMetaStore();
    metaStore.createElementType(namespace, elementType);
  }

  @Override
  public void updateElementType(String namespace, IMetaStoreElementType elementType) throws MetaStoreException {
    IMetaStore metaStore = getActiveMetaStore();
    metaStore.updateElementType(namespace, elementType);
  }

  @Override
  public void deleteElementType(String namespace, String elementTypeId) throws MetaStoreException, MetaStoreDependenciesExistsException {
    IMetaStore metaStore = getActiveMetaStore();
    metaStore.deleteElementType(namespace, elementTypeId);
  }

  @Override
  public List<IMetaStoreElement> getElements(String namespace, String elementTypeId) throws MetaStoreException {
    if (activeMetaStoreName==null) {
      List<IMetaStoreElement> elements = new ArrayList<IMetaStoreElement>();
      for (IMetaStore metaStore : metaStoreList) {
        for (IMetaStoreElement element :  metaStore.getElements(namespace, elementTypeId)) {
          if (!elements.contains(element)) {
            elements.add(element);
          }
        }
      }
      return elements;
    } else {
      return getActiveMetaStore().getElements(namespace, elementTypeId);
    }
  }

  @Override
  public List<String> getElementIds(String namespace, String elementTypeId) throws MetaStoreException {
    if (activeMetaStoreName==null) {
      List<String> elementIds = new ArrayList<String>();
      for (IMetaStore metaStore : metaStoreList) {
        for (String id :  metaStore.getElementIds(namespace, elementTypeId)) {
          if (!elementIds.contains(id)) {
            elementIds.add(id);
          }
        }
      }
      return elementIds;
    } else {
      return getActiveMetaStore().getElementIds(namespace, elementTypeId);
    }
  }

  @Override
  public IMetaStoreElement getElement(String namespace, String elementTypeId, String elementId) throws MetaStoreException {
    if (activeMetaStoreName==null) {
      for (IMetaStore metaStore : metaStoreList) {
        IMetaStoreElement element = metaStore.getElement(namespace, elementTypeId, elementId);
        if (element!=null) {
          return element;
        }
      }
      return null;
    } else {
      return getActiveMetaStore().getElement(namespace, elementTypeId, elementId);
    }
  }
  
  @Override
  public IMetaStoreElement getElementByName(String namespace, IMetaStoreElementType elementType, String name) throws MetaStoreException {
    if (activeMetaStoreName==null) {
      for (IMetaStore metaStore : metaStoreList) {
        for (IMetaStoreElement element : metaStore.getElements(namespace, elementType.getId())) {
          if (element.getName()!=null && element.getName().equalsIgnoreCase(name)) {
            return element;
          }
        }
      }
    } else {
      for (IMetaStoreElement element : getElements(namespace, elementType.getId())) {
        if (element.getName()!=null && element.getName().equalsIgnoreCase(name)) {
          return element;
        }
      }
    }
    return null;
  }

  @Override
  public void createElement(String namespace, String elementTypeId, IMetaStoreElement element) throws MetaStoreException, MetaStoreElementExistException {
    getActiveMetaStore().createElement(namespace, elementTypeId, element);
  }

  @Override
  public void deleteElement(String namespace, String elementTypeId, String elementId) throws MetaStoreException {
    getActiveMetaStore().deleteElement(namespace, elementTypeId, elementId);
  }

  @Override
  public IMetaStoreElementType newElementType(String namespace) throws MetaStoreException {
    return getActiveMetaStore().newElementType(namespace);
  }

  @Override
  public IMetaStoreElement newElement() throws MetaStoreException {
    return getActiveMetaStore().newElement();
  }

  @Override
  public IMetaStoreElement newElement(String id, Object value) throws MetaStoreException {
    return getActiveMetaStore().newElement(id, value);
  }
  
  public IMetaStoreAttribute newAttribute(String id, Object value) throws MetaStoreException {
    return getActiveMetaStore().newAttribute(id, value);
  }


  @Override
  public IMetaStoreElementOwner newElementOwner(String name, MetaStoreElementOwnerType ownerType) throws MetaStoreException {
    return getActiveMetaStore().newElementOwner(name, ownerType);
  }


  @Override
  public String getName() throws MetaStoreException {
    return getActiveMetaStore().getName();
  }

  @Override
  public String getDescription() throws MetaStoreException {
    return getActiveMetaStore().getDescription();
  }

  @Override
  public String getLifeCycle() throws MetaStoreException {
    return getActiveMetaStore().getLifeCycle();
  }

  @Override
  public String getCustomerName() throws MetaStoreException {
    return getActiveMetaStore().getCustomerName();
  }

  @Override
  public String getProjectName() throws MetaStoreException {
    return getActiveMetaStore().getProjectName();
  }

  
/*
  @Override
  public void addElementTypeListener(MetaStoreElementTypeListener elementTypeListener) throws MetaStoreException {
    getActiveMetaStore().addElementTypeListener(elementTypeListener);
  }

  @Override
  public List<MetaStoreElementTypeListener> getElementTypeListeners() throws MetaStoreException {
    return getActiveMetaStore().getElementTypeListeners();
  }

  @Override
  public void removeElementTypeListener(MetaStoreElementTypeListener elementTypeListener) throws MetaStoreException {
    getActiveMetaStore().removeElementTypeListener(elementTypeListener);
  }

  @Override
  public void addElementListener(MetaStoreElementListener listener) throws MetaStoreException {
    getActiveMetaStore().addElementListener(listener);
  }

  @Override
  public List<MetaStoreElementListener> getElementListeners() throws MetaStoreException {
    return getActiveMetaStore().getElementListeners();
  }

  @Override
  public void removeElementListener(MetaStoreElementListener elementListener) throws MetaStoreException {
    getActiveMetaStore().removeElementListener(elementListener);
  }
*/
}
