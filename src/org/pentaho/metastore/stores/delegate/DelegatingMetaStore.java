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
import org.pentaho.metastore.api.security.Base64TwoWayPasswordEncoder;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.ITwoWayPasswordEncoder;
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
  protected List<IMetaStore> metaStoreList;
  
  /** The active metastore */
  protected String activeMetaStoreName;
  
  /** The two way password encoder to use */
  protected ITwoWayPasswordEncoder passwordEncoder;
  
  public DelegatingMetaStore() {
    metaStoreList = new ArrayList<IMetaStore>();
    passwordEncoder = new Base64TwoWayPasswordEncoder();
  }

  public DelegatingMetaStore(IMetaStore...stores) {
    metaStoreList = new ArrayList<IMetaStore>(Arrays.asList(stores));
  }

  public void addMetaStore(IMetaStore metaStore) throws MetaStoreException {
    metaStoreList.add(metaStore);
    setActiveMetaStoreName(metaStore.getName());
  }
  
  public void addMetaStore(int index, IMetaStore metaStore) throws MetaStoreException {
    metaStoreList.add(index, metaStore);
    setActiveMetaStoreName(metaStore.getName());
  }
  
  public void removeMetaStore(IMetaStore metaStore) throws MetaStoreException {
    metaStoreList.remove(metaStore);
    if (activeMetaStoreName!=null && metaStore.getName().equalsIgnoreCase(activeMetaStoreName)) {
      activeMetaStoreName = null;
    }
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
  public boolean namespaceExists(String namespace) throws MetaStoreException {
    IMetaStore activeMetaStore = getActiveMetaStore();
    return activeMetaStore.namespaceExists(namespace);
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
  
  private IMetaStoreElementType getElementTypeByName(List<IMetaStoreElementType> types, String name) {
    for (IMetaStoreElementType type : types) {
      if (type.getName().equalsIgnoreCase(name)) {
        return type;
      }
    }
    return null;
  }

  @Override
  public List<IMetaStoreElementType> getElementTypes(String namespace) throws MetaStoreException {
    List<IMetaStoreElementType> elementTypes = new ArrayList<IMetaStoreElementType>();
    for (IMetaStore metaStore : metaStoreList) {
      for (IMetaStoreElementType elementType :  metaStore.getElementTypes(namespace)) {
        if (getElementTypeByName(elementTypes, elementType.getName())==null) {
          elementTypes.add(elementType);
        }
      }
    }
    return elementTypes;
  }

  @Override
  public List<String> getElementTypeIds(String namespace) throws MetaStoreException {
    List<String> elementTypeIds = new ArrayList<String>();
    for (IMetaStoreElementType elementType :  getElementTypes(namespace)) {
      elementTypeIds.add(elementType.getId());
    }
    return elementTypeIds;
  }

  @Override
  public IMetaStoreElementType getElementType(String namespace, String elementTypeId) throws MetaStoreException {
    for (IMetaStoreElementType type : getElementTypes(namespace)) {
      if (type.getId().equals(elementTypeId)) {
        return type;
      }
    }
    return null;
  }
  
  @Override
  public IMetaStoreElementType getElementTypeByName(String namespace, String elementTypeName) throws MetaStoreException {
    return getElementTypeByName(getElementTypes(namespace), elementTypeName);
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
  public void deleteElementType(String namespace, IMetaStoreElementType elementType) throws MetaStoreException, MetaStoreDependenciesExistsException {
    IMetaStore metaStore = getActiveMetaStore();
    metaStore.deleteElementType(namespace, elementType);
  }

  private IMetaStoreElement getElementByName(List<IMetaStoreElement> elements, String name) {
    for (IMetaStoreElement element : elements) {
      if (element.getName().equalsIgnoreCase(name)) {
        return element;
      }
    }
    return null;
  }
  @Override
  public List<IMetaStoreElement> getElements(String namespace, IMetaStoreElementType elementType) throws MetaStoreException {
    List<IMetaStoreElement> elements = new ArrayList<IMetaStoreElement>();
    for (IMetaStore metaStore : metaStoreList) {
      for (IMetaStoreElement element :  metaStore.getElements(namespace, elementType)) {
        if (getElementByName(elements, element.getName())==null) {
          elements.add(element);
        }
      }
    }
    return elements;
  }

  @Override
  public List<String> getElementIds(String namespace, IMetaStoreElementType elementType) throws MetaStoreException {
    List<String> elementIds = new ArrayList<String>();
    for (IMetaStoreElement element : getElements(namespace, elementType)) {
      elementIds.add(element.getId());
    }
    return elementIds;
  }

  @Override
  public IMetaStoreElement getElement(String namespace, IMetaStoreElementType elementType, String elementId) throws MetaStoreException {
    for (IMetaStoreElement element : getElements(namespace, elementType)) {
      if (element.getId().equals(elementId)) {
        return element;
      }
    }
    return null;
  }
  
  @Override
  public IMetaStoreElement getElementByName(String namespace, IMetaStoreElementType elementType, String name) throws MetaStoreException {
    return getElementByName(getElements(namespace, elementType), name);
  }

  @Override
  public void createElement(String namespace, IMetaStoreElementType elementType, IMetaStoreElement element) throws MetaStoreException, MetaStoreElementExistException {
    getActiveMetaStore().createElement(namespace, elementType, element);
  }

  @Override
  public void deleteElement(String namespace, IMetaStoreElementType elementType, String elementId) throws MetaStoreException {
    getActiveMetaStore().deleteElement(namespace, elementType, elementId);
  }

  @Override
  public void updateElement(String namespace, IMetaStoreElementType elementType, String elementId, IMetaStoreElement element) throws MetaStoreException {
    getActiveMetaStore().updateElement(namespace, elementType, elementId, element);
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
  public IMetaStoreElement newElement(IMetaStoreElementType elementType, String id, Object value) throws MetaStoreException {
    return getActiveMetaStore().newElement(elementType, id, value);
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
  public void setTwoWayPasswordEncoder(ITwoWayPasswordEncoder encoder) {
    this.passwordEncoder = encoder;
  }

  @Override
  public ITwoWayPasswordEncoder getTwoWayPasswordEncoder() {
    return passwordEncoder;
  }
}
