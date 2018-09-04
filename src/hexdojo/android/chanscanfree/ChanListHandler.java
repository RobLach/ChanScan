package hexdojo.android.chanscanfree;

import hexdojo.android.chanscanfree.ChanScan.BoardType;

import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler; 

public class ChanListHandler extends DefaultHandler {
	boolean inSiteList = false;
	boolean inSite = false;
	boolean inBoard = false;
	private ArrayList<Chan> m_chans = null;
	Chan currentChan;
	Board currentBoard;
	
	
	public ChanListHandler(ArrayList<Chan> list){
		this.m_chans = list;
	}
	
	public ArrayList<Chan> getChanList(){
		return m_chans;
	}
	
	public void setChanList(ArrayList<Chan> chanlist){
		m_chans = chanlist;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		if(localName.equals("SiteList")){
			inSiteList = true;
		} else if(localName.equals("Site")){
			inSite = true;
			currentChan = new Chan();
			currentChan.setChanName(attributes.getValue("Name"));
			currentChan.setIconURL((String)attributes.getValue("Icon"));
			currentChan.setURL((String)attributes.getValue("URL"));
			String btype = (String)attributes.getValue("Type");
			 if(btype.contains("NEWFOURCHANv2")){
				currentChan.setBoardType(BoardType.NEWFOURCHANv2);
			}
			else if(btype.contains("NEWFOURCHAN")){
				currentChan.setBoardType(BoardType.NEWFOURCHAN);
			}
			else if(btype.contains("FOURCHAN"))
			{
				currentChan.setBoardType(BoardType.FOURCHAN);
			}
			else if(btype.contains("SEVENCHAN")){
				currentChan.setBoardType(BoardType.SEVENCHAN);
			}
			else if(btype.contains("FOURTWENTYCHAN")){
				currentChan.setBoardType(BoardType.FOURTWENTYCHAN);
			}
			else if(btype.contains("TWOCHAN")){
				currentChan.setBoardType(BoardType.TWOCHAN);
			}
			else if(btype.contains("KUSABAX")){
				currentChan.setBoardType(BoardType.KUSABAX);
			}
			else if(btype.contains("WAKABA")){
				currentChan.setBoardType(BoardType.WAKABA);
			}
			else if(btype.contains("FOURARCHIVE")){
				currentChan.setBoardType(BoardType.FOURARCHIVE);
			}
			else{
				currentChan.setBoardType(BoardType.TWOCHAN);
			}
			
		} else if(localName.equals("Board")){
			inBoard = true;
			if(inSite){
				try{
				currentBoard = new Board();
				currentBoard.setBoardName(attributes.getValue("Name"));
				currentBoard.setShortName(attributes.getValue("short"));
				currentBoard.setURL((String)attributes.getValue("URL"));
				currentBoard.setPageSuffix((String)attributes.getValue("pagesuffix"));
				currentBoard.setPages(Integer.valueOf(attributes.getValue("pages")));
				currentBoard.setParent(currentChan);
				}catch(Exception e){} 
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		if(localName.equals("SiteList")){ 
			inSiteList = false;
		}
		else if(localName.equals("Site")){
			inSite = false;
			m_chans.add(currentChan);
		}
		else if(localName.equals("Board")){
			inBoard = false;
			currentBoard.setBoardType(currentChan.getBoardType());
			currentChan.addSubBoard(currentBoard);
		}
	}
}
