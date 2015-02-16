package my.home.domain.events;

import java.util.List;

/**
 * Created by legendmohe on 15/2/15.
 */
public class ShowAutoCompleteItemEvent {
    private List resultList;

    public ShowAutoCompleteItemEvent(List resultList) {
        this.resultList = resultList;
    }

    public List getResultList() {
        return resultList;
    }

    public void setResultList(List resultList) {
        this.resultList = resultList;
    }
}
