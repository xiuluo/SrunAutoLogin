package cn.cxfer;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by xiuluo on 2017/1/8.
 */
public class SrunAutoLogin {
    public static void main(String[] args) {
        String authIP = "211.70.160.3";//认证服务器的ip
        String account = "net";
        String password = "net";
        String netPost = String.format("action=login&username=%s&password=%s&ac_id=1&user_ip=&nas_ip=&user_mac=&save_me=0&ajax=1", account, password);
        String netLength = "90";
        try {
            if (needLogin())
                doLogin(netPost, netLength, authIP);
            else
                System.out.println("能够访问外网，无需重复登陆");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static boolean needLogin() throws IOException {
        boolean flag = false;
        StringBuilder buffer = new StringBuilder();
        String url = "http://qq.com/";//需要一个不会返回200状态码的链接
        URL serverUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
        //发送get请求
        connection.setRequestMethod("GET");
        //必须设置false，否则会自动redirect到重定向后的地址
        connection.setInstanceFollowRedirects(false);
        connection.addRequestProperty("Accept-Charset", "UTF-8;");
        connection.addRequestProperty("Referer", "http://qq.com/");
        connection.connect();
        //未登录情况下所有站点均返回200状态码，深澜直接挟持http连接插入js跳转代码的
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            flag = true;//需要登录
        }
        return flag;
    }
    private static void doLogin(final String post, final String content_length, final String host) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://" + host + "/include/auth_action.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                //设置连接属性
                connection.setRequestProperty("Host", host);
                connection.setRequestProperty("Origin", "http://" + host);
                connection.setRequestProperty("Referer", "http://"+ host + "/srun_portal_pc.php?url=&ac_id=1");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", content_length);
                connection.setRequestProperty("Charset", "UTF-8");

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(post);
                out.close();

                int responseCode = connection.getResponseCode();
                if (HttpURLConnection.HTTP_OK == responseCode) {
                    StringBuilder buffer = new StringBuilder();
                    String line;
                    BufferedReader responseReader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    );
                    while ((line = responseReader.readLine()) != null) {
                        buffer.append(line);
                    }
                    responseReader.close();
                    System.out.println("服务器返回消息" + buffer.toString());
                    if (buffer.toString().matches("login_ok,[\\w\\d,%]+")) {
                        System.out.println("登陆成功");
                    } else {
                        System.out.println("登陆失败");
                        System.out.println(buffer.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
