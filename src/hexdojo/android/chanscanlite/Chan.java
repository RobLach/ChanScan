package hexdojo.android.chanscanlite;
import hexdojo.android.chanscanlite.ChanScan.BoardType;

import java.util.ArrayList;

public class Chan {
	private String chanName;
	private String URL;
	private String IconURL;
	private BoardType btype;
	private ArrayList<Board> subBoards = null;
	
	public Chan(){
		subBoards = new ArrayList<Board>();
	}
	
	public String getChanName(){return chanName;}
	public void setChanName(String chanName){this.chanName = chanName;}	
	
	public String getURL(){
		return URL;
	}
	
	public void setURL(String URL){
		this.URL = URL;
	}	
	
	public String getIconURL(){
		return IconURL;
	}
	
	public void setIconURL(String IconURL){
		this.IconURL = IconURL;
	}	
	
	public void addSubBoard(Board board){
		
		subBoards.add(board);
	}
	
	public ArrayList<Board> getSubBoardList(){
		return subBoards;
	}
	
	public BoardType getBoardType()	{
		return btype;
	}
	
	public void setBoardType(BoardType btype){
		this.btype = btype;
	}
}