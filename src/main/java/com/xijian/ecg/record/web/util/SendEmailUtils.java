package com.xijian.ecg.record.web.util;

import com.sun.mail.util.MailSSLSocketFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;

/**
 * Created by 邵伟 on 2017/5/21 0021.
 */
public class SendEmailUtils {
    private  String account;//登录用户名
    private  String pass;        //登录密码
    private  String from;        //发件地址
    private  String host = "smtp.exmail.qq.com";        //服务器地址
    private  String port = "465";        //端口
    private  String protocol = "smtp"; //协议

    private  String[] to;

    public  String getAccount() {
        return account;
    }

    public  void setAccount(String account) {
        this.account = account;
    }

    public  String getPass() {
        return pass;
    }

    public  void setPass(String pass) {
        this.pass = pass;
    }

    public  String getFrom() {
        return from;
    }

    public  void setFrom(String from) {
        this.from = from;
    }

    public  String getHost() {
        return host;
    }

    public  void setHost(String host) {
        this.host = host;
    }

    public  String getPort() {
        return port;
    }

    public  void setPort(String port) {
        this.port = port;
    }

    public  String getProtocol() {
        return protocol;
    }

    public  void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public  String[] getTo() {
        return to;
    }

    public  void setTo(String[] to) {
        this.to = to;
    }

    class MyAuthenricator extends Authenticator {
        String u = null;
        String p = null;

        public MyAuthenricator(String u, String p) {
            this.u = u;
            this.p = p;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(u, p);
        }
    }

    public void send(String subject,String text) {
        Properties prop = new Properties();
        //协议
        prop.setProperty("mail.transport.protocol", protocol);
        //服务器
        prop.setProperty("mail.smtp.host", host);
        //端口
        prop.setProperty("mail.smtp.port", port);
        //使用smtp身份验证
        prop.setProperty("mail.smtp.auth", "true");
        //使用SSL，企业邮箱必需！
        //开启安全协议
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
        } catch (GeneralSecurityException e1) {
            e1.printStackTrace();
        }
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);
        //
        Session session = Session.getDefaultInstance(prop, new MyAuthenricator(account, pass));
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(from, from));
            for(String t : to){
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(t));
            }
            mimeMessage.setSubject(subject);
            mimeMessage.setSentDate(new Date());
            mimeMessage.setText(text);
            mimeMessage.saveChanges();
            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
