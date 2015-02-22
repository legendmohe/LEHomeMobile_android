package my.home.domain.usecase;

/**
 * Created by legendmohe on 15/2/8.
 */
public interface AutoCompleteItemUsecase extends Usecase {

    public static final String TAG = "AutoCompleteItemUsecaseImpl";

    public static final int MODE_DO_NOTHING = -1;
    public static final int MODE_GETITEM = 0;
    public static final int MODE_SAVE_CONF = 1;
    public static final int MODE_LOAD_CONF = 2;

    int getMode();

    AutoCompleteItemUsecase setMode(int mMode);

    AutoCompleteItemUsecase setInputText(String inputText);

    String getConfString();

    AutoCompleteItemUsecase setConfString(String mConfString);
}
