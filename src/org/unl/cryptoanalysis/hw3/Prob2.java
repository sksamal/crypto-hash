package org.unl.cryptoanalysis.hw3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.kocakosm.pitaya.security.Algorithm;
import org.kocakosm.pitaya.security.Digest;
import org.kocakosm.pitaya.security.Digests;
import org.kocakosm.pitaya.security.KDF;
import org.kocakosm.pitaya.security.KDFs;
import org.unl.cryptoanalysis.tools.HashComparator;

/* Problem 2 */
/* Generate Rainbow tables using tool at https://github.com/kocakosm/pitaya (lib/pitaya.jar)*/

public class Prob2 {
	
	
	public static void main(String args[]) {

		/* Dictionary and the output rainbow hash files */
		String fileName = "/usr/share/dict/american";
		String md4File = "data/md4.txt";
		String md5File = "data/md5.txt";
		String sha1File = "data/sha1.txt";
		String sha256File = "data/sha256.txt";
		String sha3File = "data/sha3.txt";
		String pbkdf2File = "data/pbkdf2.txt";

		/* Call appropriate method with parameters */
		writeRainbowTab(fileName,md4File,"MD4",32);
		writeRainbowTab(fileName,md5File,"MD5",32);
		writeRainbowTab(fileName,sha1File,"SHA-1",40);
		writeRainbowTab(fileName,sha256File,"SHA-256",64);
		writeRainbowTab(fileName,sha3File,"SHA-3",64);
		
		/* For PBKDF2, we use salt = shoop, iterations = 1000, output length = 20 bytes (40 hex chars) 
		 * Algorithm = HMAC_SHA1 */
		writeRainbowTabPBKDF(fileName,pbkdf2File,"PBKDF2","shoop",1000,40);

	}

	/* Returns an appropriate digest instance from the pitaya library */
	public static Digest getInstance(String instance) {
		
		switch (instance) {
			case "MD4":
				return Digests.md4();
			case "MD5":
				return Digests.md5();
			case "SHA-1":
				return Digests.sha1();
			case "SHA-256":
				return Digests.sha256();
			case "SHA-3":
				return Digests.keccak256();
				
		}
		
		return null;
	}
	
	
	/* Method that reads dictionary and calls the appropriate instance type from the library
	 * generates the hash, sorts them and then writes them to the file
	 * used for MD4, MD5, SHA-1, SHA-256, and SHA-3(Keccak-256)
	 */
	public static void writeRainbowTab(String fileName,String outFile,String instance, int size) {
		
		System.out.println("\nGenerating rainbow tables for : " + instance + "\n");
		try {
			Scanner s = new Scanner(new File(fileName));
			PrintWriter pw = new PrintWriter(new File(outFile));
			Digest m = getInstance(instance);
			
			int i=0;
			List<String> hList = new ArrayList<String>();
			long t1 = System.currentTimeMillis();


			/* Read a line from dictionary, call the hash function then add to hList */
			while(s.hasNextLine()) {
			String message = s.nextLine();
			m.reset();
			m.update(message.getBytes());
			String hash = new BigInteger(1,m.digest()).toString(16);
			while(hash.length() < size)
				hash="0"+hash;
			hList.add(message+":"+ hash);
			
			if(++i%30000==0)
				System.out.print(i + " records processed...  ");
			if(i%100000 == 0) System.out.println();
			}
			s.close();
		
			/* Sort and write to output file */
			String[] hArray = hList.toArray(new String[0]);
			Arrays.sort(hArray, new HashComparator());
			for(String hash: hArray)
				pw.println(hash);
			pw.close();
			long t2 = System.currentTimeMillis();
			System.out.println(i + " records written to " + outFile + 
					" in " + (t2-t1)/1000 + " seconds.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/* Method that reads dictionary and calls the PBKDF2 instance from the library
	 * generates the hash, sorts them and then writes them to the file
	 * used for PBKDF2
	 */
	public static void writeRainbowTabPBKDF(String fileName,String outFile,String instance, 
			String salt, int iterations, int size) {
		
		System.out.println("\nGenerating rainbow table for: " + instance);
		System.out.println(" \t(This may take a long time..plz be patient)");
		System.out.println("instance = "+ instance + " HMAC_SHA-1, salt= " + salt
				+ ", iterations=" + iterations + ", outputsize=" + size);

		try {
			Scanner s = new Scanner(new File(fileName));
			PrintWriter pw = new PrintWriter(new File(outFile));
			
			/* See if a temp processed file exists, if yes reuse it rather
			 * than starting from scratch
			 */
			Scanner stemp = null; 
			int count = 0;
			String tempFile = outFile+".temp";
			File fp = new File(tempFile);
			if(fp.exists() && fp.length() > 0)  {
				stemp = new Scanner(fp);
				while(stemp.hasNextLine())
					{ count++; stemp.nextLine(); };
				stemp.close();
			}

			PrintWriter pwtemp = null;
			if(count > 0)
			    pwtemp = new PrintWriter(new FileWriter(tempFile,true));
			else
				pwtemp = new PrintWriter(tempFile);
			
			int i = 0;
			while (i < count) {
				if(s.hasNextLine()) s.nextLine();
				i++;
			}

			if(count > 0)
				System.out.println("A temporary file already exists, starting at recordNumber=" + i);

			
			/* Hash, sort and output to the out file */
			KDF pbkdf2 = KDFs.pbkdf2(Algorithm.HMAC_SHA1, iterations, size/2);
			List<String> hList = new ArrayList<String>();
			
			long t1 = System.currentTimeMillis();
			while(s.hasNextLine()) {
			String message = s.nextLine();
			byte[] digest = pbkdf2.deriveKey((message).getBytes(),(salt).getBytes());
			String hash = new BigInteger(1,digest).toString(16);
			
			if(++i%10000==0) {
				System.out.print(i + " records processed....  ");
				if(i%30000 == 0) System.out.println();
				pwtemp.flush(); }
			pwtemp.println(message+":" + hash);
			}
			s.close();
			pwtemp.close();

			System.out.println(i + " records processed");		
			/* Read from temporary file and sort */
			stemp = new Scanner(new File(tempFile));
			while(stemp.hasNextLine())
				hList.add(stemp.nextLine());
			stemp.close();
			Files.delete(Paths.get(tempFile));
		
			/* Finally write the output*/
			String[] hArray = hList.toArray(new String[0]);
			Arrays.sort(hArray, new HashComparator());
			for(String hash: hArray)
				pw.println(hash);
			pw.close();
			long t2 = System.currentTimeMillis();
			System.out.println(i + " records processed to " + outFile + 
					" in " + (t2-t1)/1000 + " seconds.");

		} catch (FileNotFoundException  e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	}
