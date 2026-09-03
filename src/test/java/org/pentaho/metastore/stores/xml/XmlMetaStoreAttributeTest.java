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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.w3c.dom.Node;

public class XmlMetaStoreAttributeTest {

  @Test
  public void testLoadAttribute() throws Exception {
    XmlMetaStoreAttribute attribute = new XmlMetaStoreAttribute();
    attribute.loadAttribute( parseAttribute( "<attribute>"
        + "<id>root</id>"
        + "<value>root-value</value>"
        + "<type>String</type>"
        + "<children>"
        + "<child><id>integer</id><value>42</value><type>Integer</type></child>"
        + "<child><id>double</id><value>4.2</value><type>Double</type></child>"
        + "<child><id>long</id><value>42000000000</value><type>Long</type></child>"
        + "<child><id>nested</id><value>nested-value</value><type>String</type>"
        + "<children><child><id>leaf</id><value>leaf-value</value><type>String</type></child></children>"
        + "</child>"
        + "</children>"
        + "</attribute>" ) );

    assertEquals( "root", attribute.getId() );
    assertEquals( "root-value", attribute.getValue() );
    assertEquals( 4, attribute.getChildren().size() );

    IMetaStoreAttribute integerAttribute = attribute.getChild( "integer" );
    assertTrue( integerAttribute.getValue() instanceof Integer );
    assertEquals( Integer.valueOf( 42 ), integerAttribute.getValue() );

    IMetaStoreAttribute doubleAttribute = attribute.getChild( "double" );
    assertTrue( doubleAttribute.getValue() instanceof Double );
    assertEquals( Double.valueOf( 4.2 ), doubleAttribute.getValue() );

    IMetaStoreAttribute longAttribute = attribute.getChild( "long" );
    assertTrue( longAttribute.getValue() instanceof Long );
    assertEquals( Long.valueOf( 42000000000L ), longAttribute.getValue() );

    IMetaStoreAttribute nestedAttribute = attribute.getChild( "nested" );
    assertEquals( "nested-value", nestedAttribute.getValue() );
    assertEquals( "leaf-value", nestedAttribute.getChild( "leaf" ).getValue() );
  }

  private Node parseAttribute( String xml ) throws Exception {
    DocumentBuilderFactory factory = XmlUtil.createSafeDocumentBuilderFactory();
    return factory.newDocumentBuilder().parse(
        new ByteArrayInputStream( xml.getBytes( StandardCharsets.UTF_8 ) ) ).getDocumentElement();
  }
}
