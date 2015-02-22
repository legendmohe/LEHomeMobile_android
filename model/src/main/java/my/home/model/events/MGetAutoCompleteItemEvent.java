package my.home.model.events;

import java.util.List;

/**
 * Created by legendmohe on 15/2/13.
 */
public class MGetAutoCompleteItemEvent {
    private List resultList;

    public MGetAutoCompleteItemEvent(List resultList) {
        this.resultList = resultList;
    }

    public List getResultList() {
        return resultList;
    }

    public void setResultList(List resultList) {
        this.resultList = resultList;
    }
}
