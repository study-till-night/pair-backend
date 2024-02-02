package com.shuking.pairBackend.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CallingThirdPartyInterfacesUtil {
    //    请求方法数组
    private static final String[] methods = new String[]{"get", "post", "put", "delete", "options"};

    /**
     * 以post方式调用第三方接口,若使用get，则在代码第40行打开，两种方式就都可以了
     *
     * @param pathUrl 调用的url 例：http://192.168.0.149:8090/toll
     * @param data    //调用参数，必须是一个JSON
     */
    public static void doRequest(String pathUrl, String data, int method) {
        System.out.println("data" + data);
        System.out.println("pathUrl" + pathUrl);
        OutputStreamWriter out = null;
        BufferedReader br = null;
        //        高频次拼接使用StringBuilder效率更高
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(pathUrl);
            //打开和url之间的连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //请求方式
            conn.setRequestMethod(methods[method]);

            //设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");

            //DoOutput设置是否向httpUrlConnection输出，DoInput设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个
            conn.setDoOutput(true);
            conn.setDoInput(true);

            /*
              下面的三句代码，就是调用第三方http接口
             */
            //获取URLConnection对象对应的输出流
            out = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
            //发送请求参数即数据
            out.write(data);
            //flush输出流的缓冲
            out.flush();

            /*
              下面的代码相当于，获取调用第三方http接口后返回的结果
             */
            //获取URLConnection对象对应的输入流
            InputStream is = conn.getInputStream();
            //构造一个字符流缓存
            br = new BufferedReader(new InputStreamReader(is));
            String str = "";
            while ((str = br.readLine()) != null) {
                result.append(str);
            }
            System.out.println("返回结果--》》" + result);
            //关闭流
            is.close();
            //断开连接，disconnect是在底层tcp socket链接空闲时才切断，如果正在被其他线程使用就不切断。
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
