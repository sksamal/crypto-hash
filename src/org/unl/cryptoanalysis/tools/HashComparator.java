package org.unl.cryptoanalysis.tools;

import java.math.BigInteger;
import java.util.Comparator;

public class HashComparator implements Comparator<String> {

	@Override
	public int compare(String str1, String str2) {
		if (str1== null) {
			if (str2 == null)
				return 0;
			else
				return -1;
		} else {
			if (str2 == null) {
				return 1;
			}
		}

		return str1.split(":")[1].compareTo(str2.split(":")[1]);
		}
	}
