package com.eenet.test;

import org.junit.Test;

import com.eenet.util.EEBeanUtils;
import com.eenet.util.cryptography.EncryptException;
import com.eenet.util.cryptography.RSAEncrypt;
import com.eenet.util.cryptography.RSAUtil;

public class SSOSystemTester {
	@Test
	public void test() throws EncryptException{
		RSAEncrypt encrypt = new RSAEncrypt();
		encrypt.setModulus("106712420423343952170664978944926079393579997057563195783133824550978845577607200365836363103675051075681355661561521905638127714585171548814200428974018569289446040295164277425200303883220785353079858223685449216711620468446418526635344201040310751955657353841399373391132369275713530371098532522210318693391");
		encrypt.setPublicExponent("65537");
		
		String code = EEBeanUtils.getUUID();
		String secretKey = EEBeanUtils.getUUID();
		
		String cipertext = RSAUtil.encrypt(encrypt, secretKey);
		System.out.println("code : "+code);
		System.out.println("secretKey : "+secretKey);
		System.out.println("cipertext length : "+cipertext.length());
		System.out.println("cipertext : "+cipertext);
		
		System.out.println("cipertext-A : "+RSAUtil.encrypt(encrypt, "1429D423ED444F2983D0B048B409F173"));
		System.out.println("cipertext-B : "+RSAUtil.encrypt(encrypt, "1429D423ED444F2983D0B048B409F173"));
		System.out.println("cipertext-C : "+RSAUtil.encrypt(encrypt, "1429D423ED444F2983D0B048B409F173"));
		System.out.println("cipertext-D : "+RSAUtil.encrypt(encrypt, "1429D423ED444F2983D0B048B409F173"));
	}
}
