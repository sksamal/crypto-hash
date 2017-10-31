/* My MD5 Implementation */
package org.unl.cryptoanalysis.tools;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MyMD5 {

	private static boolean debug = false;
	private String n;
	private long[] T = new long[64]; 
	private long[] s = new long[64];
	long two_pow_32 = (long)Math.abs(Math.pow(2, 32));
	
	/* Logical functions */
	private long not(long x) {
		return (two_pow_32-x-1);
	}
	
	private long xor(long x, long y) {
		return (x&not(y))|(not(x)&y);
	}

	// Functions F,G,H,I as defined in RFC
	private long F(long x, long y, long z) {
		return ((x&y)|(not(x)&z));
	}
	
	private long G(long x, long y, long z) {
		return ((x&z)|(y&not(z)));
	}
	
	private long H(long x, long y, long z) {
		long x_xor_y = xor(x,y);
		return 	xor(x_xor_y,z);
	}
	
	private long I(long x, long y, long z) {
		long x_v_not_z = (x|not(z));
		return xor(y,x_v_not_z);
	}

	/* Performs a single operation on appropriate part of a chunk */
	private long operation(long[] chunks, int round, long a,  long b, long c, long d, long s, int i ) {
		int k = 0;
		switch (round) {
		case 1:
			a = a + F(b,c,d);
			k = i%16;
			break;
		case 2:
			a = a + G(b,c,d);
			k = (5*i + 1)%16;
			break;
		case 3:
			a = a + H(b,c,d);
			k = (3*i + 5)%16;
			break;
		case 4:
			a = a + I(b,c,d);
			k = (7*i)%16;
			break;
		}
//				System.out.print("[k="+k+" s=" + s+ " i="+(i+1)+"]  ");
//		System.out.println("len=" + chunk.length() + " k=" + k);
		long Xk = chunks[k];
//		System.out.print("Xk="+Xk);
//		String subs = chunk.substring(k*8,k*8+8);
//		long Xk = Long.parseLong(subs,16);
//		System.out.println("a="+a+" Xk=" + Xk+ " T[i]=" + T[i]);
		a = (a + Xk + T[i])% two_pow_32;
//		System.out.println("a="+a + " s=" + s);
//		System.out.println("a=" + Long.toBinaryString(a));
		
		/*Rotate a */
		a = (((a<<(32+s))>>>32)|(a>>>(32-s)));
//		System.out.println("(a<<(32+s)>>>32)|t=" + Long.toBinaryString(a));
//		System.out.println("a="+a);
		a = (b + a)% two_pow_32;
//		System.out.println("a="+a);
		return a;

	}
	
	public String getN() {
		return n;
	}

	public void setN(String n) {
		this.n = n;
	}
	
	/* Real implementation that divides into chunks and 
	 * calls the operation method
	 */
	public String getDigest() {
		
		long A = 0x67452301L;
		long B = 0xefcdab89L;
		long C = 0x98badcfeL;
		long D = 0x10325476L;

		if(debug) System.out.println("\n\nMDBUFFER:\n\n words A="+A+", B="+B+", C="+C+", D="+D);

		// Pad 1 and length at the end and break it into chunks
		long chunks[][] = padMessage();
		int num = chunks.length;

		if(debug) System.out.println("\nOPERATIONS:");

		// Repeat the process for each block in the message
		for(int m=0; m < num; m++) {
			if(debug) System.out.println("\n[Block="+m+"]");
			long AA = A, BB = B, CC= C, DD = D;
			for(int i=0;i<64;) {
				A = operation(chunks[m],(i/16)+1,A,B,C,D,s[i],i);
				if(debug) System.out.println("  [i = "+i + "] A="+A+", B="+B+", C="+C+", D="+D);
				i++;
				D = operation(chunks[m],(i/16)+1,D,A,B,C,s[i],i);
				if(debug) System.out.println("  [i = "+i + "] A="+A+", B="+B+", C="+C+", D="+D);
				i++;
				C = operation(chunks[m],(i/16)+1,C,D,A,B,s[i],i);
				if(debug) System.out.println("  [i = "+i + "] A="+A+", B="+B+", C="+C+", D="+D);
				i++;
				B = operation(chunks[m],(i/16)+1,B,C,D,A,s[i],i);
				if(debug) System.out.println("  [i = "+i + "] A="+A+", B="+B+", C="+C+", D="+D);
				i++;
				}
		A = (A + AA)%(long)two_pow_32;
		B = (B + BB)%(long)two_pow_32;
		C = (C + CC)%(long)two_pow_32;
		D = (D + DD)%(long)two_pow_32;
		if(debug) System.out.println("Block="+m+" Processed: A="+A + " B="+B + " C="+C + " D="+D);
		}
				String A_Hex = Long.toString(A,16);
				String B_Hex = Long.toString(B,16);
				String C_Hex = Long.toString(C,16);
				String D_Hex = Long.toString(D,16);
				

				/* Final padding and appending */
				while(A_Hex.length()!=8) A_Hex = "0"+A_Hex;
				while(B_Hex.length()!=8) B_Hex = "0"+B_Hex;
				while(C_Hex.length()!=8) C_Hex = "0"+C_Hex;
				while(D_Hex.length()!=8) D_Hex = "0"+D_Hex;

				if(debug) System.out.println("\nFINAL VALUES:\n  A_Hex="+A_Hex + " B_Hex="+B_Hex + " C_Hex="+C_Hex + " D_Hex="+D_Hex);

				A_Hex = A_Hex.substring(6,8)+ A_Hex.substring(4,6) + 
							A_Hex.substring(2,4) + A_Hex.substring(0,2);
				B_Hex = B_Hex.substring(6,8)+ B_Hex.substring(4,6) + 
							B_Hex.substring(2,4) + B_Hex.substring(0,2);
				C_Hex = C_Hex.substring(6,8)+ C_Hex.substring(4,6) + 
							C_Hex.substring(2,4) + C_Hex.substring(0,2);
				D_Hex = D_Hex.substring(6,8)+ D_Hex.substring(4,6) + 
							D_Hex.substring(2,4) + D_Hex.substring(0,2);

				return A_Hex+B_Hex+C_Hex+D_Hex;
			}
	
	
	/* Pads length, zeros and breaks it into chunks */
	private long[][] padMessage() {
		
		if(debug) System.out.println("\nAPPEND PADDING BITS AND LENGTH:\n");
		String hexSize = Long.toHexString(n.length()*8);
		while(hexSize.length()<16)
			hexSize="0"+hexSize;
		
//		System.out.println(hexSize);
		
		String padded_msg = (n+ (char)(0x80));
		while(padded_msg.length()%64 != 56)
			padded_msg+=(char)0x00;
		
		if(debug) System.out.println("  Padded Message= '"+padded_msg + "'");
		if(debug) System.out.println("  Length="+padded_msg.length());
		
		//System.out.println(padded_msg.length());
		int num = padded_msg.length()/64 + 1; 
		long splitted[][] = new long[num][16];
		
		if(debug) System.out.println("  Number of blocks="+num);
				
		for(int m=0;m<num;m++) {
			if(debug) System.out.println("  Block "+ m + " contains:");
			
			for(int i=0;i<16;i++) {
				if ((m == num - 1) && (i == 14)) {
					splitted[m][i] = Long.parseLong(hexSize.substring(8, 16),16);
					if(debug) System.out.println("   ["+ i+ "]"+ splitted[m][i]);
					i++;
					splitted[m][i] = Long.parseLong(hexSize.substring(0, 8),16);
					if(debug) System.out.println("   ["+ i+ "]"+ splitted[m][i]);
				} else {
	
				String hexMessage = "";
				for(char c : padded_msg.substring(m*64+i*4,m*64+i*4+4).toCharArray()) {
					String c_hex = Integer.toHexString(c);
					while(c_hex.length()<2)
						c_hex="0" + c_hex;						
					hexMessage=c_hex+hexMessage;
	//				System.out.println(c + " " + hexMessage);
		}
			splitted[m][i] = Long.parseLong(hexMessage,16);
			if(debug) System.out.println("   ["+ i+ "]"+ splitted[m][i]);
//			System.out.println("["+ i+ "]"+ Long.toBinaryString(splitted[i]));
//			System.out.println("["+ i+ "]"+ Long.toBinaryString(1819043144));
				}
		}
		}
		
		if(debug) 	System.out.println("  Length after padding=" + num*512 + " bits [should be a multiple of 512]");
		return splitted;
	}

	public MyMD5(String n) {
		this.n = n;

		if(debug) System.out.print("String to be hashed: '"+n + "'\n\n64-ELEMENT TABLE 'T' (from"
				+ "sine function):\n\n  ");
		// Initialize T and s
		for(int i=0;i<T.length;i++){
			T[i] = (long)(Math.abs(Math.sin(i+1)*two_pow_32));
			if(debug) {
				System.out.print(String.format("%-20s", "T["+i+"]="+T[i]+""));
				if((i+1)%8==0) System.out.print("\n  ");
			}
			
		}
				
		if(debug) System.out.print("\nOPERATION TABLE 's':\n\n  ");
		int j = 0;
		int[][] a = {{7,12,17,22}, {5,9,14,20}, {4,11,16,23},{6,10,15,21}};
		for(int i=0;i<64;i=i+16){
			s[i]=s[i+4]=s[i+8]=s[i+12] = a[j][0];
			s[i+1]=s[i+5]=s[i+9]=s[i+13] = a[j][1];
			s[i+2]=s[i+6]=s[i+10]=s[i+14] = a[j][2];
			s[i+3]=s[i+7]=s[i+11]=s[i+15] = a[j][3];
			j++;
		}
		
	   for(int i=0;i<64;i++)
			if(debug) {
				System.out.print(String.format("%-20s","s["+i+"]="+s[i]));
				if((i+1)%8==0) System.out.print("\n  ");
			}
				
	}

		
	public static void main(String args[])   {
		String s = "";
		if(args.length < 1){
			runTests();
			System.out.println("Usage: java org.unl.cryptoanalysis.hw3.Prob1 <fileName> \n\t where <fileName> = file con"
					+ "taining string to be hashed");
			System.out.println("Usage: java MyMD5 <s> \n\t s = string to be hashed");
		}
		else {
			s = args[0];
			if(args.length >= 2) debug = true;
		
			if(debug) System.out.println("For terminologies, please refer to the RFC "
					+ "at: https://www.ietf.org/rfc/rfc1321.txt\n");
		System.out.println("\nOUTPUT:\n  MD5(\""+s+"\")=\n\t"+new MyMD5(s).getDigest());
//		System.out.println("  Verifying using API=" + getMD5HashUsingAPI(s));
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
				+ "12345678901234567890123456789012345678901234567890123456789012345678901234567890",						"123456789012345678901234567890123456789012345678901234567890123456789012345678"
				+ "9012345678901234567890123456789012345678901234567890123456789012345678901234567890"
				+ "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
				+ "123456789012345678901234567890123456789012345678901234567890123456789012345678"
				+ "9012345678901234567890123456789012345678901234567890123456789012345678901234567890"
				+ "12345678901234567890123456789012345678901234567890123456789012345678901234567890"};

		
		System.out.println("==============Running basic tests==============");
		for(int i=0;i<tests.length;i++) {
			String hash = new MyMD5(tests[i]).getDigest();
			String api_hash = getMD5HashUsingAPI(tests[i]);
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

	/* Uses Java API to get the MD5 Hash */
	public static String getMD5HashUsingAPI(String message) {

		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		m.reset();
		m.update(message.getBytes());
		String hash = new BigInteger(1,m.digest()).toString(16);
		while(hash.length() < 32 )
		  hash = "0"+hash;
		return hash;
	}
}
