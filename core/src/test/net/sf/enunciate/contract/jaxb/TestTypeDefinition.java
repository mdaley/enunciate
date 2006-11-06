package net.sf.enunciate.contract.jaxb;

import net.sf.enunciate.InAPTTestCase;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.validation.ValidationResult;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;

import javax.xml.bind.annotation.XmlAccessType;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class TestTypeDefinition extends InAPTTestCase {

  /**
   * tests a basic type definition.
   */
  public void testBasicTypeDefinition() throws Exception {
    ClassDeclaration elementBeanOneDecl = (ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.ElementBeanOne");
    ComplexTypeDefinition elementBeanOneType = new ComplexTypeDefinition(elementBeanOneDecl);
    RootElementDeclaration elementBeanOne = new RootElementDeclaration(elementBeanOneDecl, elementBeanOneType);

    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);
    model.add(elementBeanOneType);
    model.add(elementBeanOne);

    TypeDefinition typeDef = new MockTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.FullTypeDefBeanOne"));
    assertEquals(1, typeDef.getAttributes().size());
    assertEquals("property1", typeDef.getAttributes().iterator().next().getSimpleName());

    assertNotNull(typeDef.getValue());
    assertEquals("property2", typeDef.getValue().getSimpleName());

    assertNotNull(typeDef.getXmlID());
    assertEquals(3, typeDef.getElements().size());
    assertTrue(typeDef.getElements().contains(typeDef.getXmlID()));

    assertEquals(XmlAccessType.PROPERTY, typeDef.getAccessType());

    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.schema.UnsupportedTypeDefBean");
    FieldDeclaration mixed = findField(declaration, "mixedProperty");
    FieldDeclaration anyElement = findField(declaration, "anyElementProperty");
    FieldDeclaration anyAttribute = findField(declaration, "anyAttributeProperty");
    assertTrue(typeDef.isUnsupported(mixed));
    assertTrue(typeDef.isUnsupported(anyElement));
    assertTrue(typeDef.isUnsupported(anyAttribute));
  }

  /**
   * tests a extended type definition.
   */
  public void testExtendedTypeDefinition() throws Exception {
    ClassDeclaration elementBeanOneDecl = (ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.ElementBeanOne");
    ComplexTypeDefinition elementBeanOneType = new ComplexTypeDefinition(elementBeanOneDecl);
    RootElementDeclaration elementBeanOne = new RootElementDeclaration(elementBeanOneDecl, elementBeanOneType);

    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);
    model.add(elementBeanOneType);
    model.add(elementBeanOne);

    TypeDefinition typeDef = new MockTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.ExtendedFullTypeDefBeanOne"));
    assertEquals(XmlAccessType.PROPERTY, typeDef.getAccessType());
  }

  private static class MockTypeDefinition extends TypeDefinition {

    public MockTypeDefinition(ClassDeclaration delegate) {
      super(delegate);
    }

    public ValidationResult accept(Validator validator) {
      return null;
    }

    public XmlTypeMirror getBaseType() {
      return null;
    }
  }

  protected FieldDeclaration findField(TypeDeclaration typeDef, String propertyName) {
    FieldDeclaration field = null;
    Collection<FieldDeclaration> fields = typeDef.getFields();
    for (FieldDeclaration fieldDeclaration : fields) {
      if (propertyName.equals(fieldDeclaration.getSimpleName())) {
        field = fieldDeclaration;
      }
    }
    return field;
  }
}