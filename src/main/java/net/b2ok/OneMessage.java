package net.b2ok;

/**
 * Created by andrei on 4/16/16.
 */
public class OneMessage {
    private String cmd;     //Команда
    private String text;    //Текст сообщения
    private String param;   //Параметры команды

    public OneMessage() {}

    public OneMessage(String cmd, String text, String param) {
        this.cmd = cmd;
        this.text = text;
        this.param = param;
    }

    public String toJson(){
        return ChatieApplication.gson.toJson(this);
    }

    public String getCmd() {
        return cmd;
    }

    public String getText() {
        return text;
    }

    public String getParam() {
        return param;
    }
}
