package vaporGen;

public class TempCounters {

    private int ifNesting = 0;
    private int whileCount = 0;
    private int nullCount = 0;
    private int boundsCount = 0;
    private int tCount = 0;
    private int andCount = 0;

    public int getIf() {
        ifNesting++;
        return ifNesting;
    }
    public int getWhile() {
        whileCount++;
        return whileCount;
    }
    public int getNull() {
        nullCount++;
        return nullCount;
    }
    public int getBounds() {
        boundsCount++;
        return boundsCount;
    }
    public int getAnd() {
        andCount++;
        return andCount;
    }
    public String getTemp() {
        tCount++;
        return new String("t."+tCount);
    }

    public void resetTVarCounter() {
        tCount = 0;
    }
}
