package symbolTable;

public class TypeError {
    private int lineNum;
    private String msg;

    public TypeError(int lineNum, String msg) {
        this.lineNum = lineNum;
        this.msg = msg;
    }

    @Override
    public String toString() {
       return String.format("Line: %d, %s", lineNum, msg); 
    }
}
