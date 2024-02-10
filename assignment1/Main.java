package assignment1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

//ReadMe file has the instructions to run the program

public class Main {

	public static void main(String[] args) throws IOException {
		System.out.print("Enter the program and parameters ");
		Scanner sc= new Scanner(System.in);		
		String[] inputs = sc.nextLine().split("\\s");
		sc.close();
		
		if (inputs[0].equals("cdestimate")) {
			//reading the given inputs
			File folder1 = new File(inputs[1]);
			File folder2 = new File(inputs[2]);
			int R =  Integer.parseInt(inputs[3]);
			int t =  Integer.parseInt(inputs[4]);
			listFilesForFolder(folder1,folder2,R,t);}
		else {System.out.println("enter inputs correctly");}
		
		}

	private static void listFilesForFolder(File folder1,File folder2 ,int R, int t) 
	throws IOException {
		//declaring the lists for each input data type
		List<String> source_ip =new ArrayList<String>();
		List<String> cluster_id =new ArrayList<String>();
		List<String> job_sign =new ArrayList<String>();
		List<String> date =new ArrayList<String>();
		List<String> geography =new ArrayList<String>();
		//declaring hashmaps for each respective cluster id ,month and geography 
		//each hashmap stores the distinct elements for the given id
		HashMap<Integer,ArrayList<String>> Cluster_ID = new HashMap<>();
		HashMap<Integer,ArrayList<String>> Month = new HashMap<>();
		HashMap<Integer,ArrayList<String>> Geography_1 = new HashMap<>();
		HashMap<Integer,ArrayList<String>> Geography_2 = new HashMap<>();
		
		//creating keys and values for each hashmap with keys as id and values as new 
		//list of strings
		for (int j=0;j<=99;j++) {
			Cluster_ID.put(j,new ArrayList<String>());}
		for (int j=1;j<=12;j++) {
			Month.put(j,new ArrayList<String>());}
		for (int j=0;j<=9;j++) {
			Geography_1.put(j,new ArrayList<String>());}
		for (int j=0;j<=9;j++) {
			Geography_2.put(j,new ArrayList<String>());}
		
		//generating seed values
		Random r_bf = new Random(5);
		int[] hashSeeds;
		hashSeeds = new int[t];
		for (int j=0; j<hashSeeds.length; ++j) {
            hashSeeds[j] = r_bf.nextInt(1000);
        }
		
		//reading the data file 
		String source = new String(Files.readAllBytes(Paths.get(folder1.toString())));
		//breaking the strings at new line character
		String[] lines = source.split("\\r?\\n");
		
		for(String line :lines) {
			//split each data entry at tab 
			String[] lineSplited = line.split("\\s+");
			for (int j = 0; j < lineSplited.length; j++) { 
				if (j == 0)
					//collecting all the source ip strings
					{source_ip.add(lineSplited[j]);}
				else if (j==1)
					//mapping the job signature for the given cluster id
					{cluster_id.add(lineSplited[j]);
					 int x = Integer.parseInt(lineSplited[j]);
					 Cluster_ID.get(x).add(lineSplited[2]);}
				else if (j==2)
					//collecting all the source ip strings
					{job_sign.add(lineSplited[j]);}
				else if (j==3)
					//mapping the job signature for the given Month
					{date.add(lineSplited[j]);
					 int x = Integer.parseInt(lineSplited[j].split(":")[1]);
					 Month.get(x).add(lineSplited[2]);}
				else if (j==4)
					//mapping the job signature and IP address for the given Geography
					{geography.add(lineSplited[j]);
					 int x = Integer.parseInt(lineSplited[j]);
					 Geography_1.get(x).add(lineSplited[2]);
					 Geography_2.get(x).add(lineSplited[0]);}
			}
		}
		
		//reading the query file 
		String result = new String(Files.readAllBytes(Paths.get(folder2.toString())));
		//breaking the strings at new line character
		String[] lines_result = result.split("\\r?\\n");
		
		//list for storing the range of id's given in the query file
		List<Integer> range = new ArrayList<Integer>();
		
		//for each query id in the file 
		for(String line :lines_result) {
			//split each data entry at tab 
			String[] lineSplited = line.split("\\s+");
			int m =0; int k=0;
			//if the query line has length 2,the query is to find number of distinct 
			//elements in the given id of cluster ,month or geography
			if (lineSplited.length == 2) {
				 m = Integer.parseInt(lineSplited[1]);}
			//if the query line has length 3,the query is to find number of distinct 
			//elements in the given range of id's of cluster and month
			else if (lineSplited.length == 3) {
				 range.clear();
				 m = Integer.parseInt(lineSplited[1]);
				 k = Integer.parseInt(lineSplited[2]);
				 for (int j=m;j<=k;j++) {
					 range.add(j);
					 }}
			//if the query line has more length than 2 or 3,the query is to find number  
			//of distinct elements in the given set of geographies
			else {range.clear();
				 for (int j=1;j<lineSplited.length;j++) {
				 range.add(Integer.parseInt(lineSplited[j]));
				 }}
			    //query id in the file is passed
				switch(Integer.parseInt(lineSplited[0])) {
				case 1: Estimate(Update(source_ip,hashSeeds,R),R);break;
				case 2: Estimate(Update(job_sign,hashSeeds,R),R);break;
				case 3: Estimate(Update(Cluster_ID.get(m),hashSeeds,R),R);break;
				case 4: Estimate(Update(Cluster_ID,range,hashSeeds,R),R);break;
				case 5: Estimate(Update(Month.get(m),hashSeeds,R),R);break;
				case 6: Estimate(Update(Month,range,hashSeeds,R),R);break;
				case 7: Estimate(Update(Geography_1.get(m),hashSeeds,R),R);break;
				case 8: Estimate(Update(Geography_2.get(m),hashSeeds,R),R);break;
				case 9: Estimate(Update(Geography_1,range,hashSeeds,R),R);break;
				case 10:Estimate(Update(Geography_2,range,hashSeeds,R),R);break;
			}
		}
	}
	
