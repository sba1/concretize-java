package de.sonumina.concrete;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

import org.junit.Test;

import com.google.testing.compile.JavaFileObjects;

import de.sonumina.concrete.ConcreteProcessor;

public class ConcreteTest
{
	private final static String SINGLE_SRC_ONE_TYPE =
	"import de.sonumina.concrete.Concrete;\n" +
	"\n" +
	"@Concrete(name=\"SingleInt\", type=\"int\")\n" +
	"public class Single<T>\n" +
	"{\n" +
	"\tprivate T t;\n"+
	"\n" +
	"\tpublic T get()\n" +
	"\t{\n" +
	"\t\treturn t;\n"+
	"\t}\n"+
	"}\n";

	private final static String SINGLE_SRC_ONE_TYPE_SOME_OTHERS =
	"import de.sonumina.concrete.Concrete;\n" +
	"\n" +
	"@Concrete(name=\"SingleInt\", type=\"int\")\n" +
	"public class Single<T>\n" +
	"{\n" +
	"\tprivate long count;\n"+
	"\tprivate T t;\n"+
	"\n" +
	"\tpublic T get()\n" +
	"\t{\n" +
	"\t\tcount++;\n" +
	"\t\treturn t;\n"+
	"\t}\n"+
	"}\n";

	private final static String IMMUTABLE_ARRAY_SRC =
	"import de.sonumina.concrete.Concrete;\n" +
	"\n" +
	"@Concrete(name=\"ImmutableIntArray\", type=\"int\")\n" +
	"public final class ImmutableArray<T>\n" +
	"{\n" +
	"\tprivate T [] array;\n"+
	"\n" +
	"\tprivate ImmutableArray(T [] array)\n"+
	"\t{\n"+
	"\t\tthis.array = array;"+
	"\t}\n"+
	"\n"+
	"\tpublic int length()\n"+
	"\t{\n"+
	"\t\treturn array.length;\n"+
	"\t}"+
	"\n"+
	"\tpublic T get(int i)\n"+
	"\t{"+
	"\t\treturn array[i];\n"+
	"\t}"+
	"}";

	private final static String SINGLE_SRC_TWO_TYPES =
	"import de.sonumina.concrete.Concrete;\n" +
	"\n" +
	"@Concrete(name=\"SingleInt\", type=\"int\")\n" +
	"public class Single<T,U>\n" +
	"{\n" +
	"\tprivate T t;\n"+
	"\n" +
	"\tpublic T get()\n" +
	"\t{\n" +
	"\t\treturn t;\n"+
	"\t}\n"+
	"}\n";

	@Test
	public void testConcreteWorks()
	{
		assertAbout(javaSource())
			.that(JavaFileObjects.forSourceString("Single", SINGLE_SRC_ONE_TYPE)).withCompilerOptions("-verbose")
			.processedWith(new ConcreteProcessor())
			.compilesWithoutError();
	}

	@Test
	public void testConcreteWorksMoreFields()
	{
		assertAbout(javaSource())
			.that(JavaFileObjects.forSourceString("Single", SINGLE_SRC_ONE_TYPE_SOME_OTHERS)).withCompilerOptions("-verbose")
			.processedWith(new ConcreteProcessor())
			.compilesWithoutError();
	}

	@Test
	public void testConcreteWorksImmutableArray()
	{
		assertAbout(javaSource())
			.that(JavaFileObjects.forSourceString("ImmutableArray", IMMUTABLE_ARRAY_SRC)).withCompilerOptions("-verbose")
			.processedWith(new ConcreteProcessor())
			.compilesWithoutError();
	}

	public void testConcreteFailure()
	{
		assertAbout(javaSource())
			.that(JavaFileObjects.forSourceString("Single", SINGLE_SRC_TWO_TYPES)).withCompilerOptions("-verbose")
			.processedWith(new ConcreteProcessor())
			.failsToCompile();
	}
}
