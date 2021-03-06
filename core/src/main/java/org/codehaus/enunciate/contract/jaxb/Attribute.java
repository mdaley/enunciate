/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.type.PrimitiveType;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.doc.DocumentationExample;
import org.codehaus.enunciate.json.JsonName;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonNode;
import org.jdom.Namespace;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.namespace.QName;

/**
 * An accessor that is marshalled in xml to an xml attribute.
 *
 * @author Ryan Heaton
 */
public class Attribute extends Accessor {

  private final XmlAttribute xmlAttribute;

  public Attribute(MemberDeclaration delegate, TypeDefinition typedef) {
    super(delegate, typedef);

    xmlAttribute = getAnnotation(XmlAttribute.class);
  }

  // Inherited.
  public String getName() {
    String name = getSimpleName();

    if ((xmlAttribute != null) && (!"##default".equals(xmlAttribute.name()))) {
      name = xmlAttribute.name();
    }

    return name;
  }

  // Inherited.
  public String getNamespace() {
    String namespace = null;

    if (getForm() == XmlNsForm.QUALIFIED) {
      namespace = getTypeDefinition().getNamespace();
    }

    if ((xmlAttribute != null) && (!"##default".equals(xmlAttribute.namespace()))) {
      namespace = xmlAttribute.namespace();
    }

    return namespace;
  }

  /**
   * The form of this attribute.
   *
   * @return The form of this attribute.
   */
  public XmlNsForm getForm() {
    XmlNsForm form = getTypeDefinition().getSchema().getAttributeFormDefault();

    if (form == null || form == XmlNsForm.UNSET) {
      form = XmlNsForm.UNQUALIFIED;
    }
    
    return form;
  }


  /**
   * An attribute is a ref if its namespace differs from that of its type definition (JAXB spec 8.9.7.2).
   *
   * @return The ref or null.
   */
  @Override
  public QName getRef() {
    boolean qualified = getForm() == XmlNsForm.QUALIFIED;
    String typeNamespace = getTypeDefinition().getNamespace();
    typeNamespace = typeNamespace == null ? "" : typeNamespace;
    String namespace = getNamespace();
    namespace = namespace == null ? "" : namespace;

    if ((!namespace.equals(typeNamespace)) && (qualified || !"".equals(namespace))) {
      return new QName(namespace, getName());
    }

    return null;
  }

  /**
   * Whether the attribute is required.
   *
   * @return Whether the attribute is required.
   */
  public boolean isRequired() {
    return xmlAttribute != null && xmlAttribute.required() || (getAccessorType() instanceof PrimitiveType);
  }

  /**
   * @return true
   */
  @Override
  public boolean isAttribute() {
    return true;
  }

  /**
   * Generates example xml to the specified parent element.
   *
   * @param parent The parent element.
   */
  public void generateExampleXml(org.jdom.Element parent) {
    DocumentationExample exampleInfo = getAnnotation(DocumentationExample.class);
    if (exampleInfo == null || !exampleInfo.exclude()) {
      String namespace = getNamespace();
      String prefix = namespace == null ? null : ((EnunciateFreemarkerModel) FreemarkerModel.get()).getNamespacesToPrefixes().get(namespace);
      String exampleValue = exampleInfo == null || "##default".equals(exampleInfo.value()) ? "..." : exampleInfo.value();
      Namespace jdomNS;
      if (org.jdom.Namespace.XML_NAMESPACE.getURI().equals(namespace)) {
        jdomNS = org.jdom.Namespace.XML_NAMESPACE;
      }
      else if (namespace == null || "".equals(namespace)) {
        jdomNS = org.jdom.Namespace.NO_NAMESPACE;
      }
      else {
        jdomNS = Namespace.getNamespace(prefix, namespace);
      }
      org.jdom.Attribute attr = new org.jdom.Attribute(getName(), exampleValue, jdomNS);
      parent.setAttribute(attr);
    }
  }

  /**
   * Generates some example json to the specified object node (type def).
   *
   * @param jsonNode The parent node.
   */
  public void generateExampleJson(ObjectNode jsonNode) {
    DocumentationExample exampleInfo = getAnnotation(DocumentationExample.class);
    if (exampleInfo == null || !exampleInfo.exclude()) {
      JsonNode valueNode = getBaseType().generateExampleJson(exampleInfo == null || "##default".equals(exampleInfo.value()) ? null : exampleInfo.value());
      jsonNode.put(getName(), valueNode);
    }
  }

  @Override
  public String getJsonMemberName() {
    JsonName jsonName = getAnnotation(JsonName.class);
    return jsonName == null ? getName() : jsonName.value();
  }
}
