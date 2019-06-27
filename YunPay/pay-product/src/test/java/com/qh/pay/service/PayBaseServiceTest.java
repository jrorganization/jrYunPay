package com.qh.pay.service;

import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.OrderState;
import com.qh.redis.service.RedisUtil;

/**
 * @ClassName PayBaseServiceTest
 * @Description 支付基础类测试
 * @Date 2017年11月22日 下午5:48:22
 * @version 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan("com.qh.pay")
public class PayBaseServiceTest {
	
	@Autowired
	public PayService payService; 
	
	public static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayBaseServiceTest.class);
	/****
	 * 商户号
	 */
	public final static String merchNo = "admin";
	/**
	 * 公钥 --聚富
	 */
//	public final static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCMlNqJh3JG6shlMJ0OJ42QnuG9OVUiBlcpbUXbaaprUjF1XTqDaUJZLvk5fkRDAgZAC/CbyYOOoZBpp8y3CnnCSPtJ8oKoLuQOcN1hW4snE0VP+J2wKMQQyjmzFK4MiRRDE6oxD2nWFe517zl8IOJYZWK3egTIXezoidLG0bucZwIDAQAB";
   	public final static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRATDdlWIjeXHlqVpn2CiwMWtkTJRO998UPlYoGw9cS7ZJVU8tChVKinUbp2HCMdpGHsdnAw43ixw49u0K+mQCyo2/Y6HURNwclvOIWt8rBoH3FmjlrnkmC8lyD8ULtvGUK236O0jP430tzdCbaQpRUPqPEH5pWK4TQUx3pcfkPQIDAQAB";

    /**
	 * 私钥 --聚富
	 */
    public final static String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJEBMN2VYiN5ceWpWmfYKLAxa2RMlE733xQ+VigbD1xLtklVTy0KFUqKdRunYcIx2kYex2cDDjeLHDj27Qr6ZALKjb9jodRE3ByW84ha3ysGgfcWaOWueSYLyXIPxQu28ZQrbfo7SM/jfS3N0JtpClFQ+o8QfmlYrhNBTHelx+Q9AgMBAAECgYBQw08KO6e7hmrPtbGq4RRYfk4IQTbhfOF9DvNBx0CY8XqIAcHlnhYZvGBZlLK4TLf8EiyRzXvfp9WCTNXeJXQJ2dQV1pHRunq9jIn/KZJRb52jAkQ0qx82SPEsYPJCbjp+dZsT06JCvbcd3iTZi22g1RQRHOwz4iSAq1hvhjCr6QJBANjS8/maKXQMfCMcNJDXyM+hawzNuz3JgCtHJZE3cf/jeoWqgmLswiJ51/TmGAJbCSsEvRxDi4IGMo6DmfEayBcCQQCrNEgUCPvRPhms+YpT5nTCDkwpMxh9pccBveqvklwe5tlOgdPXQDjmaLVuEfjtpARBIzV+ey/guWQryNwk+NbLAkEAj/giv825ULEpjDaiQLrHP/aymiHQ/knZrOLk8vOZ4ostQ6vgP8dtcG7vElHmB0pjYAkZeLbw3zk2QKLpiMp7qQJATqjrwQDLqjytEVNp4diNpqdpCLjoNLqZL8yxak+FsdEA4Ng3m7tvKTXMvjDVvWHRbpgduOoiek7TnmZf90C5dwJBAKU0f1QcHh6pIRCXFGEzW4sMzgJhQi+6zSa1ggEoMnN/19eAyVJG12VISbOvdTU8TtwsrSIikNQ3/dkh2nH80UA=";

	/**
	 * 熊猫公钥
	 */
	public final static String PublicKeyXiongmao = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCQ6Apl/5Y9p/BvwRNXLLX+pCCCKKjyP4/7grgD+xAzS93u1CwDvYHoiyo3vsQhmmHX9Mnhci5khfIZffDipkMdKjaEvOutCGFwIHzHC08d7x/hL5XdJ/rFJuSwScEK7xmcJpfmNKHvpMBUdh48Hxifr9B9GuEUXbopZWZxfzJCzQIDAQAB";
	/**
	 * 熊猫公私钥
	 */
	public final static String privateKeyXiongmao = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJDoCmX/lj2n8G/BE1cstf6kIIIoqPI/j/uCuAP7EDNL3e7ULAO9geiLKje+xCGaYdf0yeFyLmSF8hl98OKmQx0qNoS8660IYXAgfMcLTx3vH+Evld0n+sUm5LBJwQrvGZwml+Y0oe+kwFR2HjwfGJ+v0H0a4RRduillZnF/MkLNAgMBAAECgYB87WARobEwhLnYTxfSfzyERZG1RUKqXzxNtNvaqrfD0bOLdKZhrx7xkhEasD+9TLDwEx19XQg8J/KaIabscDTI/jf7hvuyeL0mERFM3NXuLo1l1R56n8h97FmgTBi8d9Ql7ndBAwwzTK8cfKYYpzLG6+SrNsWZtJ+pvWkCQtkjgQJBAOQ4zMNLSCLueOcm+Iw5yTt//6QYdptOrLzPZbP1WoQQ/FvcblsmcRQb1P65TxQ4vSXh3/EAwDOgFnKN+zTCBS0CQQCiizOT+ZUrxTBP7wFo0ceaWTz2Xh//biCvGYr9DyEA8ve041VU8sJwNWUlw1W1lUPdJyG9c+30nz6QCqAFXPghAkEA4mWqU036CJUTIROa2th0VO8cNbgC6Px6BW+kj4okugBzp9kbLJcM9ArMF8jStte2Y78XvWemQ1BbFFbeza5vHQJAfAlTu7j6n2Mjgev2HGHxOpSck7iyHD6SzGvmh0PzQIEoi63rIR77R5tHa3DLR/z2w52n/qWn0UNv/4VMJauTYQJAIBLfNg714fYrNCgEHkVKym91/2bFj5py9XKGhkf1LxoBStiAiy6EzCHkWHethT9yg3blwOnQYz1f+nvBISdenw==";



    /**
	 * 公钥 --商户
	 */
	public final static String mcPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCFr5fSL3N0qa+tpIRFn/ApDfeuOMrvhz3Cb3T94by7KigO57ppkMadAOG2wLV5S6QA5WeN5oZWHzNUnYZbn6cFE38cV8LX0ABMl0A0x5O00NCMTCkxxUZ/5IlrK6SYEjk75vSiimtlAI9ZW/F8RKqzVoOr5pHZJ4tRSXaR5VHO0wIDAQAB";
	/**
	 * 私钥---商户
	 */
	public final static String mcPrivateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIWvl9Ivc3Spr62khEWf8CkN9644yu+HPcJ"
			+ "vdP3hvLsqKA7nummQxp0A4bbAtXlLpADlZ43mhlYfM1SdhlufpwUTfxxXwtfQAEyXQDTHk7TQ0IxMKTHFRn/kiWsrpJgSOTvm9KKKa2"
			+ "UAj1lb8XxEqrNWg6vmkdkni1FJdpHlUc7TAgMBAAECgYBNlfoDtxxHoc9edHN7wPXtrbiIOVe1qgSy2mLIkYEqEq5K8Dvk1mweZIuat"
			+ "77alYaqKnluBlMCmnr86as3c7HHTQlh8tlOOSnmwLzacVF453FvKAjvH9ti1nSf6dk9yCoDcsgulOYnqqRbAvVg+evBmmWuIVqZxvwe"
			+ "CxNERo98CQJBANFPtCSJdzMDk0uiE0r9nwiESYyX1n0NCozHKc6kSuGilx30xrrcedMbZyKDTCYgogp+d+QEzYddqq0Gj67jtXcCQQ"
			+ "CjgXQjgaFfUEBsFQ2menYGQgawCGnxYCJ7oUlBUScJrFpFhosHcBaoq69acQyGkC6kOu/jjuODjAAzjUVn4biFAkEAlu9tzOcgALZ0U"
			+ "hb26J3JP5/9VZfsgNKVp/y6phuNL/ZKGLz5TahNZTEehyG9GMVxdDXMiK3588JUoF7Z39iucwJBAJYsUyA9cprZWaIroBL0zSwoPn41"
			+ "7CBPPLyyQVclkyZWT78luNIHCDi5H2CBDpEVIlGi9CvcVGjBEHpI2aN09QUCQCg0j5IEsCeinYje4Pjs6v8y6GdiW6qUl8p2pol1LBt"
			+ "R/ycMcYkJWSUN1Ffgz84cRCkxuLS6oxyyyLporbj4kig=";

	/***
	 * 支付域名
	 */
	public final static String url = "http://localhost:8181/pay/order";
	
	/****
	 * 绑卡域名
	 */
	public final static String card_url = "http://localhost:8888/pay/card";
	
	/**
	 * @Description 设置订单支付中 用于查询
	 * @param orderNo
	 */
	public void setOrderIng(String orderNo) {
		Order order = RedisUtil.getOrder(merchNo, orderNo);
		if(order == null){
			logger.info("订单不存在：{},{}",merchNo,orderNo);
		}
		order.setOrderState(OrderState.ing.id());
		RedisUtil.setOrder(order);
	}

}
