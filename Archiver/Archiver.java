

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Archiver {


	//epoch = 00.00.00 Jan 1st, 1970
	private String absoluteDirectoryPath = null;
	private File f = null;
	private File writer;
	private boolean goodFilePath = false;
	private long dateModified;
	private int totalYears;		
	private int currYear;  
	private long totalDays;  
	private long dayOfYear;
	private int intMonth;  
	private String newDirName = null;
	private String sortedFolderMarker = "~";
	private BufferedWriter bw = null;


	public final int daysInJan = 31; 
	public final int daysInFeb = 28;
	public final int daysInMar = 31;
	public final int daysInApr = 30;
	public final int daysInMay = 31;
	public final int daysInJune= 30;
	public final int daysInJuly = 31;
	public final int daysInAug = 31;
	public final int daysInSept = 30;
	public final int daysInOct = 31;
	public final int daysInNov = 30;


	public final int dayFebStart = daysInJan;
	public final int dayMarStart = dayFebStart+daysInFeb;
	public final int dayAprStart= dayMarStart+daysInMar;
	public final int dayMayStart= dayAprStart+daysInApr;
	public final int dayJuneStart= dayMayStart+daysInMay;
	public final int dayJulyStart= dayJuneStart+daysInJune;
	public final int dayAugStart= dayJulyStart+daysInJuly;
	public final int daySeptStart= dayAugStart+daysInAug;
	public final int dayOctStart= daySeptStart+daysInSept;
	public final int dayNovStart= dayOctStart+daysInOct;
	public final int dayDecStart= dayNovStart+daysInNov;


	private final String[] listMonths = {"January","February","March", "April", "May", "June", "July",
			"August","September", "October", "November", "December"};

	//day each month starts every year put into array
	private final int[] startDayOfMonth = {0,dayFebStart,dayMarStart,dayAprStart,dayMayStart,dayJuneStart,dayJulyStart
			,dayAugStart,daySeptStart,dayOctStart,dayNovStart,dayDecStart};

	public Archiver(){

	}

	public boolean getFilePath(){

		System.out.println("what directory would you like me to organize?");


		while(!goodFilePath){  //gets valid file path
			Scanner input = new Scanner(System.in);
			absoluteDirectoryPath =input.nextLine();
			f = new File(absoluteDirectoryPath);
			if(f.isDirectory()){  
				goodFilePath = true;
			}else{
				System.out.println("directory was bad, please restate directory:");
			}
		}

		return true;
	}

	private boolean initWriter(){
		try {
			bw = new BufferedWriter(new FileWriter(writer.toString(),true));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean initArchive(){
		writer = new File(absoluteDirectoryPath+"\\~Archive");
		if(!writer.exists()){
			writer.mkdir();
		}
		writer = new File(absoluteDirectoryPath+"\\~Archive\\Archive.txt");

		if(!writer.exists()){
			try {
				writer.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return initWriter();  

	}
	private boolean writeToArchive(Path tmpDest){
		try {
			bw.write(tmpDest.toString());
			bw.newLine();
			bw.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void moveFile(Path tmpSource, Path tmpDest,File tmpFile){
		try {

			Files.move(tmpSource, tmpDest, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			System.out.println("file:"+tmpFile + "\n"+ "already exists in:"+ absoluteDirectoryPath +
					"\n File was not moved");
			e.printStackTrace();
		}
	}
	
	public boolean sortFiles(){
		File[] listOfFiles = f.listFiles();  
		String[] absolute = f.list();
		for(int i = 0; i < listOfFiles.length; ++i){
			if(absolute[i].toString().startsWith(sortedFolderMarker)){  //file is a sorted folder

			}else{
				dateModified = listOfFiles[i].lastModified();
				totalDays = TimeUnit.MILLISECONDS.toDays(dateModified); 
				totalYears = (int) (totalDays/365); 
				dayOfYear = totalDays;
				for(int j = 2; dayOfYear>365; ++j){  //loewr to day of year that it was modified
					//offset j by 2 because there is leap year in 1972. 2 years after epoch
					dayOfYear-=365;
					if(j%4 == 2){  //leap year
						dayOfYear--;
					}
				}
				intMonth = 0;
				for(intMonth = 0; intMonth<11; ++intMonth){ //find what month file was made
					if(dayOfYear > startDayOfMonth[intMonth] && dayOfYear< startDayOfMonth[intMonth+1]){ 
						break;
					}
				}
				currYear = 1970 + totalYears;  //epoch + years since epoch
				newDirName = absoluteDirectoryPath+"\\"+sortedFolderMarker+listMonths[intMonth]+" "+ currYear;  // backwards slash for windows
				//the ~ is to mark a distinct year so that it is easily filtered out of
				f = new File(newDirName);  //chosen directory + month + year
				Path source = listOfFiles[i].toPath();
				Path moveTo =Paths.get(newDirName+"\\"+absolute[i]);
				if(f.exists()){
					moveFile(source,moveTo,listOfFiles[i]);
				}else{
					f.mkdir();
					moveFile(source,moveTo,listOfFiles[i]);
				}
				writeToArchive(moveTo);

			}
		}
		//end going through and sorting files
		System.out.println("All files have been archived");
		return true;
	}


}
