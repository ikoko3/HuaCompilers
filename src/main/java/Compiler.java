
import ast.ASTNode;
import ast.ASTVisitor;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symbol.SymTable;

/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
public class Compiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Compiler.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.info("Usage : java Compiler [ --encoding <name> ] <inputfile(s)>");
        } else {
            int firstFilePos = 0;
            String encodingName = "UTF-8";
            if (args[0].equals("--encoding")) {
                firstFilePos = 2;
                encodingName = args[1];
                try {
                    java.nio.charset.Charset.forName(encodingName); // Side-effect: is encodingName valid? 
                } catch (Exception e) {
                    LOGGER.error("Invalid encoding '" + encodingName + "'");
                    return;
                }
            }
            for (int i = firstFilePos; i < args.length; i++) {
                Lexer scanner = null;
                try {
                    java.io.FileInputStream stream = new java.io.FileInputStream(args[i]);
                    LOGGER.info("Scanning file " + args[i]);
                    java.io.Reader reader = new java.io.InputStreamReader(stream, encodingName);
                    scanner = new Lexer(reader);
                    
                    // parse
                    parser p = new parser(scanner);
                    ASTNode compUnit = (ASTNode) p.parse().value;
                    LOGGER.info("Constructed AST");
                    
                    // keep global instance of program
                    Registry.getInstance().setRoot(compUnit);
                    
                    // build symbol table
                    compUnit.accept(new SymTableBuilderASTVisitor());

                    //collect symbols
                    compUnit.accept(new CollectSymbolsASTVisitor());

                    compUnit.accept(new CollectTypesASTVisitor());
                    
                    // print program
                    LOGGER.info("Input:");
                    ASTVisitor printVisitor = new PrintASTVisitor();
                    compUnit.accept(printVisitor);
                    
                    LOGGER.info("Compilation done");
                } catch (java.io.FileNotFoundException e) {
                    LOGGER.error("File not found : \"" + args[i] + "\"");
                } catch (java.io.IOException e) {
                    LOGGER.error("IO error scanning file \"" + args[i] + "\"");
                    LOGGER.error(e.toString());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                    //e.printStackTrace();
                }
            }
        }
    }

}
