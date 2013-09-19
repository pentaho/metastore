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

package org.pentaho.metastore.stores.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class MemoryMetaStore extends BaseMetaStore implements IMetaStore {

  private Map<String, MemoryMetaStoreNamespace> namespacesMap;
  
  public MemoryMetaStore() {
    namespacesMap = new HashMap<String, MemoryMetaStoreNamespace>();
  }
  
  @Override
  public List<String> getNamespaces() throws MetaStoreException {
    return new ArrayList<String>(namespacesMap.keySet());
  }
  
  @Override
  public boolean namespaceExists(String namespace) throws MetaStoreException {
    return namespacesMap.get(namespace)!=null;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this==obj) return true;
    if (!(obj instanceof MemoryMetaStore)) {
      return false;
    }
    return ((MemoryMetaStore)obj).name.equalsIgnoreCase(name);
  }
  
  @Override
  public synchronized void createNamespace(String namespace) throws MetaStoreException, MetaStoreNamespaceExistsException {
    MemoryMetaStoreNamespace storeNamespace = namespacesMap.get(namespace);
    if (storeNamespace!=null) {
      throw new MetaStoreNamespaceExistsException("Unable to create namespace '"+namespace+"' as it already exist!");
    }
    storeNamespace = new MemoryMetaStoreNamespace(namespace);
    namespacesMap.put(namespace, storeNamespace);
  }

  @Override
  public synchronized void deleteNamespace(String namespace) throws MetaStoreException, MetaStoreDependenciesExistsException {
    if (namespacesMap.get(namespace)==null) {
      throw new MetaStoreException("Unable to delete namespace '"+namespace+"' as it doesn't exist");
    }
    List<IMetaStoreElementType> elementTypes = getElementTypes(namespace);
    if (elementTypes.isEmpty()) {
      namespacesMap.remove(namespace);
    } else {
      List<String> ids = new ArrayList<String>();
      for (IMetaStoreElementType type : elementTypes) {
        ids.add(type.getId());
      }
      throw new MetaStoreDependenciesExistsException(ids, "Namespace '"+namespace+"' is not empty!");
    }
  }

  @Override
  public synchronized List<IMetaStoreElementType> getElementTypes(String namespace) throws MetaStoreException {
    MemoryMetaStoreNamespace storeNamespace = namespacesMap.get(namespace);
    if (storeNamespace==null) {
      return new ArrayList<IMetaStoreElementType>();
    } else {
      return new ArrayList<IMetaStoreElementType>(storeNamespace.getTypeMap().values());
    }
  }

  @Override
  public synchronized IMetaStoreElementType getElementType(String namespace, String elementTypeId) throws MetaStoreException {
    MemoryMetaStoreNamespace storeNamespace = namespacesMap.get(namespace);
    if (storeNamespace!=null) {
      return storeNamespace.getTypeMap().get(elementTypeId);
    }
    return null;
  }
  
  @Override
  public synchronized IMetaStoreElementType getElementTypeByName(String namespace, String elementTypeName) throws MetaStoreException {
    MemoryMetaStoreNamespace storeNamespace = namespacesMap.get(namespace);
    if (storeNamespace!=null) {
      for (MemoryMetaStoreElementType elementType : storeNamespace.getTypeMap().values()) {
        if (elementType.getName().equalsIgnoreCase(elementTypeName)) {
          return elementType;
        }
      }
    }
    return null;
  }

  @Override
  public synchronized List<String> getElementTypeIds(String namespace) throws MetaStoreException {
    MemoryMetaStoreNamespace storeNamespace = namespacesMap.get(namespace);
    if (storeNamespace!=null) {
      ArrayList<String> list = new ArrayList<String>();
      for (MemoryMetaStoreElementType elementType : storeNamespace.getTypeMap().values()) {
        list.add(elementType.getId());
      }
      return list;
    } else {
      return new ArrayList<String>();
    }
  }

  @Override
  public synchronized void createElementType(String namespace, IMetaStoreElementType elementType) throws MetaStoreException, MetaStoreElementTypeExistsException {
    MemoryMetaStoreNamespace storeNamespace = namespacesMap.get(namespace);
    if (storeNamespace!=null) {
      
      // For the memory store, the ID is the same as the name if empty
      if (elementType.getId()==null) {
        elementType.setId(elementType.getName());
      }
      
      MemoryMetaStoreElementType verifyType = storeNamespace.getTypeMap().get(elementType.getId());
      if (verifyType!=null) {
        throw new MetaStoreElementTypeExistsException(getElementTypes(namespace), "Element type with ID '"+elementType.getId()+"' already exists");
      } else {
        MemoryMetaStoreElementType copiedType = new MemoryMetaStoreElementType(elementType);
        storeNamespace.getTypeMap().put(elementType.getId(), copiedType);
        copiedType.setMetaStoreName(getName());
        elementType.setMetaStoreName(getName());
      }
    } else {
      throw new MetaStoreException("Namespace '"+namespace+"' doesn't exist!");
    }
  }

  @Override
  public synchronized void updateElementType(String namespace, IMetaStoreElementType elementType) throws MetaStoreException {
    MemoryMetaStoreNamespace storeNamespace = namespacesMap.get(namespace);
    if (storeNamespace!=null) {
      MemoryMetaStoreElementType verifyType = storeNamespace.getTypeMap().get(elementType.getId());
      if (verifyType==null) {
        throw new MetaStoreElementTypeExistsException(getElementTypes(namespace), "Element type to update, with ID '"+elementType.getId()+"', does not exist");
      } else {
        MemoryMetaStoreElementType copiedType = new MemoryMetaStoreElementType(elementType);
        storeNamespace.getTypeMap().put(elementType.getId(), copiedType);
        copiedType.setMetaStoreName(getName());
        elementType.setMetaStoreName(getName());
      }
    } else {
      throw new MetaStoreException("Namespace '"+namespace+"' doesn't exist!");
    }
  }

  @Override
  public synchronized void deleteElementType(String namespace, IMetaStoreElementType elementType) throws MetaStoreException, MetaStoreDependenciesExistsException {
    MemoryMetaStoreNamespace storeNamespace = namespacesMap.get(namespace);
    if (storeNamespace!=null) {
      MemoryMetaStoreElementType verifyType = storeNamespace.getTypeMap().get(elementType.getId());
      if (verifyType==null) {
        throw new MetaStoreElementTypeExistsException(getElementTypes(namespace), "Element type to delete, with ID '"+elementType.getId()+"', does not exist");
      } else {
        // See if there are elements in there...
        //
        if (!verifyType.getElementMap().isEmpty()) {
          throw new MetaStoreDependenciesExistsException(getElementIds(namespace, elementType), "Element type with ID '"+elementType.getId()+"' could not be deleted as it still contains elements.");
        }
        storeNamespace.getTypeMap().remove(elementType.getId());
      }
    } else {
      throw new MetaStoreException("Namespace '"+namespace+"' doesn't exist!");
    }
  }

  @Override
  public synchronized List<IMetaStoreElement> getElements(String namespace, IMetaStoreElementType elementType) throws MetaStoreException {
    MemoryMetaStoreElementType foundType = (MemoryMetaStoreElementType) getElementTypeByName(namespace, elementType.getName());
    if (foundType==null) {
      return new ArrayList<IMetaStoreElement>();
    } else {
      return new ArrayList<IMetaStoreElement>(foundType.getElementMap().values());
    }
  }

  @Override
  public synchronized List<String> getElementIds(String namespace, IMetaStoreElementType elementType) throws MetaStoreException {
    MemoryMetaStoreElementType foundType = (MemoryMetaStoreElementType) getElementTypeByName(namespace, elementType.getName());
    
    List<String> ids = new ArrayList<String>();
    for (String id : foundType.getElementMap().keySet()) {
      ids.add(id);
    }
    
    return ids;
  }

  @Override
  public synchronized IMetaStoreElement getElement(String namespace, IMetaStoreElementType elementType, String elementId) throws MetaStoreException {
    MemoryMetaStoreElementType foundType = (MemoryMetaStoreElementType) getElementTypeByName(namespace, elementType.getName());
    if (foundType==null) {
      return null;
    }
    return foundType.getElementMap().get(elementId);
  }
  
  @Override
  public synchronized IMetaStoreElement getElementByName(String namespace, IMetaStoreElementType elementType, String name) throws MetaStoreException {
    for (IMetaStoreElement element : getElements(namespace, elementType)) {
      if (element.getName()!=null && element.getName().equalsIgnoreCase(name)) {
        return element;
      }
    }
    return null;
  }

  @Override
  public synchronized void createElement(String namespace, IMetaStoreElementType elementType, IMetaStoreElement element) throws MetaStoreException, MetaStoreElementExistException {
    // For the memory store, the ID is the same as the name if empty
    if (element.getId()==null) {
      element.setId(element.getName());
    }

    MemoryMetaStoreElementType foundType = (MemoryMetaStoreElementType) getElementTypeByName(namespace, elementType.getName());
    if (foundType==null) {
      throw new MetaStoreException("Element type '"+elementType.getName()+"' couldn't be found");
    }
    foundType.getElementMap().put(element.getId(), new MemoryMetaStoreElement(element));
  }
  
  @Override
  public synchronized void updateElement(String namespace, IMetaStoreElementType elementType, String elementId, IMetaStoreElement element) throws MetaStoreException {
    
    // verify that the element type belongs to this meta store
    //
    if (elementType.getMetaStoreName()==null || !elementType.getName().equals(getName())) {
      throw new MetaStoreException("The element type '"+elementType.getName()+"' needs to explicitly belong to the meta store in which you are updating.");
    }
    
    MemoryMetaStoreElementType foundType = (MemoryMetaStoreElementType) getElementTypeByName(namespace, elementType.getName());
    if (foundType==null) {
      throw new MetaStoreException("Element type '"+elementType.getName()+"' couldn't be found");
    }
    foundType.getElementMap().put(elementId, new MemoryMetaStoreElement(element));
  }

  @Override
  public synchronized void deleteElement(String namespace, IMetaStoreElementType elementType, String elementId) throws MetaStoreException {
    MemoryMetaStoreElementType foundType = (MemoryMetaStoreElementType) getElementTypeByName(namespace, elementType.getName());
    if (foundType==null) {
      throw new MetaStoreException("Element type '"+elementType.getName()+"' couldn't be found");
    }
    foundType.getElementMap().remove(elementId);
  }

  @Override
  public IMetaStoreElementType newElementType(String namespace) throws MetaStoreException {
    return new MemoryMetaStoreElementType(namespace);
  }

  @Override
  public IMetaStoreElement newElement() throws MetaStoreException {
    return new MemoryMetaStoreElement();
  }

  @Override
  public IMetaStoreElement newElement(IMetaStoreElementType elementType, String id, Object value) throws MetaStoreException {
    return new MemoryMetaStoreElement(elementType, id, value);
  }

  public IMetaStoreAttribute newAttribute(String id, Object value) throws MetaStoreException {
    return new MemoryMetaStoreAttribute(id, value);
  }

  @Override
  public IMetaStoreElementOwner newElementOwner(String name, MetaStoreElementOwnerType ownerType)
      throws MetaStoreException {
    return new MemoryMetaStoreElementOwner(name, ownerType);
  }   
   
}
