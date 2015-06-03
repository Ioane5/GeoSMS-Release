package com.steps.geosms.websms;
/**
 * Class WebSms interface
 * Created by Ioane on 3/5/2015.
 */
public interface WebSms {

    public static final String USER_AGENT_VALUE = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36";

    public static interface FIELDS{
        String GET ="GET";
        String POST ="POST";
        String HEAD ="HEAD";

        String USER = "user";
        String PASSWORD = "password";
        String USER_AGENT = "User-agent: ";
        String SET_COOKIE = "Set-Cookie";
        String COOKIE = "Cookie";
    }



    public abstract boolean authenticate();

    public abstract boolean sendSms(String message,String address);

    public abstract int getNumMessages();

    public String getCookie();

    public void setCookie(String cookie);

    public String getPassword();

    public void setPassword(String password);

    public String getUserName();

    public void setUserName(String userName);

    public String getAccountName();

    public void setAccountName(String name);
}
