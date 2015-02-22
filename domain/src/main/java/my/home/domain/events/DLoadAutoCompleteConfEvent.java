package my.home.domain.events;

/**
 * Created by legendmohe on 15/2/15.
 */
public class DLoadAutoCompleteConfEvent {
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;

    private int returnCode;

    public DLoadAutoCompleteConfEvent(int code) {
        this.returnCode = code;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }
}
