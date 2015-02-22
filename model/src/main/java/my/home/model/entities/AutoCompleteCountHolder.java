package my.home.model.entities;

/**
 * Created by legendmohe on 15/2/19.
 */
public class AutoCompleteCountHolder {
    public String from = "";
    public String to = "";
    public int count = 0;

    public AutoCompleteCountHolder(String from, String to, int count) {
        this.from = from;
        this.to = to;
        this.count = count;
    }
}