	private static List<BitSet[]> Update(HashMap<Integer, ArrayList<String>> elements,
											List<Integer> range,int[] hashSeeds,int R) {
		List<String> n = new ArrayList<String>();
		List<BitSet[]> cd = new ArrayList<BitSet[]>();
		long z ;
		int  w;
		int i ;
		//for each hash seed
		for (int seed :hashSeeds) {
		
		List<BitSet[]> all_sets = new ArrayList<BitSet[]>();
		//for each given id in the range or set 
			for (int j: range) {
				//collect the input strings in the corresponding id given
				n=elements.get(j);
				//create a bitset array of length R
				BitSet[] x= new BitSet[66];
				for(int s=1;s<=65;s++) {
					x[s] = new BitSet(R);
				}
				//for each string in the corresponding cluster id 
				for(int p=0;p<n.size();p++) {
					z = hash64(n.get(p),seed);
					w = 1+ trailing_zeros(z);
					i = (int)((z/(long)Math.pow(2, w))%R );
					x[w].set(i,true);
				}
				//add all the cd structures for given id range
				//for each hash seed
				all_sets.add(x);
			}
			
			//performing union operation for the given range for each hash
			BitSet[] array = new BitSet[66];
			for(int s=1;s<=65;s++) {
				BitSet b = new BitSet(R);
				for (int j=0;j<all_sets.size();j++) {
					b.or(all_sets.get(j)[s]);
				}
				array[s] = b ;
			}
			//add the cd after union range for each hash 
			cd.add(array);
		}
		return cd;
	}

