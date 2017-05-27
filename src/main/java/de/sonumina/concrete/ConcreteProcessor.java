package de.sonumina.concrete;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.Trees;

/**
 * This is the processor for the Concrete annotation.
 *
 * @author Sebastian Bauer
 */
@SupportedAnnotationTypes("de.sonumina.concrete.Concrete")
public class ConcreteProcessor extends AbstractProcessor
{
	private Trees trees;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);

		trees = Trees.instance(processingEnv);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		for (TypeElement e : annotations)
		{
			if (roundEnv == null)
			{
				return false;
			}
			Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(e);
			if (elements == null)
			{
				continue;
			}

			for (Element elm : elements)
			{
				if (elm.getKind() != ElementKind.CLASS)
				{
					continue;
				}

				final Concrete concrete = elm.getAnnotation(Concrete.class);
				if (concrete == null)
				{
					processingEnv.getMessager().printMessage(Kind.ERROR, "No @Concrete annotation have been found", elm);
					return false;
				}

				final PrimitiveType.Code primitiveTypeCode;

				if (concrete.type().equals("int"))
				{
					primitiveTypeCode = PrimitiveType.INT;
				} else
				{
					processingEnv.getMessager().printMessage(Kind.ERROR, "The given type \"" + concrete.type() + "\" is not supported");
					return false;
				}

				TypeElement type = (TypeElement)elm;
				final String className = type.getSimpleName().toString();
				ClassTree clTree = trees.getTree(type);
				if (clTree.getTypeParameters().size() != 1)
				{
					processingEnv.getMessager().printMessage(Kind.ERROR, "The @Concrete annotation must only be used on classes that have a single type parameter", elm);
					return false;
				}
				ASTParser parser = ASTParser.newParser(AST.JLS8);
				parser.setResolveBindings(false);
				String str = clTree.toString();
				Document source = new Document(str);
				parser.setSource(str.toCharArray());
				ASTNode node = parser.createAST(null);
				final AST ast = node.getAST();

				CompilationUnit cu = (CompilationUnit)node;
				final ASTRewrite rewrite = ASTRewrite.create(ast);

				cu.accept(new ASTVisitor()
				{
					@Override
					public boolean visit(TypeDeclaration node)
					{
						rewrite.set(node, TypeDeclaration.NAME_PROPERTY, ast.newSimpleName(concrete.name()), null);
						ListRewrite lrw = rewrite.getListRewrite(node, TypeDeclaration.TYPE_PARAMETERS_PROPERTY);
						/* Simply remove all types for now */
						for (Object o : lrw.getOriginalList())
						{
							lrw.remove((ASTNode)o, null);
						}
						return super.visit(node);
					}

					@Override
					public boolean visit(FieldDeclaration node)
					{
						Type t = node.getType();
						if (t.isSimpleType())
						{
							rewrite.set(node, FieldDeclaration.TYPE_PROPERTY, ast.newPrimitiveType(primitiveTypeCode), null);
						}
						return super.visit(node);
					}

					@Override
					public boolean visit(MethodDeclaration node)
					{
						Type rt = node.getReturnType2();
						if (rt != null)
						{
							MethodDeclaration newNode = (MethodDeclaration)ASTNode.copySubtree(ast, node);
							if (rt.isSimpleType())
							{
								PrimitiveType pt = node.getAST().newPrimitiveType(PrimitiveType.INT);
								newNode.setReturnType2(pt);
							}
							rewrite.replace(node, newNode, null);
						} else
						{
							if (node.getName().toString().equals(className))
							{
								/* Possibly the constructor */
								MethodDeclaration newNode = (MethodDeclaration)ASTNode.copySubtree(ast, node);
								newNode.setName(ast.newSimpleName(concrete.name()));
								rewrite.replace(node, newNode, null);
							}
						}
						return super.visit(node);
					}
				});

				try
				{
					TextEdit edit = rewrite.rewriteAST(source, null);
					edit.apply(source);
					System.out.println(source.get());
				} catch (RuntimeException | BadLocationException exc)
				{
					exc.printStackTrace();
				}
			}
		}
		return true;
	}
}
