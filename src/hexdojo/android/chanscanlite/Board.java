package hexdojo.android.chanscanlite;

import hexdojo.android.chanscanlite.ChanScan.BoardType;

import java.util.ArrayList;

public class Board {

	String boardName;
	String shortName;
	String Url;
	int Pages;
	String PageSuffix;
	BoardType m_btype;
	ArrayList<BoardThread> m_threads;
	int currentPage = 0;
	Chan parent;
		
	public Chan getParent() {
		return parent;
	}

	public void setParent(Chan parent) {
		this.parent = parent;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public Board(){
		m_threads = new ArrayList<BoardThread>();
	}
	
	public String getBoardName(){return boardName;}
	public void setBoardName(String boardName){this.boardName = boardName;}	
	
	public String getShortName(){return shortName;}
	public void setShortName(String shortName){this.shortName = shortName;}	
	
	public String getURL(){return Url;}
	public void setURL(String Url){this.Url = Url;}	
	
	public BoardType getBoardType(){
		return m_btype;
	}
	
	public void setBoardType(BoardType type){
		this.m_btype = type;
	}
	
	public ArrayList<BoardThread> getThreads(){
		return m_threads;
	}
	
	public void addThread(BoardThread bthread){
		m_threads.add(bthread);
	}
	public int getPages() {
		return Pages;
	}

	public void setPages(int pages) {
		Pages = pages;
	}

	public String getPageSuffix() {
		return PageSuffix;
	}

	public void setPageSuffix(String pageSuffix) {
		PageSuffix = pageSuffix;
	}
	
}
