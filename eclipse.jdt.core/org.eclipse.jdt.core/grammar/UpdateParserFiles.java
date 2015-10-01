import java.io.IOException;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class UpdateParserFiles {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			printUsage();
			return;
		}
		Parser.buildFilesFromLPG(args[0], args[1]);
	}
	
	public static void printUsage() {
		System.out.println("Usage: UpdateParserFiles <path to javadcl.java> <path to javahdr.java>");
		System.out.println("e.g. UpdateParserFiles c:/javadcl.java c:/javahdr.java");
	}
}
