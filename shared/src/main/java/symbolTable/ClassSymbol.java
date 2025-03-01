package symbolTable;

import java.util.ArrayList;

public class ClassSymbol extends Symbol {

    private String parent;
    public ArrayList<String> methodList;
    public ArrayList<String> fieldList;

    public ClassSymbol(String parentSym) {
        super(true);
        this.parent = parentSym;
        this.methodList = new ArrayList<>();
        this.fieldList = new ArrayList<>();
    }

    public ClassSymbol() {
        super(true);
        parent = "";
        this.methodList = new ArrayList<>();
        this.fieldList = new ArrayList<>();
    }

    public String getParent() {
        return parent;
    }

    public void addMethod(String method) {
        methodList.add(method);
    }

    public void addField(String field) {
        fieldList.add(field);
    }

    public boolean hasMethod(String methodName) {
        for (String s: methodList) {
            if (methodName.equals(s.split("\\.")[1])) {
                return true;
            }
        }
        return false;
    }

    public int methodIndex(String methodName) {
        for (int i = 0; i < methodList.size(); i++) {
            if (methodName.equals(methodList.get(i).split("\\.")[1])) {
                return i;
            }
        }
        return -1;
    }

    public String methodClass(String methodName) {
        for (String s : methodList) {
            if (methodName.equals(s.split("\\.")[1])) {
                return s.split("\\.")[0];
            }
        }
        return "";
    }

    public boolean hasField(String fieldName) {
        for (String s: fieldList) {
            if (fieldName.equals(s.split("\\.")[1])) {
                return true;
            }
        }
        return false;
    }

    public int fieldIndex(String fieldName) {
        for (int i = 0; i < fieldList.size(); i++) {
            if (fieldName.equals(fieldList.get(i).split("\\.")[1])) {
                return i;
            }
        }
        return -1;
    }
}
