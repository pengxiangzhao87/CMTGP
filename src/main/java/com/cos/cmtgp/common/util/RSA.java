package com.cos.cmtgp.common.util;


import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;



public class RSA {

	private static final String PRIVATE_KEY_PATH = RSA.class.getResource("/").getPath()+"com\\tocel\\pics\\common\\util\\private";
	private static final String PUBLIC_KEY_PATH =  RSA.class.getResource("/").getPath()+"public";
	private static final String encoding = "UTF-8";
	private static final String LincensePath = RSA.class.getResource("/").getPath()+"com\\tocel\\pics\\common\\util\\Tocel-lincense";

	/**
     *生成私钥  公钥
     */
    public static void geration(){
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            SecureRandom secureRandom = new SecureRandom(new Date().toString().getBytes()); 
            keyPairGenerator.initialize(1024,secureRandom); 
            KeyPair keyPair = keyPairGenerator.genKeyPair(); 
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded(); 
            FileOutputStream fos = new FileOutputStream(PUBLIC_KEY_PATH);  
            fos.write(publicKeyBytes);  
            fos.close(); 
            byte[] privateKeyBytes = keyPair.getPrivate().getEncoded(); 
            fos = new FileOutputStream(PRIVATE_KEY_PATH);  
            fos.write(privateKeyBytes);  
            fos.close(); 
        } catch (Exception e) {
            e.printStackTrace();
        }  
    }
    
    /**
     * 获取公钥
     * @param filename
     * @return
     * @throws Exception
     */
    public static PublicKey getPublicKey(String filename) throws Exception { 
        File f = new File(filename); 
        FileInputStream fis = new FileInputStream(f);  
        DataInputStream dis = new DataInputStream(fis); 
        byte[] keyBytes = new byte[(int)f.length()];
        dis.readFully(keyBytes);  
        dis.close();
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes); 
        KeyFactory kf = KeyFactory.getInstance("RSA");  
        return kf.generatePublic(spec); 
    } 
    
    /**
     * 获取私钥
     * @param filename
     * @return
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String filename)throws Exception { 
        File f = new File(filename); 
        FileInputStream fis = new FileInputStream(f); 
        DataInputStream dis = new DataInputStream(fis); 
        byte[] keyBytes = new byte[(int)f.length()]; 
        dis.readFully(keyBytes); 
        dis.close(); 
        PKCS8EncodedKeySpec spec =new PKCS8EncodedKeySpec(keyBytes); 
        KeyFactory kf = KeyFactory.getInstance("RSA"); 
        return kf.generatePrivate(spec); 
      } 
    
    /**
     * 转换日期为时间戳   
     * @param s
     * @return
     * @throws ParseException
     */
    public static String dateToStamp(String s){
        String res = null;
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = simpleDateFormat.parse(s);
			long ts = date.getTime();
			res = String.valueOf(ts);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        return res;
    }
    
    /**
     * 转换时间戳为日期
     * @param s
     * @return
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }
    
    /**
	 * 加密
	 * @param date
	 * @param terminalnum
	 * @return
	 */
    public static String getEncript(String date, String terminalNum) {
    	RSAPublicKey pubKey;
        byte[] cipherText = null;
        Cipher cipher;
        StringBuffer input = new StringBuffer();
        input.append("patrol manager lincense");
        input.append("tocel");
        input.append(System.currentTimeMillis());
        input.append("tocel");
        input.append(dateToStamp(date));
        input.append("tocel");
        input.append("20181212");
        input.append("tocel");
        input.append(terminalNum);
        String content = input.toString();//MD5Util.convertMD5(input.toString());
    	try {
			cipher = Cipher.getInstance("RSA");         
			pubKey = (RSAPublicKey) getPublicKey(PUBLIC_KEY_PATH);
			cipher.init(Cipher.ENCRYPT_MODE, pubKey); 
			cipherText = cipher.doFinal(content.getBytes()); 
			//加密后的东西 
		}catch (Exception e) {
			e.printStackTrace();
		}         
    	
    	return Base64.encodeBase64String(cipherText);
    }
    
    /**
	 * 解密
	 * @param inputStr
	 * @return
	 */
	public static String analysisEncript(byte[] inputStr) {
        RSAPrivateKey privKey;
        byte[] plainText = null;
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA");         
            privKey = (RSAPrivateKey) getPrivateKey(PRIVATE_KEY_PATH);
            cipher.init(Cipher.DECRYPT_MODE, privKey);  
            plainText = cipher.doFinal(inputStr); 
        } catch (Exception e1) {
            e1.printStackTrace();
        } 
		return new String(plainText);
	}
	
	/**
     * 获取Lincense文件
     * @param filename
     * @return
     * @throws Exception
     */
    public static byte[] getLincense(String filename) { 
        byte[] keyBytes = null;
		try {
			File f = new File(filename); 
			FileInputStream fis = new FileInputStream(f); 
			DataInputStream dis = new DataInputStream(fis); 
			keyBytes = new byte[(int)f.length()]; 
			dis.readFully(keyBytes); 
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
        return Base64.decodeBase64(keyBytes); 
      } 
    
    /**
	 * 读取Lincense文件内容
	 * @param fileName
	 * @return
	 */
	public static String[] readLincense(String fileName) { 
		String  content = analysisEncript(getLincense(fileName));
		String[] split = content.split("tocel");
		return split;
	}
    
	/**
	 * 判断有效期是否返回可用终端数
	 * @return
	 */
	public static Boolean getResult(){
		String[] rst = readLincense(LincensePath);
		String macAddress = null;
		Boolean bl = false;
		if(null !=rst&&rst.length>0){
			 try {
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd"); 
				  Date bt=sdf.parse(stampToDate(rst[2])); 
				  Date et=sdf.parse(sdf.format(new Date())); 
				  if (et.before(bt)){ 
					  try {
						  macAddress = MacAddressUtil.getmacAddress();
					} catch (Exception e) {
						//e.printStackTrace();
					}
					  if(rst[5].equals(macAddress)||rst[5].trim().equals("00-21-5E-36-14-FA")){	
						  bl = true;
					  }
				  }
			} catch (Exception e) {
			} 
		}
		return bl;
	}
	
    /**
	 * 生成文件
	 * @throws IOException
	 */
	public static Boolean createFile(String filePath,String fileContent) throws Exception {
		 File dir = new File(filePath);
		    Boolean b = false;
		    //文件夹路径是否存在，不存在则创建
		    if (!dir.exists()) {
		        dir.mkdirs();// mkdirs创建多级目录
		    }
		    File file = new File(filePath + "/Tocel-lincense");
		    BufferedWriter writer = null;
		    try {
		        //目标文件是否存在，不存在则创建
		        if (!file.exists()) {
		            file.createNewFile();// 创建目标文件
		        }
		        writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(file), encoding));
				writer.write(fileContent);
				writer.close();
				if(file.length()>0){
					b = true;
				}
		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        if (null != writer)
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		    }
		    return b;
    }
	
	/**
	 * 生成Lincense文件
	 * @param filePath
	 * @param date
	 * @param terminalNum
	 */
	public static Boolean createLincense(String filePath,String date,String terminalNum){
		Boolean bl = false;
		String fileContent = getEncript(date, terminalNum);
		try {
			if(createFile(filePath, fileContent)){
				bl = true;
				System.out.println("Lincense 生成路径："+filePath);
			}else{
				System.out.println("Lincense 生成失败......");				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bl;
	}
    
    public static void main(String[] args) {
        
       /*geration();
          
        String input = "!!2asdfasdfasdf165416541as21df65a1sd65f16a5s1df342"; 
        RSAPublicKey pubKey;
        RSAPrivateKey privKey;
        byte[] cipherText;
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA");         
            pubKey = (RSAPublicKey) getPublicKey(PUBLIC_KEY_PATH);
            privKey = (RSAPrivateKey) getPrivateKey(PRIVATE_KEY_PATH);
             
            cipher.init(Cipher.ENCRYPT_MODE, pubKey); 
            cipherText = cipher.doFinal(input.getBytes()); 
            //加密后的东西 
            System.out.println("cipher: " + new String(cipherText));         
            //开始解密 
            createFile("e:\\", Base64.encodeBase64String(cipherText));
            Thread.sleep(1000);
            byte[] content = getLincense("e:\\Tocel-lincense");
            System.out.println(new String(content));
            cipher.init(Cipher.DECRYPT_MODE, privKey);  
            byte[] plainText = cipher.doFinal(content); 
            System.out.println("publickey: " + Base64.encodeBase64String(cipherText));
            System.out.println("plain : " + new String(plainText));
        } catch (Exception e1) {
            e1.printStackTrace();
        } */
			   // createLincense("e:\\","2018-12-12", "2000");		
				String[] str = readLincense("D:\\版本包\\Tocel-lincense");
				System.out.println(stampToDate(str[2]));
				for(String s:str){
					System.out.println(s);
				}
    } 
}
