package com.example.learn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Download {
	public static final String base_url = "https://www.pixiv.net/";
	
	static Map<String,Map<String,String>> getImgSrcs(String main_url, int page) {
		//存referer和图片src
		Map<String,Map<String,String>> imgSrcs = new HashMap<String,Map<String,String>>();
		
		//获取一页的要跳转的地址,同时也是referer
		String html = Tool.getHTML(main_url+page);
		
		//String urls_regex = "<a href=\"(.*?)\".*?style=\"display:block\">";
		Document doc = Jsoup.parse(html);
		Elements atags = doc.select("[style=display:block]");
		
		List<String> referers = new ArrayList<String>();
		for(Element e : atags) {
			String href = e.attr("href");
			referers.add(base_url + href);
		}
		
		System.out.println("第 " + page + " 页有 "+ atags.size() +" 张图片。");
		long startTime = System.currentTimeMillis();
		System.out.print("少女获取图片中......");
		
		//跳转到下一层页面并找到图片src
		Iterator<String> iter = referers.iterator();
		while(iter.hasNext()) {
			String jump_url = iter.next();
			String jump_html = Tool.getHTML(jump_url);

			String imgSrc_regex = "\"original\":\"(.*?)\"";
			String authorID_regex = "\"authorId\":\"(.*?)\"";
			
			Pattern p_imgSrc = Pattern.compile(imgSrc_regex);
			Matcher m_imgSrc = p_imgSrc.matcher(jump_html);
			
			Pattern p_authorID = Pattern.compile(authorID_regex);
			Matcher m_authorID = p_authorID.matcher(jump_html);
			
			//找作者pid
			String authorID = "";
			if(m_authorID.find()) {
				authorID = m_authorID.group(1);
			}
			
			//找到Src
			if(m_imgSrc.find()) {
				String src = m_imgSrc.group(1).replaceAll("\\\\", "");
				
				Map<String,String> img_authorID_src = new HashMap<String,String>();
				img_authorID_src.put(authorID, src);
				if(imgSrcs.get(jump_url) != null) {
					System.out.println("冲突");
				} else {
					imgSrcs.put(jump_url, img_authorID_src);
				}
			}
			
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("  用时： " + (endTime-startTime) + " 微秒。");
		return imgSrcs;
	}
	
	
	
	public static void download_img(String referer, Map<String,String> img_authorID_src) {
		try {
			for(String authorID : img_authorID_src.keySet()) {
				String imgSrc = img_authorID_src.get(authorID);
				
				System.out.print("开始下载: " + imgSrc + "   ");
				long startTime = System.currentTimeMillis();
				
				HttpGet getImg = new HttpGet(imgSrc);
				Map<String,String> header = new HashMap<String,String>();
				header.put("Referer", referer);
				//可随机取一个User-Agent来破反爬
				header.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
				//同时需要代理ip
				
				for(String key : header.keySet()) {
					getImg.addHeader(key,header.get(key));
				}
				
				CloseableHttpResponse res = Tool.httpClient.execute(getImg);
				int status_code = res.getStatusLine().getStatusCode();
				
				if(status_code == 200) {
					String img_type = imgSrc.substring(imgSrc.lastIndexOf('.'), imgSrc.length());
					String imgID = referer.substring(referer.lastIndexOf('=') + 1, referer.length());
					
					File save_dir = new File(".\\download_imgs\\");
					if(!save_dir.exists()) {
						if(!save_dir.mkdirs()) 
							System.out.println("新建文件夹失败.");
					}
					File save_file = new File(".\\download_imgs\\" + authorID + "_" + imgID + img_type);
					
					
					if(save_file.exists()) {
						System.out.println("图片已存在");
					} else {
						FileOutputStream fos = new FileOutputStream(save_file);
						int len = 0;
						byte[] buffer = new byte[1024];
						while((len = res.getEntity().getContent().read(buffer,0,buffer.length)) != -1) {
							fos.write(buffer,0,len);
						}
						
						fos.close();
						long endTime = System.currentTimeMillis();
						System.out.println("用时： " + (endTime-startTime) + " 微秒。");
					}
					
					
				} else {
					System.out.println("失败!!《《====");
					
				}
				
				getImg.releaseConnection();  //不释放卡死！！！
			}
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("error");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("error");
			e.printStackTrace();
		}
	}
	
	public static boolean downloadPerPage(String main_url, int page) {
		Map<String,Map<String,String>> imgSrcs = getImgSrcs(main_url,page);
		if(imgSrcs.isEmpty()) {
			return false;
		}
		
		for(String referer : imgSrcs.keySet()) {
			Map<String,String> img_authorID_src = imgSrcs.get(referer);
			download_img(referer, img_authorID_src);
		}
		
		return true;
	}
}
