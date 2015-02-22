package my.home.domain.events;

import java.util.List;

/**
 * Created by legendmohe on 15/2/15.
 */
public class DShowAutoCompleteItemEvent {
    private List resultList;

    public DShowAutoCompleteItemEvent(List resultList) {
        this.resultList = resultList;
    }

    public List getResultList() {
        return resultList;
    }

    public void setResultList(List resultList) {
        this.resultList = resultList;
    }
}
