package symbolTable;

public class Symbol {

    private boolean compound;
    private String type;
    private boolean dynamic;

    protected Symbol(boolean compound) {
        this.compound = compound;
    }


    public static Symbol newClassSymbol(String name, String parentSym) {
        Symbol s = new ClassSymbol(parentSym);
        s.type = name;
        return s;
    }

    public static Symbol newClassSymbol(String name) {
        Symbol s = new ClassSymbol();
        s.type = name;
        return s;
    }

    public static Symbol newMethodSymbol(String name, String returnType) {
        Symbol s = new MethodSymbol(returnType);
        s.type = name;
        return s;
    }

    public static Symbol newSimpleSymbol(String type) {
        Symbol s = new Symbol(false);
        s.type = type;
        return s;
    }

    public static Symbol newSimpleDymSymbol(String type) {
        Symbol s = new Symbol(false);
        s.type = type;
        s.dynamic = true;
        return s;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public boolean isCompound() {
        return compound;
    }

    public boolean isDynamic() {
        return dynamic;
    }
}
