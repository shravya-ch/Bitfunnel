package assignment1;

//ReadMe file has the instructions to run the program

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.stream.*;
import org.tartarus.snowball.ext.englishStemmer;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class Doc_Index {
		
	public static void main(String[] args) throws IOException {
		System.out.print("Enter the program and parameters ");
		Scanner sc= new Scanner(System.in);		
		String[] inputs = sc.nextLine().split("\\s");
		sc.close();
		
		if (inputs[0].equals("doc_index")) {
			//reading the given inputs
			File folder = new File(inputs[1]);
			double d =  Double.parseDouble(inputs[2]);
			double snr =  Double.parseDouble(inputs[3]);
			double m_given;
			if (inputs.length==4) {m_given = 0.0;}
			else{ m_given = Double.parseDouble(inputs[4]);}
			listFilesForFolder(folder,d,snr,m_given);}
		else {System.out.println("enter inputs correctly");}}	

	public static void listFilesForFolder(File folder,double d,double snr,double m_bf) throws IOException {
		//storing file names 
		ArrayList<String> file_names ;
		file_names = new ArrayList<String>();
		for ( File fileEntry : folder.listFiles()) {
	        	file_names.add(fileEntry.getName());
	        }
		//reading the content of documents as strings
		ArrayList<String> docs = new ArrayList<String>();	
		for (int i=0; i<file_names.size();i++) {
			docs.add(new String(Files.readAllBytes(Paths.get(folder.toString()+"\\"+file_names.get(i)))));			
			}
		
		englishStemmer stemmer = new englishStemmer();
		ArrayList<String> docs_final = new ArrayList<String>();	
		
		//trimming the documents at space and stemming them using snowball stemmer
		for (int i=0; i<file_names.size();i++) {
		ArrayList<String> stemmed = new ArrayList<String>();
		String[] trimmed = docs.get(i).trim().split("\\s+");
				for(int j=0; j<trimmed.length;j++) {
				    stemmer.setCurrent(trimmed[j]);
					if (stemmer.stem()){
						stemmed.add(stemmer.getCurrent());};
				 }
			docs_final.add(String.join(",",stemmed));
			}	
		
		String corpus = String.join(",", docs_final);
		String corpus_final = Arrays.stream(corpus.split(",")).distinct().collect(Collectors.joining(","));
		String[] words;
		//words contains the whole corpus for the given documents
		words = corpus_final.split(",");
		double[] freq = new double[words.length];
		double[] a = new double[words.length];
		double n =  docs_final.size() ;
		//calculating the term frequency of each word 
		for (int i=0;i<words.length;i++) {
			freq[i] =0.0;
			for(int j=0;j<docs_final.size();j++) {
				if (docs_final.get(j).contains(words[i])) {
				freq[i]++;};
			}
			a[i]=freq[i]/n;
		}
		
		HashMap<String,Integer> map = new HashMap<>();
		int[] k_bf;
		k_bf = new int[words.length];
		double total=0.0;
		//mapping the words and the number of hash functions required for each word
		for (int i=0;i<words.length;i++) {
			k_bf[i]= (int) Math.max(3.0,Math.min(Math.ceil((a[i])/((1-a[i])*snr)), 20));
			map.put(words[i], k_bf[i]);
			total += a[i]*k_bf[i];
		}
		if (m_bf == 0) {
		m_bf = total/(Math.log(1/(1-d)));
		m_bf = Math.ceil(m_bf);}
		Random r_bf = new Random(5);
		//Array containing the hash seed
		int[] hashSeeds;
		hashSeeds = new int[Collections.max(map.values())];
		for (int i=0; i<hashSeeds.length; ++i) {
            hashSeeds[i] = r_bf.nextInt(1000);
        }
	    //bloom filter for each document is prepared
		BitSet[] bt = new BitSet[docs_final.size()];		
		for (int i=0;i<docs_final.size();i++) {
			bt[i] = new BitSet((int)m_bf);
			String[] words_bit = docs_final.get(i).split(",");			
			//passing each word of the document and preparing bloom filter for doc
			for(int j=0; j<words_bit.length;j++) {
				int p= map.get(words_bit[j]);
				bt[i].or(hash(words_bit[j],p,Arrays.copyOfRange(hashSeeds, 0, p),m_bf));				
			}
		}
		BitSet[] bt_t;
		bt_t = new BitSet[(int)m_bf];
		//transpose of the bloom filter(terms as rows and columns as documents)
		for (int u=0;u<m_bf;u++) {
			bt_t[u] = new BitSet(docs_final.size());
			for (int v=0;v<docs_final.size();v++) {
				bt_t[u].set(v,(bt[v].get(u)));
			}			
		}		
		//saving the index files in the current working directory
		ObjectOutputStream out_1 = new ObjectOutputStream(new FileOutputStream("bt_t.ser"));
		out_1.writeObject(bt_t);
		out_1.flush();
		out_1.close();
		
		ObjectOutputStream out_2 = new ObjectOutputStream(new FileOutputStream("map.ser"));
		out_2.writeObject(map);
		out_2.flush();
		out_2.close();
		
		ObjectOutputStream out_3 = new ObjectOutputStream(new FileOutputStream("hashSeeds.ser"));
		out_3.writeObject(hashSeeds);
	    out_3.flush();
		out_3.close();
		
		ObjectOutputStream out_4 = new ObjectOutputStream(new FileOutputStream("file_names.ser"));
		out_4.writeObject(file_names);
		out_4.flush();
		out_4.close();
		
		System.out.println("Index Files are saved");
		//storage space occupied by the index files 
		String[] indexes = {"bt_t","map","hashSeeds","file_names"};
		String dir = System.getProperty("user.dir");
		double total_mb = 0.0;
        for (String s : indexes) {
        	File file = new File (dir+"\\"+s+".ser") ;
        	total_mb += file.length()/(1024*1024);
        }
        System.out.printf("Total Storage space in MB %f",total_mb);
		
        long total_time = 0 ;
        double fp =0.0;
        int[] freq_words = new int[10000];
        HashMap<String,Long> time = new HashMap<>();
        //for the first 10k words in the corpus,calculating time taken for finding
        //each word,then queries per second and avg percentage of false positive
        //error
		for (int i =0;i<10000;i++) {
			long startTime = System.nanoTime();
			BitSet bs = new BitSet(bt_t.length);
			int p = map.get(words[i]);
			bs.or(hash(words[i],p,Arrays.copyOfRange(hashSeeds, 0, p), bt_t.length));
			BitSet index = (BitSet) bt_t[bs.nextSetBit(0)].clone();
			  for (int j = bs.nextSetBit(0); j >= 0; j = bs.nextSetBit(j+1)) {
				index.and((bt_t[j]));
			    if (j == Integer.MAX_VALUE) {
			    break; 
			    }
			  }
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);
			time.put(words[i], duration);
			total_time += duration;
			if(index.isEmpty()) {freq_words[i] = 0;}
			else {freq_words[i]=index.cardinality();} 
			//freq[i] consists of the number of documents containing that word
			fp += (Math.abs(freq[i] - freq_words[i])/1000)*100;
		}
		double total_qps = 10000/(total_time*0.000000001);
		System.out.printf("Query time for each term :%s",time.toString());
		System.out.printf("\n Queries Per Second %f", total_qps);
		System.out.printf("\n False Positive Error percentage %f", fp/10000);
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
	public static BitSet hash( final String text, final long p ,int[] hash_bf,double m_bf) {
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

