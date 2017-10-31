/* SHA-1 Implementation */
package org.unl.cryptoanalysis.tools;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MySHA1 {

	private static boolean debug = false;
	private String n;
	long two_pow_32 = (long)Math.abs(Math.pow(2, 32));
	long AA ,BB,CC,DD,EE;

	/* Logical functions */
	private long not(long x) {
		return (two_pow_32-x-1);
	}

	private long F(long x, long y, long z) {
		return ((x&y)|(not(x)&z));
	}
	
	private long G(long x, long y, long z) {
		return (xor(xor(x,y),z));
	}
	
	private long H(long x, long y, long z) {
		return ((x&y)|(x&z)|(y&z));
	}
	
	private long I(long x, long y, long z) {
		return (xor(xor(x,y),z));
	}
	
	private long xor(long x, long y) {
		return (x&not(y))|(not(x)&y);
	}
	
	/* Single round operation */
	private void operation(long chunk, int round) {
		long k = 0;
		long a = AA, b= BB, c = CC, d = DD, e = EE;
		long temp = ((a<<(32+5)>>>32)|(a>>>(32-5)));

		/* Assign values based on round , note round1 = 1-20 iterations and so on */
		switch (round) {
		case 1:
			temp+=F(b,c,d);
			k = 0x5A827999L;
			break;
		case 2:
			temp+=G(b,c,d);
			k = 0x6ED9EBA1L;
			break;
		case 3:
			temp+=H(b,c,d);
			k = 0x8F1BBCDCL;
			break;
		case 4:
			temp+=I(b,c,d);
			k = 0xCA62C1D6L;
			break;
		}
		temp+=(e+k+chunk);
		EE = d;
		DD = c;
		CC = ((b<<(32+30)>>>32)|(b>>>(32-30)));
		BB = a;
		AA = temp% two_pow_32;
	}
	
	public String getN() {
		return n;
	}

	public void setN(String n) {
		this.n = n;
	}
	
	
	/* Actual Hashing method */
	public String getDigest() {

		long A = 0x67452301L;
		long B = 0xEFCDAB89L;
		long C = 0x98BADCFEL;
		long D = 0x10325476L;
		long E = 0xC3D2E1F0L;

		if(debug) System.out.println("Inital H's:\n\n  H0="+A+", H1="+B+", H2="+C+", H3="+D+", H5="+E);

		/* Divide, pad and extend each message block to 160 bits for SHA-1*/
		long chunks[][] = padAndExtendMessage();
		int num = chunks.length;

		if(debug) System.out.println("\nOPERATIONS:");

		for (int m = 0; m < num; m++) {
			if(debug) System.out.println("\n[Block="+m+"]");

			AA = A;
			BB = B;
			CC = C; 
			DD = D;
			EE = E;

			/* call 80 times the operation method */
			for (int i = 0; i < 80; i++) {
				operation(chunks[m][i], (i/20)+1);
				if(debug) System.out.println("  [i = "+i + "] A="+AA+", B="+BB+", C="+CC+", D="+DD+", E="+EE);
			}
			A = (A + AA) % two_pow_32;
			B = (B + BB) % two_pow_32;
			C = (C + CC) % two_pow_32;
			D = (D + DD) % two_pow_32;
			E = (E + EE) % two_pow_32;	
			if(debug) System.out.println("Block="+m+" Processed: H0="+A+", H1="+B+", H2="+C+", H3="+D+", H4="+E);
		}
				String A_Hex = Long.toString(A,16);
				String B_Hex = Long.toString(B,16);
				String C_Hex = Long.toString(C,16);
				String D_Hex = Long.toString(D,16);
				String E_Hex = Long.toString(E,16);
				
				/* Finally append and return the hash */
				while(A_Hex.length()!=8) A_Hex = "0"+A_Hex;
				while(B_Hex.length()!=8) B_Hex = "0"+B_Hex;
				while(C_Hex.length()!=8) C_Hex = "0"+C_Hex;
				while(D_Hex.length()!=8) D_Hex = "0"+D_Hex;
				while(E_Hex.length()!=8) E_Hex = "0"+E_Hex;
				
				if(debug) System.out.println("\nFINAL VALUES:\n  H0_Hex="+A_Hex + " H1_Hex="+B_Hex + " H2_Hex="+C_Hex
						+ " H3_Hex="+D_Hex +" H4_Hex="+E_Hex);
				
				return A_Hex+B_Hex+C_Hex+D_Hex+E_Hex;
			}
	
	/* This method pads 1, length of message, zeros if required, 
	 * breaks message into blocks and extends each block of 32-bit to 160-bit blocks
	 */
	private long[][] padAndExtendMessage() {
		
		if(debug) System.out.println("\nAPPEND PADDING BITS,LENGTH AND EXTEND MESSAGE:\n");
		String hexSize = Long.toHexString(n.length()*8);
		while(hexSize.length()<16)
			hexSize="0" + hexSize;
		
		String padded_msg = (n+ (char)(0x80));
		while(padded_msg.length()%64 != 56)
			padded_msg+=(char)0x00;
		
		if(debug) System.out.println("  Padded Message= '"+padded_msg + "'");
		if(debug) System.out.println("  Length="+padded_msg.length());

		///		System.out.println(size);
		int num = 1 + padded_msg.length()/64; 
		long splitted[][] = new long[num][80];
		
		if(debug) System.out.println("  Number of blocks="+num);

		/* For each block */
		for (int m = 0; m < num; m++) {
			if(debug) System.out.println("  Block "+ m + " contains:");

			for (int i = 0; i < 16; i++) {
				String hexMessage = "";
				/* Append the length at the end */
				if ((m == num - 1) && (i == 14)) {
					splitted[m][i] = Long.parseLong(hexSize.substring(0, 8),16);
					if(debug) System.out.println("   ["+ i+ "]"+ splitted[m][i]);
					i++;
					splitted[m][i] = Long.parseLong(hexSize.substring(8, 16),16);
					if(debug) System.out.println("   ["+ i+ "]"+ splitted[m][i]);
				} else {
					for (char c : padded_msg.substring(m*64+i*4, m*64+i*4 + 4).toCharArray()) {
						String c_hex = Integer.toHexString(c);
						while (c_hex.length() < 2)
							c_hex = "0" + c_hex;
						hexMessage = hexMessage + c_hex;
//						System.out.println((int)c);
					}
					splitted[m][i] = Long.parseLong(hexMessage, 16);
					if(debug) System.out.println("   ["+ i+ "]"+ splitted[m][i]);
				}
			}

			//Extend each block according to the RFC standard
			for (int i = 16; i < 80; i++) {
				long t_1_2 = xor(splitted[m][i - 3], splitted[m][i - 8]);
				long t_3_4 = xor(splitted[m][i - 14], splitted[m][i - 16]);
				splitted[m][i] = xor(t_1_2,t_3_4);
				splitted[m][i] = ((splitted[m][i] << (32 + 1) >>> 32) | (splitted[m][i] >>> (32 - 1)));
				if(debug) System.out.println("   ["+ i+ "]"+ splitted[m][i]);
			}
		}
		if(debug) 	System.out.println("  Length after padding=" + num*512 + " bits [should be a multiple of 512]");
		return splitted;
	}

	public MySHA1(String n) {
		this.n = n;	
		if(debug) System.out.print("String to be hashed: '"+n + "'\n\n");
	}

		
	public static void main(String args[])  {
		String s = "";
		if(args.length < 1){
			runTests();
			System.out.println("Usage: java org.unl.cryptoanalysis.hw3.Prob2 <fileName> \n\t where <fileName> = file con"
					+ "taining string to be hashed");
			System.out.println("Usage: java SHA1 <s> \n\t s = string to be hashed");
		}
		else {
			s = args[0];
			if(args.length >= 2) debug = true;

			if(debug) System.out.println("For terminologies, please refer to the RFC "
					+ "at: https://tools.ietf.org/html/rfc3174\n");
			System.out.println("\nOUTPUT:\n SHA1(\""+s+"\")=\n\t"+new MySHA1(s).getDigest());
//			System.out.println("\n Verifying using API="+ getSHA1HashUsingAPI(s));
		}

	}
	 public static void runTests() {
		// Standard Tests
		String[] tests = {"","a","abc","message digest","abcdefghijklmnopqrstuvwxyz",
						"The quick brown fox jumps over the lazy dog",
						"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
						"12345678901234567890123456789012345678901234567890123456789012345678901234567890",
						"123456789012345678901234567890123456789012345678901234567890123456789012345678"
						+ "9012345678901234567890123456789012345678901234567890123456789012345678901234567890"
						+ "12345678901234567890123456789012345678901234567890123456789012345678901234567890",
						"123456789012345678901234567890123456789012345678901234567890123456789012345678"
						+ "9012345678901234567890123456789012345678901234567890123456789012345678901234567890"
						+ "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
						+ "123456789012345678901234567890123456789012345678901234567890123456789012345678"
						+ "9012345678901234567890123456789012345678901234567890123456789012345678901234567890"
						+ "12345678901234567890123456789012345678901234567890123456789012345678901234567890"};
					

		System.out.println("==============Running basic tests==============");
		for(int i=0;i<tests.length;i++) {
					String hash = new MySHA1(tests[i]).getDigest();
					String api_hash = getSHA1HashUsingAPI(tests[i]);
					if(api_hash.equalsIgnoreCase(hash))
						System.out.println("*PASSED* ["+ 
							" Length= "+ tests[i].length() + " chars, s=\""+tests[i]+"\"]\n\tSHA1(s)=" +hash);
					else
						System.out.println("*FAILED* ="+  
							" Length= "+ tests[i].length() + " chars, s=\""+tests[i]+"\"]\n\tSHA1(s)=" +hash
							+ "Actual="+api_hash);
				}
		System.out.println("==============Basic Tests Completed==============");

	}

	
	/* Uses Java API to get the SHA1 Hash */
	public static String getSHA1HashUsingAPI(String message) {

		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m.reset();
		m.update(message.getBytes());
		String hash = new BigInteger(1,m.digest()).toString(16);
		while(hash.length() < 40 )
		  hash = "0"+hash;
		return hash;
	}


}