	private static  List<BitSet[]> Update(List<String> elements, int[] hashSeeds, int R) {
		long z ;
		int  w;
		int i ;
		//list of bitsets for storing where each bitset array is from each hash function
		List<BitSet[]> cd = new ArrayList<BitSet[]>();
		//for each hash seed
		for (int seed :hashSeeds) {
			//create a bitset array of length R
			BitSet[] x= new BitSet[66];
			for(int j=1;j<=65;j++) {
				x[j] = new BitSet(R);
			}
			//for each string in the corresponding cluster id 
			for(int p=0;p<elements.size();p++) {
				//hash the string
				z = hash64(elements.get(p),seed);
				//add 1 to the trailing zeros
				w = 1+ trailing_zeros(z);
				//update the specific cell in the wth row id 
				i = (int) ((z/(long)Math.pow(2, w))%R) ;
				x[w].set(i,true);
			}
			//add each cd structure of each hash function to the list
			cd.add(x);
		}
		return cd;
	}

	private static void Estimate(List<BitSet[]> each_cluster, int R) {
		//list for adding unique count estimate from each hash
		ArrayList<Double> f = new ArrayList<Double>() ;
		
		//for each cd structure
		for(BitSet[] b: each_cluster) {
			int min = R/2;int w = 0;
			for(int j=1; j<=65;j++) {
				//finding minimum of (number of zeros) - (R/2)
				//(R- cardinality of bitset)-(R/2) = (R/2)- cardinality of bitset
				if(Math.abs((R/2)-b[j].cardinality())<min) {
					min = Math.abs((R/2)-(b[j].cardinality()));
					w = j;
				}
			}
			//calculating p and f0 as per the algorithm
			double p = (R-b[w].cardinality())/(double)R;
			Double f0 = Math.pow(2, w)*(Math.log(p)/Math.log(1-(1/(double)R)));
			f.add(f0);
		}
		//sort the values in increasing order
		Collections.sort(f);
		
		//finding median if t is even
		if (f.size() % 2 == 0)
		    System.out.printf("\n %f" ,f.get(f.size()/2) + f.get(f.size()/2 - 1)/2);
		//finding median if t is odd by averaging 
		else
			System.out.printf("\n %f" ,f.get(f.size()/2));
	}

	//function to count number of trailing zeros
	private static int trailing_zeros(long z) {
		if (z == 0) {
		    return 0;
		}
		int counter = 0;
		while (z % 10 == 0) {
		    counter++;
		    z /= 10;
		}
		return counter;
	}
	
	//hash functions that takes strings,seed value and pass the bytes for hashing
	public static long hash64( final String text,final int seed) {
		final byte[] bytes = text.getBytes(); 
		return Math.abs(hash64( bytes, bytes.length,seed));
	}
	
	//hash function that takes bytes as input and return the hash 
	public static long hash64( final byte[] data, int length, int seed) {
		final long m = 0xc6a4a7935bd1e995L;
		final int r = 47;

		long h = (seed&0xffffffffl)^(length*m);

		int length8 = length/8;

		for (int i=0; i<length8; i++) {
			final int i8 = i*8;
			long k =  ((long)data[i8+0]&0xff)      +(((long)data[i8+1]&0xff)<<8)
					+(((long)data[i8+2]&0xff)<<16) +(((long)data[i8+3]&0xff)<<24)
					+(((long)data[i8+4]&0xff)<<32) +(((long)data[i8+5]&0xff)<<40)
					+(((long)data[i8+6]&0xff)<<48) +(((long)data[i8+7]&0xff)<<56);
			
			k *= m;
			k ^= k >>> r;
			k *= m;
			
			h ^= k;
			h *= m; 
		}
		
		switch (length%8) {
		case 7: h ^= (long)(data[(length&~7)+6]&0xff) << 48;
		case 6: h ^= (long)(data[(length&~7)+5]&0xff) << 40;
		case 5: h ^= (long)(data[(length&~7)+4]&0xff) << 32;
		case 4: h ^= (long)(data[(length&~7)+3]&0xff) << 24;
		case 3: h ^= (long)(data[(length&~7)+2]&0xff) << 16;
		case 2: h ^= (long)(data[(length&~7)+1]&0xff) << 8;
		case 1: h ^= (long)(data[length&~7]&0xff);
		        h *= m;
		};
	 
		h ^= h >>> r;
		h *= m;
		h ^= h >>> r;

		return h;
	}
}
