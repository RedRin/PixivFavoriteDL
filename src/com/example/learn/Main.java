package com.example.learn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Main {
	//private static final String IMGURL_REGEX = "<img.*?src=.*?>";
	//.*?中?跟着其他限制符后面表示非贪婪的，尽量少匹配。
	
	public static final String main_url = "https://www.pixiv.net/bookmark.php?rest=show&p=";
	
	static int page = 1;
	
	
	public static void main(String[] args) {
		
		try {
			Tool.login();
			
			while(Download.downloadPerPage(main_url,page)) {
				page++;
			}
			
			Tool.readStringFromConsole("press enter to exit!");
		} catch(Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}
		
	}

}
