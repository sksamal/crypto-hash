package org.unl.cryptoanalysis.hw3;

import java.io.File;
import java.util.Arrays;

import org.unl.cryptoanalysis.tools.SearchTables;


public class Prob3 {
	
	final static int LINE_SIZE = 95;
	static String passwdmd4File = "data/passwd.md4";
	static String passwdmd5File = "data/passwd.md5";
	static String passwdsha1File = "data/passwd.sha1";
	static String passwdsha256File = "data/passwd.sha256";
	static String passwdsha3File = "data/passwd.sha3";
	static String passwdpbkdf2File = "data/passwd.pbkdf2";

	static String fileName;
	static String md4File;
	static String md5File;
	static String sha1File;
	static String sha256File;
	static String sha3File;
	static String pbkdf2File ;
	static String fResult[][];

	public static void main(String args[]) {
		
		/* Use Existing rainbow tables first */
		fileName = "/usr/share/dict/american";
		md4File = "data/md4.txt";
		md5File = "data/md5.txt";
		sha1File = "data/sha1.txt";
		sha256File = "data/sha256.txt";
		sha3File = "data/sha3.txt";
		pbkdf2File = "data/pbkdf2-cse477.txt";
		crackUsingRainbowTables();

		/* Rainbow Table results */
		System.out.println("=========RainbowTable Results==========");
		System.out.println(String.format("%-15s %-10s %-10s %-10s %-10s","Type","Total","Cracked","Sucess(%)","Time(ms)"));
		for(String[] res : fResult)
			System.out.println(String.format("%-15s %-10s %-10s %-10s %-10s",res[0],res[1],res[2],res[3],res[4]));

		
		/* Extra tables obtained from external resources (internet) */
		/* Use extra Rainbow tables to crack */
		fileName = "data/extradict";
		md4File = "data/md4extra.txt";
		md5File = "data/md5extra.txt";
		sha1File = "data/sha1extra.txt";
		sha256File = "data/sha256extra.txt";
		sha3File = "data/sha3extra.txt";
		pbkdf2File = "data/pbkdf2-cse477extra.txt";
		
		crackUsingRainbowTables();

		/* Print final results */
		System.out.println("=========Final Results==========");
		System.out.println(String.format("%-15s %-10s %-10s %-10s ","Type","Total","Cracked","Sucess(%)"));
		for(String[] res : fResult)
			System.out.println(String.format("%-15s %-10s %-10s %-10s ",res[0],res[1],res[2],res[3]));
		
		
	}
	
	 public static void crackUsingRainbowTables() {

			String[][] files = {	{ "MD4", md4File, passwdmd4File },
					{ "MD5", md5File, passwdmd5File },
					{ "SHA-1", sha1File, passwdsha1File },
					{ "SHA-256", sha256File, passwdsha256File },
					{ "SHA-3", sha3File, passwdsha3File },
					{ "PBKDF2HMACSHA1",pbkdf2File,passwdpbkdf2File }};

			fResult = new String[files.length][5];
			
			/* If rainbow tables are not present, write them */
			if(!new File(md4File).exists() || new File(md4File).length() == 0) 
				Prob2.writeRainbowTab(fileName,md4File,"MD4",32);
			if(!new File(md5File).exists() || new File(md5File).length() == 0) 
				Prob2.writeRainbowTab(fileName,md5File,"MD5",32);
			if(!new File(sha1File).exists() || new File(sha1File).length() == 0) 
				Prob2.writeRainbowTab(fileName,sha1File,"SHA-1",40);
			if(!new File(sha256File).exists() || new File(sha256File).length() == 0) 
				Prob2.writeRainbowTab(fileName,sha256File,"SHA-256",64);
			if(!new File(sha3File).exists() || new File(sha3File).length() == 0) 
				Prob2.writeRainbowTab(fileName,sha3File,"SHA-3",64);
			if(!new File(pbkdf2File).exists() || new File(pbkdf2File).length() == 0) 
				Prob2.writeRainbowTabPBKDF(fileName,pbkdf2File,"PBKDF2","cse477",10000,40);
		 
		System.out.println("\n[Using rainbow tables created from ]......");
		System.out.println("Dictionary: " + fileName);
	
		System.out.println(String.format("%-15s %-20s %-20s","Type", "Passwords file","Rainbow Table"));
		for (String[] info : files) 
			System.out.println(String.format("%-15s %-20s %-20s",info[0], info[2],info[1]));
	

//		long tmd4, tmd5, tsha1, tsha256, tsha3, tpbkdf2;
		long t1, t2;
		
		int i = 0;

		for(String[] info : files) {
		
		System.out.println("\n==" + info[0] + " Results (sorted by username) ==");
		t1 = System.currentTimeMillis();
		SearchTables st = new SearchTables(info[1]);
		String results[] = st.search(info[2]);
		t2 = System.currentTimeMillis() - t1;
		Arrays.sort(results);
		for(String result : results)
			System.out.println(result);
		System.out.println(st.getBroken() + " out of " + st.getCount() + " cracked.(" + st.getBroken()*100/(double)st.getCount() + "% success)");
		System.out.println("in " + t2 + " msecs");

		fResult[i][0] = info[0];
		fResult[i][1] = st.getCount() + "";
		fResult[i][2] = st.getBroken() + "";
		fResult[i][3] = st.getBroken()*100/(double)st.getCount() + "";
		fResult[i][4] = t2 + "";
		i++;
		}
	
	 }
	
}
