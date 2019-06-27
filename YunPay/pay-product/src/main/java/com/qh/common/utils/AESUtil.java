package com.qh.common.utils;

import com.qh.pay.api.utils.Md5Util;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

public class AESUtil {  
  
    /** 
     * 密钥算法 
     */  
    private static final String ALGORITHM = "AES";  
    /** 
     * 加解密算法/工作模式/填充方式 
     */  
    private static final String ALGORITHM_MODE_PADDING = "AES/ECB/PKCS7Padding";


    private static final String tokeyKey = Md5Util.MD5("chgdx");
    /** 
     * AES加密 
     *  
     * @param data 加密内容
     * @param password 加密密码
     * @return 
     * @throws Exception 
     */  
    public static String encryptData(String data,String password) throws Exception {  
        Security.addProvider(new BouncyCastleProvider());
        // 创建密码器  
        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING);  
        // 初始化为加密模式的密码
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password));
        // 加密
        byte[] result = cipher.doFinal(data.getBytes());
        
        return  Base64.encodeBase64String(result);
    }  
  
    /** 
     * AES解密 
     *  
     * @param base64Data 解密内容
     * @param password 解密密码
     * @return 
     * @throws Exception 
     */  
    public static String decryptData(String base64Data,String password) throws Exception {  
        Security.addProvider(new BouncyCastleProvider());
        // 创建密码器  
        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING); 
        //使用密钥初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password));
        //执行操作
        byte[] result = cipher.doFinal(Base64.decodeBase64(base64Data));
        
        return new String(result, "utf-8");  
    }
    
    /**
     * 生成加密秘钥
     *
     * @return
     */
    private static SecretKeySpec getSecretKey(String password) {
        SecretKeySpec key = new SecretKeySpec(Md5Util.MD5(password).toLowerCase().getBytes(), ALGORITHM);
        return key;
    }


    public static String tokenEnc(String content){
        try {
            return encryptData(content,tokeyKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }
    public static String tokenDec(String content){
        try {
            return decryptData(content,tokeyKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }


    public static void main(String[] args) throws Exception {
        String aa = encryptData("<root>" +
                "<out_refund_no><![CDATA[R18032701140]]></out_refund_no>" +
                "<out_trade_no><![CDATA[OT18032701139]]></out_trade_no>" +
                "<refund_account><![CDATA[REFUND_SOURCE_RECHARGE_FUNDS]]></refund_account>" +
                "<refund_fee><![CDATA[1]]></refund_fee>" +
                "<refund_id><![CDATA[50000106222018032703920525020]]></refund_id>" +
                "<refund_recv_accout><![CDATA[支付用户零钱]]></refund_recv_accout>" +
                "<refund_request_source><![CDATA[API]]></refund_request_source>" +
                "<refund_status><![CDATA[SUCCESS]]></refund_status>" +
                "<settlement_refund_fee><![CDATA[1]]></settlement_refund_fee>" +
                "<settlement_total_fee><![CDATA[1]]></settlement_total_fee>" +
                "<success_time><![CDATA[2018-03-27 12:16:50]]></success_time>" +
                "<total_fee><![CDATA[1]]></total_fee>" +
                "<transaction_id><![CDATA[4200000063201803276508012305]]></transaction_id>" +
                "</root>", "123456");
        try {
            System.out.println(aa);
            String B = tokenEnc(aa);
            System.out.println(tokenDec(B));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}