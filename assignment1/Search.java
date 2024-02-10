package assignment1;
//ReadMe file has the instructions to run the program
import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileInputStream;
import java.nio.file.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.tartarus.snowball.ext.englishStemmer;

public class Search {
	    static BitSet[] bt_search;
	    static BitSet[] bt_t;
	    static HashMap<String,Integer> map;
	    static int[] hashSeeds;
	    static ArrayList<String> file_names;
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		System.out.print("Enter the program and parameters ");
		Scanner sc= new Scanner(System.in);	
		//reading inputs from the given path
		String[] inputs = sc.nextLine().split("\\s");
		sc.close();
		if(inputs[0].equals("doc_search")) {
			//reading the search file line by line
			String search_file = inputs[1];
			List<String> search_lines = Files.readAllLines(Paths.get(search_file));
			//reading the transposed bllom filter saved index
			ObjectInputStream in_1 = new ObjectInputStream(new FileInputStream("bt_t.ser"));
			bt_t = (BitSet[]) in_1.readObject();
			in_1.close();
			//reading the mapping of words and number of  hash functions
			ObjectInputStream in_2 = new ObjectInputStream(new FileInputStream("map.ser"));
			map = (HashMap<String,Integer>) in_2.readObject();
			in_2.close();
			//reading the hash seeds used
			ObjectInputStream in_3 = new ObjectInputStream(new FileInputStream("hashSeeds.ser"));
			hashSeeds = (int[]) in_3.readObject();
			in_3.close();
			//reading the file names and their index 
			ObjectInputStream in_4 = new ObjectInputStream(new FileInputStream("file_names.ser"));
			file_names = (ArrayList<String>) in_4.readObject();
			in_4.close();
			Doc_Names(search_lines);
		}
		else {
			System.out.println("enter inputs correctly");
		}		
	}

	public static  void Doc_Names(List<String> search_lines) throws IOException {
		BitSet[] bt_search;
		bt_search = new BitSet[search_lines.size()];
		//passing each line for bloom filter of search terms
		for (int i=0;i<search_lines.size();i++) {
			bt_search[i]= Listfiles(search_lines.get(i));
			intersection(bt_search[i]);
		}		
	}

	public static void intersection(BitSet bf) {
		//if bloom filter is empty, no matching files are found else intersection
		//of all documents containing search terms are returned
		if (bf.isEmpty()) {System.out.println("No matching files");}
		else {
		  BitSet index = (BitSet) bt_t[bf.nextSetBit(0)].clone();
		  for (int i = bf.nextSetBit(0); i >= 0; i = bf.nextSetBit(i+1)) {
			index.and((bt_t[i]));
		    if (i == Integer.MAX_VALUE) {
		    break; 
		    }
		  }
			if(index.isEmpty()) {System.out.println("No matching files");}
			else {
			ArrayList<String> doc_names = new ArrayList<String>();
			for (int i = index.nextSetBit(0); i >= 0; i = index.nextSetBit(i+1)){
				doc_names.add(file_names.get(i));
				if (i == Integer.MAX_VALUE) {
				break; 
				}
			}
			System.out.println(String.join(",", doc_names));}}
	}

	public static BitSet Listfiles(String search) {
		//each line of search file is trimmed,split and bloom filter is prepared
		//for all search terms in the corpus 
		BitSet bs = new BitSet(bt_t.length);
		englishStemmer stemmer = new englishStemmer();
		String[] trimmed = search.split("\\s+");
		for (int i=0 ;i<trimmed.length;i++) {
			stemmer.setCurrent(trimmed[i]);
			if (stemmer.stem()){
				if (map.containsKey(stemmer.getCurrent())) {
				int p = map.get(stemmer.getCurrent());
				bs.or(hash(stemmer.getCurrent(),p,Arrays.copyOfRange(hashSeeds, 0, p), bt_t.length));}}
		} 
		return bs;
	}
	//hash function that takes bytes as input and return the hash 
	public static long hash64( final byte[] data, int length, int seed) {
		
		final int m = 0x5bd1e995;
		final int r = 24;
		
		int h = seed^length;
		int length4 = length/4;

		for (int i=0; i<length4; i++) {
			final int i4 = i*4;
			int k = (data[i4+0]&0xff) +((data[i4+1]&0xff)<<8)
					+((data[i4+2]&0xff)<<16) +((data[i4+3]&0xff)<<24);
			k *= m;
			k ^= k >>> r;
			k *= m;
			h *= m;
			h ^= k;
		}
		
		switch (length%4) {
		case 3: h ^= (data[(length&~3) +2]&0xff) << 16;
		case 2: h ^= (data[(length&~3) +1]&0xff) << 8;
		case 1: h ^= (data[length&~3]&0xff);
				h *= m;
		}

		h ^= h >>> 13;
		h *= m;
		h ^= h >>> 15;

		return h;
	}
	//hash functions that takes strings and pass the bytes for hashing
	public static BitSet hash( final String text, final int p ,int[] hash_bf,double m_bf) {
		BitSet bs ;
		bs = new BitSet ((int)m_bf);		
		final byte[] bytes = text.getBytes();
		for (int i =0;i<p;i++) {
			long hashed = hash64( bytes, bytes.length, hash_bf[i]);
			bs.set((int) (Math.abs(hashed)%(m_bf)), true);			
		}
		return bs;
	}
}
