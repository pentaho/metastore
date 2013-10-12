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

package org.pentaho.metastore.api.exceptions;

import java.util.List;

/**
 * This exception is thrown in case a data type is created in a metadata store when it already exists.
 * 
 * @author matt
 * 
 */

public class MetaStoreDependenciesExistsException extends MetaStoreException {

  private static final long serialVersionUID = -1658192841342866261L;

  private List<String> dependencies;

  public MetaStoreDependenciesExistsException( List<String> dependencies ) {
    super();
    this.dependencies = dependencies;
  }

  public MetaStoreDependenciesExistsException( List<String> dependencies, String message ) {
    super( message );
    this.dependencies = dependencies;
  }

  public MetaStoreDependenciesExistsException( List<String> dependencies, Throwable cause ) {
    super( cause );
    this.dependencies = dependencies;
  }

  public MetaStoreDependenciesExistsException( List<String> dependencies, String message, Throwable cause ) {
    super( message, cause );
    this.dependencies = dependencies;
  }

  public List<String> getDependencies() {
    return dependencies;
  }
}
