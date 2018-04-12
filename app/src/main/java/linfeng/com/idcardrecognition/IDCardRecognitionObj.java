package linfeng.com.idcardrecognition;

/**
 * Created by lf
 * on 2018/4/11
 */

public class IDCardRecognitionObj {
    public String address;
    public String birth;
    public String name;
    public String nationality;
    public String num;
    public String sex;
    public boolean success;
    public String end_date;
    public String issue;
    public String start_date;

    @Override
    public String toString() {
        return "IDCardRecognitionObj{" +
                "address='" + address + '\'' +
                ", birth='" + birth + '\'' +
                ", name='" + name + '\'' +
                ", nationality='" + nationality + '\'' +
                ", num='" + num + '\'' +
                ", sex='" + sex + '\'' +
                ", success=" + success +
                ", end_date='" + end_date + '\'' +
                ", issue='" + issue + '\'' +
                ", start_date='" + start_date + '\'' +
                '}';
    }
}
