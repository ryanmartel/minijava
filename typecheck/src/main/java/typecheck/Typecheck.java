package typecheck;

import syntaxtree.*;
import symbolTable.*;
import parser.*;

public class Typecheck {

    public static void main(String[] args) {
	Node root = null;
	try {
	    root = new MiniJavaParser(System.in).Goal();

	    // Build the symbol table. Top-down visitor, inherits from
	    // GJDepthFirst<R,A>. R=Void, A=Integer.
	    SymTableVis<Integer> pv =
	    	new SymTableVis<Integer>();
	    root.accept(pv, 0);
	    SymbolTable symt = pv.symTable;
        // Duplicate variable errors
        if (!pv.errors.isEmpty()) {
            System.out.println("Type error");

            // System.out.println("SYM ERRORS: ");
            for (TypeError errs: pv.errors) {
                System.out.println(errs.toString());
            }
            System.exit(1);
        }

        // Bottom up typecheck
        TypeCheckSimp ts = new TypeCheckSimp();
        root.accept(ts, symt);
        if (!ts.errors.isEmpty()) {
            System.out.println("Type error");

            // Error listing
            // System.out.println("TC ERRORS: ");
            for (TypeError errs: ts.errors) {
                System.out.println(errs.toString());
            }
            System.exit(1);
        } else {
            System.out.println("Program type checked successfully");
        }

	}
	catch (ParseException e) {
	    System.out.println(e.toString());
	    System.exit(1);
	}
    }
}

