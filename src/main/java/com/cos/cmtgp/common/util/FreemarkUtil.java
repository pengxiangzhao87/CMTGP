package com.cos.cmtgp.common.util;

import java.io.StringWriter;
import java.io.Writer;
import java.security.SecureRandom;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * freemark工具类
 *
 */
public class FreemarkUtil {
	
	/**
	 * 把模板解析成字符串
	 * @param tempStr	模板字符串
	 * @param data		模板的变量
	 */
	public static String parse(String tempStr, Object data) {
		if(StringUtil.isNotEmpty(tempStr) && (tempStr.indexOf("${") != -1 || tempStr.indexOf("</#") != -1)) {
			
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
			StringTemplateLoader stringLoader = new StringTemplateLoader();
			stringLoader.putTemplate("myTemplate", tempStr);
			cfg.setTemplateLoader(stringLoader);
			try {
				Template temp = cfg.getTemplate("myTemplate", "utf-8");
				Writer out = new StringWriter(2048);
				temp.process(data, out);
				return out.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tempStr;
	}

	public static String get3rdSeesion(int length){
		char[]chars="0123456789abcdefghijklmnopqrwtuvzxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		char[]saltchars=new char[length];
		SecureRandom random=new SecureRandom();
		for(int i=0;i<length;i++)
		{
			int n=random.nextInt(chars.length);
			saltchars[i]=chars[n];
		}
		String salt=new String(saltchars);
		return salt;
	}



}
