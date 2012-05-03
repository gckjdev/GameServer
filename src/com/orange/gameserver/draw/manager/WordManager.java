package com.orange.gameserver.draw.manager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.orange.common.utils.RandomUtil;
import com.orange.gameserver.draw.server.DrawGameServer;

public class WordManager {
	
	public static final Logger log = Logger.getLogger(WordManager.class.getName()); 
	
	// thread-safe singleton implementation
    private static WordManager manager = new WordManager();     
    private WordManager(){
    	super();
    	initWordData("/game/word/Word_En.txt", englishWordList);
    	initWordData("/game/word/Word_Hans.txt", chineseWordList);
	} 	    
    public static WordManager getInstance() { 
    	return manager; 
    }
    
    public final List<String> chineseWordList = new ArrayList<String>();
    public final List<String> englishWordList = new ArrayList<String>();

    public void initWordData(String filename, List<String> list) {
		String read;
		FileReader fileread;
		try {
			fileread = new FileReader(filename);
			BufferedReader bufread = new BufferedReader(fileread);
			try {

				while ((read = bufread.readLine()) != null) {
					String[] reads = read.split(" ");					
					if (reads != null && reads.length > 0){
						list.add(reads[0]);
					}
				}
			} catch (IOException e) {
				log.error("<initWordData> but catch exception="+e.toString(), e);
			}
		} 
		catch (FileNotFoundException e) {
			log.error("<initWordData> but catch exception="+e.toString(), e);
		}
		catch (Exception e){
			log.error("<initWordData> but catch exception="+e.toString(), e);			
		}

	}
    
    public String randomGetWord(int language, int wordLen, boolean isMatchWordLen){
    	List<String> wordList = null;
    	if (language == DrawGameServer.LANGUAGE_CHINESE){
    		wordList = chineseWordList;
    	}
    	else{
    		wordList = englishWordList;
    	}
    	
    	return randomGetWord(wordList, wordLen, isMatchWordLen);
    }
    
	private String randomGetWord(List<String> wordList, int wordLen, boolean isMatchWordLen) {
		int size = wordList.size();
		int index = RandomUtil.random(size);
		String retString = null;
		
		for (int i=index; i<size; i++){
			if (isMatchWordLen){
				retString = wordList.get(i);
				if (wordLen == retString.length()){
					return retString;
				}
			}
			else{
				return wordList.get(i);
			}
		}
		
		for (int j=index; j>=0; j--){
			if (isMatchWordLen){
				retString = wordList.get(j);
				if (wordLen == retString.length()){
					return retString;
				}
			}
			else{
				return wordList.get(j);
			}			
		}
		
		return null;
	}
}
