package com.steps.geosms.websms;

/**
 * Class WebSms interface
 * Created by Ioane on 3/5/2015.
 */
public interface WebSms {

    final String USER_AGENT_VALUE = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36";

    interface FIELDS {
        String GET = "GET";
        String POST = "POST";
        String HEAD = "HEAD";

        String USER = "user";
        String PASSWORD = "password";
        String USER_AGENT = "User-agent: ";
        String SET_COOKIE = "Set-Cookie";
        String COOKIE = "Cookie";
    }


    void updateBalance();

    boolean authenticate();

    boolean sendSms(String message, String address);

    int getNumMessages();

    String getBalance();

    String getCookie();

    void setCookie(String cookie);

    String getPassword();

    void setPassword(String password);

    String getUserName();

    void setUserName(String userName);

    String getAccountName();

    void setAccountName(String name);
}
