package my.home.domain;

import java.util.List;

/**
 * Created by legendmohe on 15/2/8.
 */
public interface GetAutoCompleteItemUsecase extends Usecase {

    public List<String> getAutoCompleteItems(String currentInput);

    public boolean resetState();
}
