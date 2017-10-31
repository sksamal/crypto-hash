package org.unl.cryptoanalysis.hw3;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.unl.cryptoanalysis.tools.MyMD5;
import org.unl.cryptoanalysis.tools.MySHA1;

public class Prob1 {
	
	
	public static void main(String args[]) {
		
		if(args.length < 2){
			usage();
			System.exit(1);
		}
		
		try {
			Scanner s = new Scanner(new File(args[0]));
			String string = "";
			while(s.hasNextLine())
				string+=s.nextLine();
			s.close();
			if(args[1].contains("md5"))
				System.out.println("MD5Hash = "+ new MyMD5(string).getDigest());
			else if(args[1].contains("sha1"))
				System.out.println("SHA-1 Hash = "+ new MySHA1(string).getDigest());
			else
				usage();
		}
	
		catch(IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void usage() {
		System.out.println("Usage: java Prob1 <fileName> <algorithm>");
		System.out.println("   where alorithm is either md5 or sha1");
	}

}
