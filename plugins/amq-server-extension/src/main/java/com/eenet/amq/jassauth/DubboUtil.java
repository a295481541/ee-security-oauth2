package com.eenet.amq.jassauth;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.eenet.authen.IdentityAuthenticationBizService;
import com.eenet.authen.request.AppAuthenRequest;
import com.eenet.base.SimpleResponse;

public class DubboUtil {

	private static IdentityAuthenticationBizService service;

	private static String profilepath = "config.properties";

	private static Properties props = new Properties();

	static {
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(profilepath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static IdentityAuthenticationBizService getService() throws Exception {
		

		if (service == null) {
			ApplicationConfig application = new ApplicationConfig();
			application.setName("jms");
			// 连接注册中心配置
			
			String zookeeperUrl = props.getProperty("zookeeper");
			
			if (zookeeperUrl == null || "".equals(zookeeperUrl.trim())) {
				throw new Exception("please appoin the zookeeper url so that the services can be worked ");
			}
			String []  urls = zookeeperUrl.split(",");
			List<RegistryConfig> registryConfigs = new ArrayList<>();
			for (int i = 0; i < urls.length; i++) {
				RegistryConfig registry = new RegistryConfig();
				registry.setAddress("zookeeper://" +urls[i]);
				registryConfigs.add(registry);
				System.out.println("zookeeper://" +urls[i]);
				
			}
			
			
			// 引用远程服务
			ReferenceConfig<IdentityAuthenticationBizService> reference = new ReferenceConfig<IdentityAuthenticationBizService>();
			reference.setApplication(application);
			//reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
			reference.setRegistries(registryConfigs);
			reference.setInterface(IdentityAuthenticationBizService.class);
			
			System.out.println( reference.get());
			
			service = reference.get();
		}

		return service;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("=======");
		IdentityAuthenticationBizService service = null;
		AppAuthenRequest request = new AppAuthenRequest();
		
		
		String userName = "AC9CCD9AD6194E1CAD8C05FE718DD6C6";
		String password = encrypt("pASS25#" + "##" + System.currentTimeMillis());
		
		request.setAppId(userName);

		request.setAppSecretKey(password);
		System.out.println("=======");
		SimpleResponse response = null;
		for (int i = 0; i < 10; i++) {
			service = getService();
			response = service.appAuthen(request);
			System.out.println(response.isSuccessful());
		}
		System.out.println("=======");

	}
	
	public static String encrypt(String plaintext) throws Exception {
		String sslPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3ofG3TuzCBaolNYFuTVkOv8yN" + "\r"
				+ "B+u3KvSwqqMYsqAKK/q518kyVnl5Mq2h4kqE6YKaV1hJgsd0n4McjCg06xXQP1nh" + "\r"
				+ "w3kjX/cL0W6jKTTERDnNDK6ifIdczsFOsaFMSxuA9T3Laji3WmTz4sDpkBN7Ymql" + "\r" + "yzqa7HG12GH4zODWtwIDAQAB"
				+ "\r";

		byte[] buffer = Base64.decodeBase64(sslPublicKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
		RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");// RSA是加密方法，ECB是加密模式，PKCS1Padding是填充方式
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] output = cipher.doFinal(plaintext.getBytes("UTF-8"));

		return Base64.encodeBase64String(output);
	}
}
