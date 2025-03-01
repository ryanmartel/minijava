package symbolTable;

import java.util.ArrayList;

public class MethodSymbol extends Symbol {

    private String returnType; 
    public ArrayList<Symbol> parameters;
    public ArrayList<String> localVars;

    public MethodSymbol(String returnType) {
        super(true);
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
        this.localVars = new ArrayList<>();
    }

    public String getReturnType() {
        return returnType;
    } 

    public void addParameter(Symbol param) {
        parameters.add(param);
    }

    public void addVar(String var) {
        localVars.add(var);
    }
}
