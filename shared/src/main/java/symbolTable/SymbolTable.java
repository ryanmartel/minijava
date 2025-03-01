package symbolTable;

import java.util.HashMap;
import java.util.ArrayList;

public class SymbolTable extends HashMap<String, Symbol> {

    public ArrayList<String> classes = new ArrayList<>();


    // Symbol table separates scope using "." 
    // getSymbol from the Symbol Table using the appropriate class and method scope.
    public Symbol getSymbol(String suffix, String currClass, String currMethod) {
        // Get class heirarchy.
        // search class scopes in order.
        ArrayList<String> superClasses;
        if (currClass != "") {
            superClasses = superClassList((ClassSymbol) this.get(currClass));
        } else {
            superClasses = superClassList((ClassSymbol) this.get(suffix));
        }

        String key;
        ArrayList<Symbol> matches = new ArrayList<>();

        // Method Scope
        if (currMethod != "") {
            // Base class
            key = superClasses.get(0)+"."+currMethod+"."+suffix;
            if (this.get(key) != null) {
                matches.add(this.get(key));
            }
        }
        // class scope
        for (String s: superClasses) {
            key = s+"."+suffix;
            if (this.get(key) != null) {
                matches.add(this.get(key));
            }
        }
        // suffix is a class symbol!
        if (this.get(suffix) != null) {
            matches.add(this.get(suffix));
        }
        if (matches.size() == 0) {
            return null;
        }
        return matches.get(0);
    }

    public Symbol getSymbol(String suffix, ClassSymbol classSym, MethodSymbol methodSym) {
        return getSymbol(suffix, classSym.getType(), methodSym.getType());
    }

    public ClassSymbol getClass(String className) {
        return (ClassSymbol) getSymbol(className, "","");
    }


    // Generate the class heirarchy for a base class using the Symbol Table
    // Every extending class stores the name of the class that it extends.
    public ArrayList<String> superClassList(ClassSymbol baseClass) {
        ArrayList<String> classList = new ArrayList<>();
        classList.add(baseClass.getType());
        if (baseClass.getParent().equals("")) {
            return classList;
        }
        String currParent = baseClass.getParent();
        ClassSymbol currClass;
        while (!baseClass.getType().equals(currParent) && !currParent.equals("")) {
            currClass = (ClassSymbol) this.get(currParent);
            classList.add(new String(currParent));
            currParent = currClass.getParent();
        }
        return classList;
    }
}
