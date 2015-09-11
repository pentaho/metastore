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

package org.pentaho.metastore.util;

import org.junit.Test;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MetaStoreUtilTest {

  @Test
  public void testCopyElements_empty() throws Exception {
    IMetaStore from = new MemoryMetaStore();
    IMetaStore to = new MemoryMetaStore();

    MetaStoreUtil.copy( from, to );

    assertEquals( from.getNamespaces().size(), to.getNamespaces().size() );
  }

  @Test
  public void testCopy() throws Exception {
    IMetaStore from = mock( IMetaStore.class );
    IMetaStore to = mock( IMetaStore.class );

    String[] namespaces = new String[] { "pentaho", "hitachi" };
    List<IMetaStoreElementType> penElementTypes = new ArrayList<>();
    IMetaStoreElementType type1 = mock( IMetaStoreElementType.class );
    IMetaStoreElementType type2 = mock( IMetaStoreElementType.class );
    penElementTypes.add( type1 );
    penElementTypes.add( type2 );

    List<IMetaStoreElement> elements = new ArrayList<>();
    IMetaStoreElement elem1 = mock( IMetaStoreElement.class );
    IMetaStoreElement elem2 = mock( IMetaStoreElement.class );
    IMetaStoreElement elem3 = mock( IMetaStoreElement.class );
    elements.add( elem1 );
    elements.add( elem2 );
    elements.add( elem3 );

    when( from.getNamespaces() ).thenReturn( Arrays.asList( namespaces ) );
    when( from.getElementTypes( "pentaho" ) ).thenReturn( penElementTypes );
    when( from.getElements( "pentaho", type1 ) ).thenReturn( elements );
    when( from.getElements( "pentaho", type2 ) ).thenReturn( elements );

    MetaStoreUtil.copy( from, to );

    verify( to ).createNamespace( "pentaho" );
    verify( to ).createNamespace( "hitachi" );
    verify( to ).createElementType( "pentaho", type1 );
    verify( to ).createElementType( "pentaho", type2 );
    verify( to ).createElement( "pentaho", type1, elem1 );
    verify( to ).createElement( "pentaho", type1, elem2 );
    verify( to ).createElement( "pentaho", type1, elem3 );
    verify( to ).createElement( "pentaho", type2, elem1 );
    verify( to ).createElement( "pentaho", type2, elem2 );
    verify( to ).createElement( "pentaho", type2, elem3 );

    verify( to, never() ).createElementType( eq( "hitachi" ), any( IMetaStoreElementType.class ) );
    verify( to, never() ).createElement( eq( "hitachi" ), any( IMetaStoreElementType.class ), any( IMetaStoreElement.class ) );
  }



}