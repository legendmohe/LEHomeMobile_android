package my.home.model.entities;

/**
 * Created by legendmohe on 15/2/8.
 */
public class AutoCompleteItem {
    private String type;
    private float weight;
    private String content;
    private String cmd;

    public AutoCompleteItem(String type, float weight, String content, String cmd) {
        this.setType(type);
        this.setWeight(weight);
        this.setContent(content);
        this.setCmd(cmd);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
