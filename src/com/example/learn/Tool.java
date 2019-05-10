package com.example.learn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

public class Tool {

	public static Random r = new Random();
	
	public static final String[] user_agent_list = new String[] {
			"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36"
	};
	

	public static CloseableHttpClient httpClient = createSSLClientDefault();
	public static String useage = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
    public static RequestConfig configtime=RequestConfig.custom().setCircularRedirectsAllowed(true).setSocketTimeout(10000).setConnectTimeout(10000).build();
    
    
    // client工具函数，信任对方（https）所有证书
	private static CloseableHttpClient createSSLClientDefault() {
		try {  
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {  
                //信任所有证书  
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {  
                    return true;  
                }  
            }).build();  
            SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslContext);  
            return HttpClients.custom().setSSLSocketFactory(sslFactory).build();  
        } catch (Exception e) {  
        }  
        return  HttpClients.createDefault();  
	}
	
	/**
	 * 获取网页html源码
	 * @param url
	 * @return
	 */
	public static String getHTML(String url) {
		String html="";
		
        HttpGet httpget = new HttpGet(url);
        httpget.addHeader("User-Agent", useage);
        httpget.setConfig(configtime);
        
        try {
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            html = EntityUtils.toString(entity, "utf-8");
            httpget.releaseConnection();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        	System.out.println("协议错误");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return html;
	}
	
	/**
	 * 获取p站的postkey
	 * @return
	 */
	private static String getPostKey() {
		String postKey = "";
		
		String target = "https://accounts.pixiv.net/login?lang=zh&source=pc&view_type=page&ref=wwwtop_accounts_index";
		String html = getHTML(target);
		String POSTKEY_REGEX = "[\"]pixivAccount.postKey[\"]:[\"](.*?)[\"],";
		
		Pattern p = Pattern.compile(POSTKEY_REGEX);
		Matcher m = p.matcher(html);
		if(m.find()) {
			postKey = m.group(1);
		}
		return postKey;
	}
	
	/**
	 * 读取输入
	 */
	public static String readStringFromConsole(String prompt) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String str = null;
		try {
			System.out.print(prompt);
			str = br.readLine();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return str;
	}
	
	/**
	 * 模拟登录
	 * @throws IOException  获取状态码失败
	 * @throws ParseException 
	 */
	static void login() throws ParseException, IOException {
		String pixiv_id = "";
		String password = "";
		
		pixiv_id = readStringFromConsole("pixiv_id: ");
		password = readStringFromConsole("password: ");
		
		List<NameValuePair> para = new ArrayList<NameValuePair>();
		String postkey = getPostKey();
		//System.out.println(postkey);
		para.add(new BasicNameValuePair("post_key",postkey));
		
		// headers只要这两个就可以了,之前加了太多其他的反而爬不上
		Map<String,String> header = new HashMap<String,String>();
		header.put("Referer", "https://accounts.pixiv.net/login?lang=zh&source=pc&view_type=page&ref=wwwtop_accounts_index");
		header.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		
		para.add(new BasicNameValuePair("pixiv_id",pixiv_id));
		para.add(new BasicNameValuePair("password",password));
		para.add(new BasicNameValuePair("return_to","http://www.pixiv.net/"));
		
		HttpPost httpPost = new HttpPost("https://accounts.pixiv.net/api/login?lang=zh");
		for(String key : header.keySet()) {
			httpPost.addHeader(key,header.get(key));
		}
		httpPost.setEntity(new UrlEncodedFormEntity(para,"utf-8"));
		CloseableHttpResponse res = httpClient.execute(httpPost);
		int status_code = res.getStatusLine().getStatusCode();
		if(status_code == 200) {
			System.out.println("请求页面成功");
		}
		//System.out.println(EntityUtils.toString(res.getEntity(),"utf-8"));
		
		httpPost.releaseConnection();
	}
}
