package symbolTable;

import visitor.*;

import java.util.ArrayList;
import java.util.Map;

import syntaxtree.*;

public class SymTableVis<A> extends GJDepthFirst<String, A> {

    public SymbolTable symTable;

    private String currClass;
    private String currMethod;

    private MethodSymbol methodParameterTarget;
    private ClassSymbol classMethodTarget;

    private int currLine;

    public ArrayList<TypeError> errors;


    public SymTableVis() {
        this.symTable = new SymbolTable();
        this.currClass = "";
        this.currMethod = "";
        this.errors = new ArrayList<>();
    }

    private boolean checkInsert(String key, Symbol val) {
        if (symTable.containsKey(key)) {
            return false;
        }
        symTable.put(key, val);
        return true;
    }

    // generates unique path key for defined variables/classes based on scope path.
    // Responsibility of setting currClass and currMethod falls to the appropriate visit
    // methods while traversing tree
    private String createKey(String suffix) {
        StringBuilder strB = new StringBuilder();
        strB.append(currClass);
        strB.append(".");
        if (currMethod != "") {
            strB.append(currMethod);
            strB.append(".");
        }
        strB.append(suffix);
        return strB.toString();
    }

    // Cleans up extended classes by moving all inherited methods into the class symbol
    private void cleanExtensions() {
        for (Map.Entry<String, Symbol> entry: symTable.entrySet()) {
            // look only at class symbols
            if (entry.getValue() instanceof ClassSymbol) {
                symTable.classes.add(entry.getValue().getType());
                clean((ClassSymbol) entry.getValue());
            }
        }
    }

    private void clean(ClassSymbol sym) {
        ClassSymbol parent = null;
        // Handle super classes first
        if (!sym.getParent().equals("")) {
            parent = (ClassSymbol) symTable.get(sym.getParent());
            clean(parent);
        }
        ArrayList<String> baseMethods = new ArrayList<>();
        // What are the methods in already in the class scope?
        for (String s : sym.methodList) {
            baseMethods.add(s.split("\\.")[1]);
        }
        // Inherit methods that are not overwritten by this class
        if (parent != null) {
            for (String s : parent.methodList) {
                String methodStr = s.split("\\.")[1];
                if (!baseMethods.contains(methodStr)) {
                    sym.methodList.add(s);
                }
            }
        }

        ArrayList<String> revisedFields = new ArrayList<>();
        // Maintain field order by starting with extended classes for ease of IR code
        if (parent != null) {
            for (String s : parent.fieldList) {
                revisedFields.add(s);
            }
        }
        // Add this classes fields to end
        // Do not overwrite.
        for (String s : sym.fieldList) {
            revisedFields.add(s);
        }
        sym.fieldList = revisedFields;

    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, A argu) {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);

        cleanExtensions();
        return _ret;
    }


    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit(MainClass n, A argu) {
        String _ret=null;

        // Main Class Identifier.
        currClass = n.f1.f0.toString();
        Symbol newSym = Symbol.newClassSymbol(new String(currClass));
        classMethodTarget = (ClassSymbol) newSym;
        // First symbol. Can not conflict
        symTable.put(new String(currClass), newSym);

        // Argument parameter Identifier. (Not used in Minijava)
        String argVar = n.f11.f0.toString();
        Symbol argSym = Symbol.newSimpleSymbol("arg");
        String key = createKey(argVar);
        if(!checkInsert(key, argSym)) {
            errors.add(new TypeError(n.f0.beginLine, "Duplicate variable"));
        }

        n.f14.accept(this, argu);

        currClass = "";

        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration n, A argu) {
        String _ret=null;

        // Class Identifier
        currClass = n.f1.f0.toString();
        Symbol newSym = Symbol.newClassSymbol(new String(currClass));
        classMethodTarget = (ClassSymbol) newSym;
        if (!checkInsert(new String(currClass), newSym)) {
            errors.add(new TypeError(n.f0.beginLine, "Duplicate variable"));
        }

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        currClass = "";

        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public String visit(ClassExtendsDeclaration n, A argu) {
        String _ret=null;

        currClass = n.f1.f0.toString();
        String parentClass = n.f3.f0.toString();
        Symbol newSym = Symbol.newClassSymbol(new String(currClass), parentClass);
        classMethodTarget = (ClassSymbol) newSym;
        if (!checkInsert(new String(currClass), newSym)) {
            errors.add(new TypeError(n.f0.beginLine, "Duplicate variable"));
        }

        n.f5.accept(this, argu);
        n.f6.accept(this, argu);

        currClass = "";

        return _ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, A argu) {
        String _ret=null;

        String typeStr = n.f0.accept(this, argu);
        String idStr = n.f1.accept(this, argu);

        Symbol newSym = Symbol.newSimpleSymbol(typeStr);
        String key = createKey(idStr);
        if(!checkInsert(key, newSym)) {
            errors.add(new TypeError(n.f2.beginLine, "Duplicate variable"));
        }

        // If outside method declaration (In class scope)
        if (currMethod.equals("")) {
            classMethodTarget.addField(key);
        } else {
            // In method var decl
            methodParameterTarget.addVar(idStr);
        }

        return _ret;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration n, A argu) {
        String _ret=null;

        String typeStr = n.f1.accept(this, argu);
        String idStr = n.f2.accept(this, argu);

        Symbol newSym = Symbol.newMethodSymbol(new String(idStr), typeStr);
        methodParameterTarget = (MethodSymbol) newSym;
        
        String key = createKey(idStr+"_M");

        if (!checkInsert(key, newSym)) {
            errors.add(new TypeError(n.f0.beginLine, "Duplicate variable"));
        }
        classMethodTarget.addMethod(classMethodTarget.getType()+"."+idStr);
        currMethod = idStr;
        currLine = n.f0.beginLine;

        n.f4.accept(this, argu);
        n.f7.accept(this, argu);

        currMethod = "";

        return _ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, A argu) {
        String _ret=null;

        String typeStr = n.f0.accept(this, argu);
        String idStr = n.f1.accept(this, argu);

        Symbol newSym = Symbol.newSimpleSymbol(typeStr);
        String key = createKey(idStr);
        if (!checkInsert(key, newSym)) {
            errors.add(new TypeError(currLine, "Duplicate variable"));
        }
        methodParameterTarget.addParameter(newSym);

        return _ret;
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public String visit(Type n, A argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType n, A argu) {
        return "int[]";
    }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, A argu) {
        return "boolean";
    }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, A argu) {
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, A argu) {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, A argu) {
        return n.f0.toString();
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, A argu) {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, A argu) {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        return _ret;
    }
}


