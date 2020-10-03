package com;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Auto {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String str = getTimeURL();

        /**首次测试成功后，请自己把源代码改为常量**/
        /******************************************/
        String courseVersion = null; // 班级名（不要随意写，我会发给你）
        String stuClassId = null; // 班级id（不要随意写，我会发给你）
        String loginName = null; // 登陆用户名（写自己的）
        String password = null; // 登陆密码（写自己的）
        String accountType = "0"; // 登陆方式（0表示电话，1表示邮箱）
        /******************************************/

        String sessionId = null;
        String imgCode = null;

        /**首次测试成功后，请注释以下控制台输入语句**/
        /******************************************/
        System.out.println("请根据提示输入配置信息");
//        System.out.println("What is your sessionId :");
//        sessionId = sc.next();
        System.out.println("What is your courseVersion :");
        courseVersion = sc.next();
        System.out.println("What is your stuClassId :");
        stuClassId = sc.next();
        System.out.println("What is your loginName :");
        loginName = sc.next();
        System.out.println("What is your password :");
        password = sc.next();
        System.out.println("What is your accountType (phone is 0 | email is 1):");
        accountType = sc.next();
        /******************************************/

        Map<String, String> map = new HashMap<>();
        map.put("loginName", loginName);
        map.put("password", md5Pwd(password));
        map.put("imgCode", imgCode);
        map.put("accountType", accountType);
        // 登陆tmooc
        String res = sendPost("http://uc.tmooc.cn/login", map);

        // 通过截取res字符串，获取sessionId
        String[] resList = res.split("id");
        String res2 = resList[1];
        sessionId = res2.substring(3, 3+32);

        // 输出日志
//        System.out.println("sessionId = "+sessionId);
//        System.out.println("courseVersion = "+courseVersion);
//        System.out.println("stuClassId = "+stuClassId);
//        System.out.println("loginName = "+loginName);
//        System.out.println("password = "+password);
//        System.out.println("imgCode = "+imgCode);
//        System.out.println("accountType = "+accountType);
//        System.out.println("登陆tts = "+"http://tts.tmooc.cn/user/myTTS?sessionId="+sessionId+"&date="+str+"&courseVersion="+courseVersion+"&stuClassId="+stuClassId);
//        System.out.println("签到tts = "+"http://tts.tmooc.cn/studentCenter/studentSign?studentClaId="+stuClassId);

        // 登陆tts
        sendGet("http://tts.tmooc.cn/user/myTTS?sessionId="+sessionId+"&date="+str+"&courseVersion="+courseVersion+"&stuClassId="+stuClassId);
        // 签到tts
        sendGet("http://tts.tmooc.cn/studentCenter/studentSign?studentClaId="+stuClassId);

        System.out.println("======");
        System.out.println("签到完成（如果需要在浏览器看效果，请记得清空缓存）");
        System.out.println("======");

    }

    /**
     * 获取当前时间并格式化为URL的格式
     * @return
     */
    public static String getTimeURL() {
        // 获取当前时间
        String time = getTime();
        // 把生成的字符串按照空格分割
        String[] s = time.split(" ");
        // 遍历分割后的字符串，重新拼接
        String str = "";
        for (int i = 0; i < s.length; i++) {
            if (i==0) {
                // 如果是第一个，就什么也不加
                str += s[i];
            } else {
                // 其他情况，就在当前位置加前缀"%20"（%20在URL编码中表示空格）
                str += "%20"+s[i];
            }
        }
        //System.out.println(str);
        return str;
    }

    /**
     * 获取当前时间，并格式化为与网站相同格式
     * @return
     */
    public static String getTime() {
        // 创建当前时间点对象
        Date date = new Date();
        // 创建日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
        // 使用日期格式化工具将时间转换为指定格式字符串
        String str = simpleDateFormat.format(new Date());
        // 末尾手动拼接时区信息
        str = str+" GMT+0800%20(CST)";
        // 去除前两个中文字符串
        str = str.substring(6);
        // 设定国际化为：`英文/中国`
        Locale locale = new Locale("en", "CN");
        // 获取`英文/中国`对应的date字符串
        String us = DateFormat.getDateInstance(DateFormat.FULL, locale).format(date);
        // 分割，只要前两个字符串，原因是通过simpleDateFormat格式化后的时间字符串前两个字符串为中文
        String[] strs = us.split(", ");
        // 拼接末尾
        String time = strs[0].substring(0,3)+" "+strs[1].substring(0,3)+str;
        // 格式效果：Thu Oct 01 2020 12:32:47 GMT 0800 (CST)
        //System.out.println(time);
        return time;
    }
    
    /**
     * 发送HttpGet请求
     * @param url
     * @return
     */
    public static String sendGet(String url) {
        //1.获得一个httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //2.生成一个get请求
        HttpGet httpget = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            //3.执行get请求并返回结果
            response = httpclient.execute(httpget);
            //System.out.println("===response===");
            //System.out.println(response);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        String result = null;
        try {
            //4.处理结果，这里将结果返回为字符串
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
            //System.out.println("===entity===");
            //System.out.println(entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 发送HttpPost请求，参数为map
     * @param url
     * @param map
     * @return
     */
    public static String sendPost(String url, Map<String, String> map) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            //给参数赋值
            formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(entity);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity entity1 = response.getEntity();
        String result = null;
        try {
            result = EntityUtils.toString(entity1);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 密码Md5加密
    public static String md5Pwd(String pwd) {
        //创建加密计算器
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        //加密
        md5.update(pwd.getBytes());

        //转换输出
        return new BigInteger(1, md5.digest()).toString(16);
    }

}
