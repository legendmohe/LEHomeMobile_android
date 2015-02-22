package my.home.domain.events;

/**
 * Created by legendmohe on 15/2/19.
 */
public class DSaveAutoCompleteLocalHistoryEvent {
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;

    private int returnCode;

    public DSaveAutoCompleteLocalHistoryEvent(int code) {
        this.returnCode = code;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }
}
