package my.home.common;

public class Utils {
    public static String readFileContent(String fileName) {
        return null;
    }

    public static String DateToCmdString(int year, int month, int day) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(year);
        stringBuilder.append("年");
        stringBuilder.append(month);
        stringBuilder.append("月");
        stringBuilder.append(day);
        stringBuilder.append("日");
        return stringBuilder.toString();
    }

    public static String TimeToCmdString(int hourOfDay, int minute) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hourOfDay);
        stringBuilder.append("点");
        stringBuilder.append(minute);
        stringBuilder.append("分");
        return stringBuilder.toString();
    }
}
