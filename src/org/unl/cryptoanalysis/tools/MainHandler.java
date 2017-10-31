package org.unl.cryptoanalysis.tools;

import org.unl.cryptoanalysis.hw3.Prob1;
import org.unl.cryptoanalysis.hw3.Prob2;
import org.unl.cryptoanalysis.hw3.Prob3;

/* Provides a menu for all the problems in the assignment
 * 
 */
public class MainHandler {

		public static void main(String args[])
		{
			try{
			if(args.length<1) 
				usage();
			else
			{
				switch(Integer.parseInt(args[0])) {
				case 1:
					System.out.println("##################################");
					System.out.println("Problem 1 - MD5 and SHA-1 Implementation");
					System.out.println("##################################");
					String inp[] = { args[1], args[2] };
					Prob1.main(inp);
					break;
				case 2:
					System.out.println("##################################");
					System.out.println("Problem 2 - Generate Rainbow Tables");
					System.out.println("(MD4,MD5,SHA-1,SHA-256 SHA-3(Keekak 256),PBKDF2");
					System.out.println("##################################");
					Prob2.main(null);
					break;
				case 3:
					System.out.println("##################################");
					System.out.println("Problem 3 - Break Hashes");
					System.out.println("(MD4,MD5,SHA-1,SHA-256 SHA-3(Keekak 256),PBKDF2");
					System.out.println("##################################");
					Prob3.main(null);
					break;
				default:
					usage();
				}
			}
			}
			catch (Exception e) {
				e.printStackTrace();
				usage();
			}
		}
		
		public static void usage() {
			System.out.println("Usage:");
			System.out.println("	java -jar crypto.jar <i> [fileName] [algorithm]");
			System.out.println(" where i = \n	1. MD5 and SHA-1 Implementation (Problem 1)");
			System.out.println("Example:\n	java crypto.jar 1 inp.txt md5 \n OR");
			System.out.println("	java crypto.jar 1 inp.txt sha1");
			System.out.println("\n    2. Generate Rainbow Tables (Problem 2)");
			System.out.println("Example:\n	java crypto.jar 2");
			System.out.println("\n    3. Break Hashes (Problem 3)");
			System.out.println("Example:\n	java crypto.jar 3");

			System.exit(1);
		}
}
