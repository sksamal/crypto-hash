package org.unl.cryptoanalysis.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/* Class to search within the rainbow table files */
public class SearchTables {

	String fileName;
	int count = 0;
	int broken = 0;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getBroken() {
		return broken;
	}

	public void setBroken(int broken) {
		this.broken = broken;
	}

	public SearchTables(String fileName) {
		this.fileName = fileName;
	}

	/* Perform search */
	public String[] search(String searchFile) {

		try {

			ArrayList<String> values = new ArrayList<String>();
			Scanner s = new Scanner(new File(searchFile));
			while (s.hasNextLine())
				values.add(s.nextLine().trim());
			s.close();
			ArrayList<String> results = new ArrayList<String>();
			String valueList[] = values.toArray(new String[0]);
			
			/* Sort according to hash, so that its easier to search in the rainbow tables */
			Arrays.sort(valueList, new HashComparator());
			
			
			// open the rainbow table file for reading and proceed in order in both files (note:
			// both are sorted
			broken = 0; 
			BufferedReader bf = new BufferedReader(new FileReader(fileName));
			String line = bf.readLine();
			for (String value : valueList) {
				String username = value.split(":")[0];
				String hashedPassword = value.split(":")[1];
				this.count++;
				while (line!= null) {
					String tokens[] = line.split(":");
					int comparison = tokens[1].trim().compareTo(hashedPassword.trim());
					if (comparison == 0) {
						results.add(username + ":" + tokens[0]);
						broken++;
						break;
					} else if (comparison > 0) {   // current comparison > 0, means no need to search further
						results.add(username + ":NOT FOUND");
						break;
					} else
						line = bf.readLine();
				}
			}
			bf.close();
			return results.toArray(new String[0]);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
