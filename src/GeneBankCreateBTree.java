import java.util.*;
import java.io.*;
import java.lang.*;

public class GeneBankCreateBTree {
	

	public static void main(String[] args){
		
		if( args.length < 4 || args.length > 6){
			System.out.println("Input should be in the following format:" + "\n" +
					"<0/1(no/with Cache)> <degree> <gbk file> <sequence length> [<cache size>] [<debug level>]"
					+ "\n The sequence length needs to be beween 0 and 31.");
			System.exit(1);
					
		}
		
		parseCommandLineArgs(args);
		
	}
	
	public static void parseCommandLineArgs(String[] args){
		
		int cache = 0;
		int degree = 0;
		File myFile = null;
		int seqLength = 0;
		int cacheSize = 0;
		int debugLvl = 0;
		
		for (int i = 0; i < args.length; i++){
			switch(i){
			
			case 0: 
				try{
					cache = Integer.parseInt(args[i]);
				}catch(NumberFormatException e){
					e.printStackTrace();
					System.exit(1);
				}	
				if(cache == 0){
					System.out.println("no cashe\n");
				}else if(cache == 1){
					System.out.println("with cashe\n");
				}else{
					System.out.println("arg 1 incompatible");
					System.exit(1);
					}
			break;
			
			case 1: 
				try{
					degree = Integer.parseInt(args[i]);
				}catch(NumberFormatException e){
					e.printStackTrace();
					System.exit(1);
				}	
			break;
			
			case 2:
				try{
					myFile = new File(args[i]);
					if( ! myFile.exists() ) throw new FileNotFoundException("The File " + args[i] + " does not exist."); 
				}
				catch(FileNotFoundException e){
					e.printStackTrace();
				}
			break;
			
			case 3:
				try{
					seqLength = Integer.parseInt(args[i]);
					if(seqLength > 31 || seqLength < 0){
						System.out.println("The sequence length needs to be between 0 and 31.\n");
						System.exit(1);
						}
				}catch(NumberFormatException e){
					e.printStackTrace();
				}
			break;
			
			case 4: 
				try{
					cacheSize = Integer.parseInt(args[i]);
				}catch(NumberFormatException e){
					e.printStackTrace();
				}
			break;
			
			case 5:
				try{
					debugLvl = Integer.parseInt(args[i]);
				}catch(NumberFormatException e){
					e.printStackTrace();
				}	
				if(cache == 0){
					System.out.println("no debug\n");
				}else if(debugLvl == 1){
					System.out.println("with debug\n");
				}else{
					System.out.println("arg 1 incompatible");
					System.exit(1);
					}
				break;
			}
			
		}
		
		System.out.println("end of parse");
		
		createBtree(cache, degree, myFile, seqLength, cacheSize, debugLvl);
		
	}

	public static void createBtree(int cache, int degree, File file, int seqLength, int cacheSize, int debugLvl){
	
		try {
			BTree tree = new BTree(degree, seqLength/*, sequence*/ );
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
		Scanner scanFile = new Scanner(file);
		StringBuilder SB;
		Scanner otherScanner;
		
		System.out.println("tree time");
		
		String curVal = scanFile.next();
		
		while (scanFile.hasNext()){
			if( curVal.equals("ORIGIN") ){
				System.out.println("Origin found");
				SB = new StringBuilder();
				
				while(!curVal.equals("//") && !curVal.equals("N")){
					scanFile.next();
					System.out.println("appending string");
					SB.append(scanFile.nextLine());
					otherScanner = scanFile;
					curVal = otherScanner.next();
					System.out.println("Current value = " + curVal);
				}
				System.out.println("Printing sequence");
				System.out.println(SB.toString());
			}
			if(scanFile.hasNext()) curVal = scanFile.next();
		}
		
		
		scanFile.close();
		}catch(FileNotFoundException e){
		}
	}
}