package hexdojo.android.chanscanfree;

import hexdojo.android.chanscanfree.ChanScan.BoardType;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler; 

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

//import android.util.//Log;

public class BoardListHandler extends DefaultHandler{
		
	private ArrayList<BoardThread> m_threads = null;
	
	public ArrayList<BoardThread> getThreadList() {
		return m_threads;
	}

	public void setThreadList(ArrayList<BoardThread> mThreads) {
		m_threads = mThreads;
	}

	BoardType type;
	BoardThread currentThread;
	String boardURL;
	StringBuffer buff = null;
	PostingInfo postingInfo;
	


	//4chan parsing bools
	Boolean inForm = false;
	Boolean inNewThread = false;
	Boolean inGotThumb = false;
	Boolean inReachedPreAuthor = false;
	Boolean inReachedAuthor = false;
	Boolean inGotAuthor = false;
	Boolean inReachedText = false;
	Boolean inGotText = false;
	Boolean inDone = false;
	Boolean inReachedThreadUrl = false;
	Boolean inGotThreadURL = false;
	Boolean inReachedPreTitle = false;
	Boolean inReachedTitle = false;
	Boolean inGotTitle = false;
	Boolean inReachedPostCount = false;
	Boolean inGotPostCount = false;
	
	//PostInfo Flags
	Boolean inGotPostInfo = false;
	Boolean inReachedPostForm = false;
	
	
	//7chan parsing bools
	Boolean inOpDescription = false;
	Boolean inThumbDiv = false;
	
	Boolean inGotFirstPost = false;
	
	public BoardListHandler(ArrayList<BoardThread> threads, BoardType btype, String boardURL, PostingInfo postingInfo){
		this.m_threads = threads;
		this.type = btype;
		this.boardURL = boardURL;
		this.postingInfo = postingInfo;
		//Log.e("Handler", boardURL);
		//Log.e("Type", btype.toString());
	}
	
	public PostingInfo getPostingInfo() {
		return postingInfo;
	}

	public void setPostingInfo(PostingInfo postingInfo) {
		this.postingInfo = postingInfo;
	}
	
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
		
		if(type.equals(BoardType.NEWFOURCHANv2)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					String id = atts.getValue("name");
					String action = atts.getValue("action");
					String enctype = atts.getValue("enctype");
					if(id!=null && id.equals("post")){
						if(action != null){
							//String baseURL = "";
							//try {
							//	baseURL = "http://"+(new URL(this.boardURL).getHost());
							//} catch (MalformedURLException e) {	}
							postingInfo.actionURL = action;
						}
						if(enctype != null){
							postingInfo.encodeType = enctype;
						}
						inReachedPostForm = true;
						Log.e("NEWFOURCHANv2", "Reached Post Form URL "+ postingInfo.actionURL);
					}
				}
				else if(inReachedPostForm && (localName.equals("input") || localName.equals("textarea"))){
					String name = atts.getValue("name");
					String value = atts.getValue("value");
					String type = atts.getValue("type");
					
					if(name != null){
						if(type != null && type.equals("file")){
							postingInfo.fileRequestName=name;
						}
						else if(value != null){
							postingInfo.inputPairs.add(new BasicNameValuePair(name, value));
						}else{
							postingInfo.inputPairs.add(new BasicNameValuePair(name, ""));
						}
					}
					Log.e("NEWFOURCHANv2","Value Pair: "+ name + " " + value);
				}
				else if(inReachedPostForm && qName.equals("img")){
					String att = atts.getValue("src");
					if(att != null){
						//att = att.replace("api.recaptcha.net", "www.google.com/recaptcha/api");
						//att = "http:".concat(att);	
						postingInfo.captchaTestURL=att;
					}
				}
			}
			else{
				if(localName.equals("form")){
			
				String name = atts.getValue("id");
				if(name != null && name.equals("delform")){
					inForm = true;
					Log.e("NEWFOURCHANv2", "in form");
					m_threads = new ArrayList<BoardThread>();
					}
				}
				else if(inForm && (!inNewThread) && (!inGotAuthor)&& localName.equals("div")){
					String name = atts.getValue("class");
					if(name != null && name.equals("thread")){
						inNewThread = true;
						currentThread = new BoardThread(type);
						Log.e("NEWFOURCHANv2", "in new thread " +name);
					}					
				}
				else if(inNewThread && (!inGotTitle) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.equals("subject")){
						inReachedTitle = true;
						Log.e("NEWFOURCHANv2", "reached title "+name);
						buff = new StringBuffer("");
					}
				}
				else if(inNewThread && (!inGotAuthor) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.equals("name")){
						inReachedAuthor = true;
						Log.e("NEWFOURCHANv2", "reached author "+name);
					}
				}
				else if(inGotAuthor && (!inReachedThreadUrl) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.contains("postNum")){
						inReachedThreadUrl = true;
						Log.e("NEWFOURCHANv2", "in reached thread url "+ name);
					}
				}
				else if(inReachedThreadUrl && (!inGotThreadURL) && localName.equals("a")){
					String name = atts.getValue("href");
					//if(name != null && name.contains("res")){
					if(name != null && name.contains("thread")){
						String [] balls = name.split("#");
						
						String url = boardURL;
						//String [] sub = balls[0].split("res");
						//url = url.replace(sub[0], "");
						
						currentThread.setOpenThreadURL(url + balls[0]);
						inGotThreadURL = true;
						Log.e("NEWFOURCHANv2", "in got open thread url "+currentThread.getOpenThreadURL());
					}
				}
				else if((!inGotThumb) && localName.equals("img")){
					String src = atts.getValue("src");
					//if(src != null && src.contains("thumb")){
					if(src != null && src.contains("s.jpg")){
						//String baseURL = "";
						//try {
						//	baseURL = "http:"+(new URL(this.boardURL).getHost());
						//} catch (MalformedURLException e) {
						//}
						currentThread.setThumbImageURL("http:"+src);
						inGotThumb = true;
						Log.e("NEWFOURCHANv2", "in got thumb "+currentThread.getThumbImageUrl());
					}
				}
	
				else if(inGotThumb && (!inReachedText) && (!inGotText) && localName.equals("blockquote")){
						inReachedText = true;
						buff = new StringBuffer("");
						Log.e("NEWFOURCHANv2", "in reached text");
				}
				if(inReachedText && !inReachedPostCount && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.contains("summary")){
						inReachedPostCount = true;
						Log.e("NEWFOURCHANv2", "in reached post count " + name);
						////Log.e("fuck","????");
					}				
				}
				if(inGotText && localName.equals("hr")){
					inNewThread = false;
					inGotThumb = false;
					inReachedAuthor = false;
					inGotAuthor = false;
					inReachedText = false;
					inGotText = false;
					inReachedTitle = false;
					inGotTitle = false;
					inOpDescription = false;
					inGotThreadURL = false;
					inReachedPostCount = false;
					inGotPostCount = false;
					currentThread.setBtype(this.type);
					m_threads.add(currentThread);
					Log.e("NEWFOURCHANv2", "Added thread");
				}
			}
		}
		else if(type.equals(BoardType.NEWFOURCHAN)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					String id = atts.getValue("name");
					String action = atts.getValue("action");
					String enctype = atts.getValue("enctype");
					if(id!=null && id.equals("post")){
						if(action != null){
							//String baseURL = "";
							//try {
							//	baseURL = "http://"+(new URL(this.boardURL).getHost());
							//} catch (MalformedURLException e) {	}
							postingInfo.actionURL = action;
						}
						if(enctype != null){
							postingInfo.encodeType = enctype;
						}
						inReachedPostForm = true;
						//Log.e("NEWFOURCHAN", "Reached Post Form URL "+ postingInfo.actionURL);
					}
				}
				else if(inReachedPostForm && (localName.equals("input") || localName.equals("textarea"))){
					String name = atts.getValue("name");
					String value = atts.getValue("value");
					String type = atts.getValue("type");
					
					if(name != null){
						if(type != null && type.equals("file")){
							postingInfo.fileRequestName=name;
						}
						else if(value != null){
							postingInfo.inputPairs.add(new BasicNameValuePair(name, value));
						}else{
							postingInfo.inputPairs.add(new BasicNameValuePair(name, ""));
						}
					}
					//Log.e("NEWFOURCHAN","Value Pair: "+ name + " " + value);
				}
				else if(inReachedPostForm && qName.equals("iframe")){
					String att = atts.getValue("src");
					if(att != null){
						//att = att.replace("api.recaptcha.net", "www.google.com/recaptcha/api");
						att = "http:".concat(att);	
						postingInfo.captchaTestURL=att;
					}
				}
			}
			else{
				if(localName.equals("form")){
			
				String name = atts.getValue("id");
				if(name != null && name.equals("delform")){
					inForm = true;
					//Log.e("NEWFOURCHAN", "in form");
					m_threads = new ArrayList<BoardThread>();
					}
				}
				else if(inForm && (!inNewThread) && (!inGotAuthor)&& localName.equals("div")){
					String name = atts.getValue("class");
					if(name != null && name.equals("thread")){
						inNewThread = true;
						currentThread = new BoardThread(type);
						//Log.e("NEWFOURCHAN", "in new thread " +name);
					}					
				}
				else if(inNewThread && (!inGotTitle) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.equals("subject")){
						inReachedTitle = true;
						//Log.e("NEWFOURCHAN", "reached title "+name);
						buff = new StringBuffer("");
					}
				}
				else if(inNewThread && (!inGotAuthor) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.equals("name")){
						inReachedAuthor = true;
						//Log.e("NEWFOURCHAN", "reached author "+name);
					}
				}
				else if(inGotAuthor && (!inReachedThreadUrl) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.equals("postNum")){
						inReachedThreadUrl = true;
						//Log.e("NEWFOURCHAN", "in reached thread url "+ name);
					}
				}
				else if(inReachedThreadUrl && (!inGotThreadURL) && localName.equals("a")){
					String name = atts.getValue("href");
					if(name != null && name.contains("res")){
						String [] balls = name.split("#");
						
						String url = boardURL;
						//String [] sub = balls[0].split("res");
						//url = url.replace(sub[0], "");
						
						currentThread.setOpenThreadURL(url + balls[0]);
						inGotThreadURL = true;
						//Log.e("NEWFOURCHAN", "in got open thread url "+currentThread.getOpenThreadURL());
					}
				}
				else if((!inGotThumb) && localName.equals("img")){
					String src = atts.getValue("src");
					if(src != null && src.contains("thumbs")){
						//String baseURL = "";
						//try {
						//	baseURL = "http:"+(new URL(this.boardURL).getHost());
						//} catch (MalformedURLException e) {
						//}
						currentThread.setThumbImageURL("http:"+src);
						inGotThumb = true;
						//Log.e("NEWFOURCHAN", "in got thumb "+currentThread.getThumbImageUrl());
					}
				}
	
				else if(inGotThumb && (!inReachedText) && (!inGotText) && localName.equals("blockquote")){
						inReachedText = true;
						buff = new StringBuffer("");
						//Log.e("NEWFOURCHAN", "in reached text");
				}
				if(inReachedText && !inReachedPostCount && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.contains("summary")){
						inReachedPostCount = true;
						//Log.e("NEWFOURCHAN", "in reached post count " + name);
						////Log.e("fuck","????");
					}				
				}
				if(inGotText && localName.equals("hr")){
					inNewThread = false;
					inGotThumb = false;
					inReachedAuthor = false;
					inGotAuthor = false;
					inReachedText = false;
					inGotText = false;
					inReachedTitle = false;
					inGotTitle = false;
					inOpDescription = false;
					inGotThreadURL = false;
					inReachedPostCount = false;
					inGotPostCount = false;
					currentThread.setBtype(this.type);
					m_threads.add(currentThread);
					//Log.e("NEWFOURCHAN", "Added thread");
				}
			}
		}
		else if(type.equals(BoardType.FOURTWENTYCHAN)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					String id = atts.getValue("id");
					String action = atts.getValue("action");
					String enctype = atts.getValue("enctype");
					if(id!=null && id.equals("postform")){
						if(action != null){
							String baseURL = "";
							try {
								baseURL = "http://"+(new URL(this.boardURL).getHost());
							} catch (MalformedURLException e) {	}
							postingInfo.actionURL = baseURL + action;
						}
						if(enctype != null){
							postingInfo.encodeType = enctype;
						}
						inReachedPostForm = true;
					}
				}
				else if(inReachedPostForm && (localName.equals("input") || localName.equals("textarea"))){
					String name = atts.getValue("name");
					String value = atts.getValue("value");
					String type = atts.getValue("type");
					
					if(name != null){
						if(type != null && type.equals("file")){
							postingInfo.fileRequestName=name;
						}
						else if(value != null){
							postingInfo.inputPairs.add(new BasicNameValuePair(name, value));
						}else{
							postingInfo.inputPairs.add(new BasicNameValuePair(name, ""));
						}
					}
				}
				
				
			}
			else{
				if(localName.equals("form")){
			
				String name = atts.getValue("id");
				if(name != null && name.equals("delform")){
					inForm = true;
					m_threads = new ArrayList<BoardThread>();
					}
				}
				else if(inForm && (!inNewThread) && (!inGotAuthor)&& localName.equals("div")){
					String name = atts.getValue("class");
					if(name != null && name.equals("thread_header")){
						inNewThread = true;
						currentThread = new BoardThread(type);
					}					
				}
				else if(inNewThread && (!inGotTitle) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.equals("filetitle")){
						inReachedTitle = true;
					}
				}
				else if(inNewThread && (!inGotAuthor) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.equals("postername")){
						inReachedAuthor = true;
					}
				}
				else if(inGotAuthor && (!inReachedThreadUrl) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.equals("reflink")){
						inReachedThreadUrl = true;
					}
				}
				else if(inReachedThreadUrl && (!inGotThreadURL) && localName.equals("a")){
					String name = atts.getValue("href");
					if(name != null){
						String [] balls = name.split("#");
						
						String url = boardURL;
						String [] sub = balls[0].split("res");
						url = url.replace(sub[0], "");
						
						currentThread.setOpenThreadURL(url + balls[0]);
						inGotThreadURL = true;
					}
				}
				else if(inGotAuthor && (!inGotThumb) && localName.equals("img")){
					String name = atts.getValue("class");
					String src = atts.getValue("src");
					if(name != null && src != null && name.equals("thumb")){
						String baseURL = "";
						try {
							baseURL = "http://"+(new URL(this.boardURL).getHost());
						} catch (MalformedURLException e) {
						}
						currentThread.setThumbImageURL(baseURL+ src);
						inGotThumb = true;
					}
				}
	
				else if(inGotThumb && (!inReachedText) && (!inGotText) && localName.equals("blockquote")){
						inReachedText = true;
						buff = new StringBuffer("");
				}
				if(inReachedText && !inReachedPostCount && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.contains("omitted")){
						inReachedPostCount = true;
						////Log.e("fuck","????");
					}				
				}
				if(inGotText && localName.equals("table")){
					inNewThread = false;
					inGotThumb = false;
					inReachedAuthor = false;
					inGotAuthor = false;
					inReachedText = false;
					inGotText = false;
					inReachedTitle = false;
					inGotTitle = false;
					inOpDescription = false;
					inGotThreadURL = false;
					inReachedPostCount = false;
					inGotPostCount = false;
					currentThread.setBtype(this.type);
					m_threads.add(currentThread);
				}
			}
		}
		if(type.equals(BoardType.SEVENCHAN)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					String action = atts.getValue("action");
					String enctype = atts.getValue("enctype");
					String id = atts.getValue("id");
					if(id!=null && id.equals("posting_form") && enctype!=null && action!=null){
						postingInfo.actionURL = action;
						postingInfo.encodeType = enctype;
						inReachedPostForm = true;
					}
				}
				else if(localName.equals("input")||localName.equals("textarea")||localName.equals("select")){
					String name = atts.getValue("name");
					String value = atts.getValue("value");
					String type = atts.getValue("type");
					if(name!=null){
						if(type!= null && type.equals("file")){
							postingInfo.fileRequestName = name;
						}
						else{
							if(value!=null){
								postingInfo.inputPairs.add(new BasicNameValuePair(name, value));
							}
							else{
								postingInfo.inputPairs.add(new BasicNameValuePair(name, ""));
							}
						}
					}
				}
			}else{ 
				if(localName.equals("form")){
					String name = atts.getValue("id");
					if(name != null && name.equals("delform")){
						inForm = true;
						m_threads = new ArrayList<BoardThread>();
					}
				}
				else if(inForm && (!inNewThread) && (!inGotThumb)&& localName.equals("div")){
					String name = atts.getValue("class");
					if(name != null && name.equals("thread")){
						inNewThread = true;
						currentThread = new BoardThread(type);
					}					
				}
				else if(inNewThread && (!inGotThreadURL) && localName.equals("a")){
					String name = atts.getValue("href");
					if(name != null && name.contains("res")){
						String url = boardURL;
						String [] sub = name.split("res");
						url = url.replace(sub[0], "");
						currentThread.setOpenThreadURL(url + name);
						inGotThreadURL = true;
					}
					
				}
				else if(inNewThread && (!inThumbDiv) && localName.equals("div")){
					String name = atts.getValue("class");
					if(name != null && name.equals("post_thumb")){
						inThumbDiv = true;
					}
				}
				else if(inThumbDiv && (!inGotThumb) && localName.equals("img")){
					String name = atts.getValue("class");
					String src = atts.getValue("src");
					if(name != null && src != null && name.equals("thumb")){
						currentThread.setThumbImageURL(src);
						inGotThumb = true;
					}
				}
				if(inGotThumb &&(!inGotTitle) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.equals("subject")){
						inReachedTitle = true;
					}
				}
				if(inGotThumb && (!inGotAuthor) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.equals("postername")){
						inReachedAuthor = true;
					}
				}
				else if(inGotAuthor && (!inReachedText) && (!inGotText) && localName.equals("p")){
					if(currentThread.getThreadAuthor().length() < 1){
						currentThread.setThreadAuthor("Anonymous");
					}
					String name = atts.getValue("class");
					if(name != null && name.equals("message")){
						inReachedText = true;
						buff = new StringBuffer("");
					}
				}
				else if(inGotText && localName.equals("hr")){
					inNewThread = false;
					inGotThumb = false;
					inReachedAuthor = false;
					inGotAuthor = false;
					inReachedText = false;
					inGotText = false;
					inOpDescription = false;
					inGotThreadURL = false;
					currentThread.setBtype(this.type);
					m_threads.add(currentThread);
				}
			}
		}
		if(type.equals(BoardType.FOURCHAN)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					String att = atts.getValue("name");
					String att2 = atts.getValue("action");
					String att3 = atts.getValue("enctype");
					if(att != null && att2 != null && att3 != null && att.equals("post")){
						postingInfo.actionURL=att2;
						postingInfo.encodeType=att3;
						inReachedPostForm=true;
					}
				}
				else if(inReachedPostForm && (localName.equals("input") || localName.equals("textarea"))){
					String att = atts.getValue("name");
					String att2 = atts.getValue("value");
					String att3 = atts.getValue("type");
					if(att != null){
						if(att3 != null && att3.equals("file")){
							postingInfo.fileRequestName = att;
						}else{
							if(att2!=null){
								postingInfo.inputPairs.add(new BasicNameValuePair(att, att2));
							}else{
								postingInfo.inputPairs.add(new BasicNameValuePair(att, ""));
							}
						}
					}
				}else if(inReachedPostForm && localName.equals("iframe")){
					String att = atts.getValue("src");
					if(att != null){
						//att = att.replace("api.recaptcha.net", "www.google.com/recaptcha/api");
						att = "http:".concat(att);						
						postingInfo.captchaTestURL=att;
					}
				}
			}
			else{
				if(localName.equals("form") && !inForm){
					String name = atts.getValue("name");
						if(name != null && name.equals("delform")){
							inForm = true;
							m_threads = new ArrayList<BoardThread>();
						}
				}
				
				else if(inForm && localName.equals("td")){
					String classatt = atts.getValue("class");
					if(classatt != null && classatt.equals("deletebuttons")){
						inForm = false;
					}
				}
				
				else if(inForm && (!inNewThread) && (!inGotThumb) && localName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("filesize")){
						inNewThread = true;
						currentThread = new BoardThread(type);
					}
				}
				
				else if(inNewThread && localName.equals("img")){
						String att = atts.getValue("align");
						String att2 = atts.getValue("src");
						if(att != null && att2 != null && att.equals("left")){
							if(att2.contains("http")){
								currentThread.setThumbImageURL(att2);
							}
							else{
								String uurl = "http:".concat(att2);
								currentThread.setThumbImageURL(uurl);
							}
							inGotThumb = true;
							inNewThread = false;
						}
					
				}
				
				else if(inGotThumb && localName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("postername")){
						inReachedAuthor = true;
						buff = new StringBuffer("");
					}
					if(att != null && att.equals("filetitle")){
						inReachedTitle = true;
						buff = new StringBuffer("");
					}
				}
				
				else if(inGotAuthor && (!inGotThreadURL) && localName.equals("a")){
					String name = atts.getValue("class");
					String src = atts.getValue("href");
					if(name != null && src != null && name.equals("quotejs")){
						String [] temp = src.split("#");
						String [] tempURL = boardURL.split("/");
						String [] bollocks = temp[0].split("/");
						for(int i =0; i< tempURL.length; i++){
							for(int j = 0; j < bollocks.length; j++){
								if(tempURL[i].equals(bollocks[j])){
									temp[0]=temp[0].replace(tempURL[i], "");
								}
							}
						}
						
						currentThread.setOpenThreadURL(boardURL + temp[0]);
					}
				}
					
				else if(inGotAuthor && (!inGotText) && localName.equals("blockquote")){
					if(currentThread.getThreadAuthor().length() == 0){
						currentThread.setThreadAuthor("Anonymous");
					}
					inReachedText = true;
					buff = new StringBuffer("");
				}
				else if(inGotText && !inReachedPostCount && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.contains("omittedposts")){
						inReachedPostCount = true;
					}
				
				}
				else if(inGotText && localName.equals("hr")){
						inNewThread = false;
						inGotThumb = false;
						inReachedAuthor = false;
						inGotAuthor = false;
						inReachedText = false;
						inGotText = false;
						inOpDescription = false;
						inGotThreadURL = false;
						inReachedTitle = false;
						inGotTitle = false;
						inReachedPostCount = false;
						inGotPostCount = false;
						currentThread.setBtype(this.type);
						m_threads.add(currentThread);
				}
			}
		}
		else if(type.equals(BoardType.TWOCHAN)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					String att = atts.getValue("method");
					String att2 = atts.getValue("action");
					String att3 = atts.getValue("enctype");
					if(	att != null && att.equals("POST") && 
						att2 != null && att2.equals("futaba.php?guid=on") && 
						att3 != null && att3.equals("multipart/form-data")){
						postingInfo.actionURL=boardURL + att2;
						postingInfo.encodeType=att3;
						inReachedPostForm=true;
					}
				}
				else if(inReachedPostForm && (localName.equals("input") || localName.equals("textarea"))){
					String att = atts.getValue("name");
					String att2 = atts.getValue("value");
					String att3 = atts.getValue("type");
					if(att != null){
						if(att3 != null && att3.equals("upfile")){
							postingInfo.fileRequestName = att;
						}else{
							if(att2!=null){
								postingInfo.inputPairs.add(new BasicNameValuePair(att, att2));
							}else{
								postingInfo.inputPairs.add(new BasicNameValuePair(att, ""));
							}
						}
					}
				}
			}else{
				if(localName.equals("form") && !inForm){
					String name = atts.getValue("method");
						if(name != null && name.equals("POST")){
							inForm = true;
							m_threads = new ArrayList<BoardThread>();
						}
				}
				else if(inForm && !inNewThread &&localName.equals("small")){
					inNewThread = true;
					currentThread = new BoardThread(type);
				}
				else if(inNewThread && (!inGotThumb) && localName.equals("a")){
					String href = atts.getValue("href");
					if(href!=null && href.contains("http")){
						inThumbDiv = true;
					}
				}
				else if(inThumbDiv && (!inGotThumb) && localName.equals("img")){
					String src = atts.getValue("src");
					if(src != null && src.contains("http")){
						currentThread.setThumbImageURL(src);
						inGotThumb = true;
						inThumbDiv = false;
					}
				}
				else if(!inReachedPreTitle && localName.equals("font")){
					String color = atts.getValue("color");
					if(color != null && color.contains("cc1105")){
						inReachedPreTitle=true;
					}
				}
				else if(inReachedPreTitle && !inGotTitle && localName.equals("b")){
					inReachedTitle = true;
				}
				else if(!inReachedPreAuthor && inGotTitle && localName.equals("font")){
					String color = atts.getValue("color");
					if(color != null && color.contains("117743")){
						inReachedPreAuthor=true;
					}
				}
				else if(inReachedPreAuthor && (!inGotAuthor) && localName.equals("b")){
					inReachedAuthor = true;
				}
				else if(!inGotThreadURL && localName.equals("a")){
					String href = atts.getValue("href");
					if(href!=null && href.contains("res")){
						currentThread.setOpenThreadURL(boardURL+href);
						inGotThreadURL = true;
					}
				}
				else if(inGotThreadURL && !inReachedText && localName.equals("blockquote")){
					inReachedText = true;
					buff = new StringBuffer();
				}
				else if(inGotText && localName.equals("hr")){
					inNewThread = false;
					inGotThumb = false;
					inReachedPreAuthor = false;
					inReachedAuthor = false;
					inGotAuthor = false;
					inReachedText = false;
					inGotText = false;
					inReachedThreadUrl = false;
					inGotThreadURL = false;
					inReachedPreTitle = false;
					inReachedTitle = false;
					inGotTitle = false;
					inThumbDiv = false;
					currentThread.setBtype(this.type);
					m_threads.add(currentThread);
				}
			}
		}else if(type.equals(BoardType.WAKABA)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					String action = atts.getValue("action");
					String enctype = atts.getValue("enctype");
					String id = atts.getValue("id");
					if(id!=null && id.equals("postform") && action!=null && enctype!=null){
						postingInfo.encodeType = enctype;
						String baseURL = "";
						try {
							baseURL = "http://"+(new URL(this.boardURL).getHost());
						} catch (MalformedURLException e) {
						}
						postingInfo.actionURL = baseURL + action;
						inReachedPostForm = true;
					}
				}
				else if(inReachedPostForm && (localName.equals("input") || localName.equals("textarea"))){
					String name = atts.getValue("name");
					String value = atts.getValue("value");
					String type = atts.getValue("type");
					
					if(name != null){
						if(type != null && type.equals("file")){
							postingInfo.fileRequestName=name;
						}
						else if(value != null){
							postingInfo.inputPairs.add(new BasicNameValuePair(name, value));
						}else{
							postingInfo.inputPairs.add(new BasicNameValuePair(name, ""));
						}
					}
				}
			}else{
				if(!inForm && !inNewThread && localName.equals("form")){
					String id = atts.getValue("id");
					if(id != null && id.equals("delform")){
						inForm = true;
						m_threads = new ArrayList<BoardThread>();
					}
				}
				else if (inForm && !inNewThread && !inGotAuthor && localName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("filesize")){
						inNewThread = true;
						currentThread = new BoardThread(type);
					}
				}
				else if(inNewThread && !inGotThumb && localName.equals("img")){
					String url = atts.getValue("src");
					String target = atts.getValue("class");
					if(target!= null && target.equals("thumb") && url != null){
						String baseURL = "";
						try {
							baseURL = "http://"+(new URL(this.boardURL).getHost());
						} catch (MalformedURLException e) {
						}
						currentThread.setThumbImageURL(baseURL+ url);
						inGotThumb = true;
					}
				}
				else if(inGotThumb && (!inGotAuthor || !inGotTitle || !inReachedThreadUrl) && localName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && !inReachedTitle && att.contains("title")){
						inReachedTitle = true;
					}
					else if(att != null && !inReachedAuthor && att.contains("name")){
						inReachedAuthor = true;
					}
					else if(att!=null && att.equals("reflink")){
						inReachedThreadUrl = true;
					}
				}
				else if(inReachedThreadUrl && !inGotThreadURL && localName.equals("a")){
					String href = atts.getValue("href");
					if(href!= null){
						href = href.substring(0, href.indexOf("#"));
						
						String baseURL = "";
						try {
							baseURL = "http://"+(new URL(this.boardURL).getHost());
						} catch (MalformedURLException e) {
						}
						currentThread.setOpenThreadURL(baseURL+ href);
						inGotThreadURL = true;
					}
				}
				else if(inGotThreadURL && !inReachedText && !inGotText && localName.equals("blockquote")){
					inReachedText = true;
					buff = new StringBuffer();
				}
			}
		}else if(type.equals(BoardType.KUSABAX)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					String action = atts.getValue("action");
					String enctype = atts.getValue("enctype");
					String id = atts.getValue("id");
					if(id!=null && id.equals("postform") && action!=null && enctype!=null){
						postingInfo.encodeType = enctype;
						postingInfo.actionURL = action;
						inReachedPostForm = true;
					}
				}
				else if(inReachedPostForm && (localName.equals("input") || localName.equals("textarea"))){
					String name = atts.getValue("name");
					String value = atts.getValue("value");
					String type = atts.getValue("type");
					
					if(name != null){
						if(type != null && type.equals("file")){
							postingInfo.fileRequestName=name;
						}
						else if(value != null){
							postingInfo.inputPairs.add(new BasicNameValuePair(name, value));
						}else{
							postingInfo.inputPairs.add(new BasicNameValuePair(name, ""));
						}
					}
				}	
			}else{ 
				if(localName.equals("form")){
					String name = atts.getValue("id");
					if(name != null && name.equals("delform")){
						inForm = true;
						m_threads = new ArrayList<BoardThread>();
					}
				}
				else if(inForm && (!inNewThread) && (!inGotThumb)&& localName.equals("span")){
					String name = atts.getValue("id");
					if(name != null && name.contains("thread")){
						inNewThread = true;
						currentThread = new BoardThread(type);
					}					
				}
				else if(inNewThread && (!inGotThreadURL) && localName.equals("a")){
					String name = atts.getValue("href");
					if(name != null && name.contains("res")){
						String url = boardURL;
						String [] sub = name.split("res");
						url = url.replace(sub[0], "");
						currentThread.setOpenThreadURL(url + name);
						inGotThreadURL = true;
					}
					
				}
				else if(inNewThread && (!inThumbDiv) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.equals("filesize")){
						inThumbDiv = true;
					}
				}
				else if(inNewThread && inThumbDiv && (!inGotThumb) && localName.equals("img")){
					String name = atts.getValue("class");
					String src = atts.getValue("src");
					if(name != null && src != null && name.equals("thumb")){
						if(src.contains("http")){
						currentThread.setThumbImageURL(src);
						}
						else{
							String baseURL = "";
							try {
								baseURL = "http://"+(new URL(this.boardURL).getHost());
							} catch (MalformedURLException e) {
							}
							currentThread.setThumbImageURL(baseURL+ src);
						}
						inGotThumb = true;
					}
				}
				if(inNewThread && inGotThumb &&(!inGotTitle) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.contains("title")){
						inReachedTitle = true;
					}
				}
				if(inNewThread && inGotThumb && (!inGotAuthor) && localName.equals("span")){
					String name = atts.getValue("class");
					if(name != null && name.contains("name")){
						inReachedAuthor = true;
					}
				}
				else if(inNewThread && inGotAuthor && (!inReachedText) && (!inGotText) && localName.equals("blockquote")){
					if(currentThread.getThreadAuthor().length() < 1){
						currentThread.setThreadAuthor("Anonymous");
					}
						inReachedText = true;
						buff = new StringBuffer("");
				}

			}
			
		}
		else if(type.equals(BoardType.FOURARCHIVE)){
			if(!inGotPostInfo){
				postingInfo.actionURL="";
				postingInfo.captchaImageURL="";
				postingInfo.captchaTestURL="";
				postingInfo.encodeType="";
				postingInfo.fileRequestName="";
				m_threads = new ArrayList<BoardThread>();
				inGotPostInfo = true;
			}
			else if(!inGotFirstPost && localName.equals("span") && !inNewThread){
				String att = atts.getValue("class");
				if(att!=null && att.equals("filesize")){
					inNewThread = true;
					currentThread = new BoardThread(type);
				}
			}
			else if(inGotFirstPost && localName.equals("hr") && !inNewThread){
				inNewThread = true;
				currentThread = new BoardThread(type);
			}
			if(inNewThread){
				if(!inGotThumb && localName.equals("img")){
					String att = atts.getValue("src");
					if(att!=null && att.contains("http")){
						currentThread.setThumbImageURL(att);
						inGotThumb = true;
					}
				}
				else if(localName.equals("span")){
					String att = atts.getValue("class");
					if(att!=null){
						if(!inReachedAuthor && att.contains("name")){
							inReachedAuthor = true;
						}
						else if(!inReachedTitle && att.contains("omittedposts")){
							inReachedTitle = true;
							buff = new StringBuffer();
						}
					}
				}
				else if(inGotAuthor && !inGotThreadURL){
					if(localName.equals("a")){
						String att = atts.getValue("href");
						if(att != null && att.contains("http") && !( att.contains("jpg") || att.contains("gif") || att.contains("png") || att.contains("bmp") || att.contains("jpeg"))){
							currentThread.setOpenThreadURL(att);
							inGotThreadURL = true;
							
						}
					}
				}
				else if(inGotThreadURL && !inReachedText && localName.equals("blockquote")){
					inReachedText = true;
					buff = new StringBuffer();
				}
			}
		}
	}
	
	@Override
    public void characters(char ch[], int start, int length) {
		
		if(type.equals(BoardType.FOURTWENTYCHAN)){
			if(inReachedTitle && (!inGotTitle)){
				currentThread.setPostTitle(new String(ch, start, length));
				inGotTitle = true;
			}
			if(inReachedAuthor && (!inGotAuthor)){
				currentThread.setThreadAuthor(new String(ch, start, length));
				inGotAuthor = true;
			}
			if(inReachedText && (!inGotText)){
				buff.append(new String(ch, start, length));
			}
			if(inReachedPostCount)
			{
				//String te = new String(ch,start,length);
				String[] te = new String(ch,start,length).split(" ");
				////Log.e("fuck",te[0]);
				for(int i = 0; i < te.length-1; i++)
				{
					if(te[i+1].contains("post")){
						currentThread.setNumPosts(5+Integer.parseInt(te[i]));
					}
					if(te[i+1].contains("image")){
						currentThread.setNumImages(5+Integer.parseInt(te[i]));
					}
				}
				inGotPostCount=true;
			}
		}
	
		else if(type.equals(BoardType.SEVENCHAN)){
			if(inReachedAuthor && (!inGotAuthor)){
				if(length > 0){
				currentThread.setThreadAuthor(new String(ch, start, length));
				}
				inGotAuthor = true;
			}
			else if(inReachedTitle && (!inGotTitle)){
				if(length>0){
					currentThread.setPostTitle(new String(ch,start,length).trim());
				
				}
				inGotTitle=true;
			}
			else if(inReachedText && (!inGotText)){
				buff.append(new String(ch, start, length));
			}
		}
		
		else if(type.equals(BoardType.FOURCHAN)|| type.equals(BoardType.NEWFOURCHAN) || type.equals(BoardType.NEWFOURCHANv2))
		{
		
			if(inReachedAuthor && !inGotAuthor){
				buff.append(new String(ch,start,length));
				currentThread.setThreadAuthor(new String(ch, start, length));
				//Log.e("NEWFOURCHAN", "reading author: "+buff);
			}
			if(inReachedText && !inGotText){
				buff.append(new String(ch, start, length));
				//Log.e("NEWFOURCHAN", "reading text: "+buff);
			}
			if(inReachedTitle && !inGotTitle){
				buff.append(new String(ch,start,length));
				currentThread.setPostTitle(new String(ch, start, length));
				//Log.e("NEWFOURCHAN", "reading post title: "+buff);
			}
			if(type.equals(BoardType.FOURCHAN) && inGotText && !inGotPostCount)
			{
				//String te = new String(ch,start,length);
				String[] te = new String(ch,start,length).split(" ");
				for(int i = 0; i < te.length-1; i++)
				{
					if(te[i+1].contains("post")){
						currentThread.setNumPosts(5+Integer.parseInt(te[i]));
					}
					if(te[i+1].contains("image")){
						currentThread.setNumImages(5+Integer.parseInt(te[i]));
					}
				}
				inGotPostCount=true;
				//Log.e("NEWFOURCHAN", "reading info: "+currentThread.getNumPosts()+" , "+currentThread.getNumImages());
			}
			if((type.equals(BoardType.NEWFOURCHAN) ||  type.equals(BoardType.NEWFOURCHANv2) )&& inGotText && inReachedPostCount && !inGotPostCount)
			{
				String[] te = new String(ch,start,length).split(" ");
				for(int i = 0; i < te.length-1; i++)
				{
					if(te[i+1].contains("post")){
						currentThread.setNumPosts(5+Integer.parseInt(te[i]));
					}
					if(te[i+1].contains("image")){
						currentThread.setNumImages(5+Integer.parseInt(te[i]));
					}
				}
				inGotPostCount=true;
				//Log.e("NEWFOURCHAN", "reading info: "+currentThread.getNumPosts()+" , "+currentThread.getNumImages());
			}
		}
		else if(type.equals(BoardType.TWOCHAN)){
			if(inReachedAuthor && !inGotAuthor){
				currentThread.setThreadAuthor(new String(ch,start,length));
				inGotAuthor=true;
			}
			else if(inReachedTitle && !inGotTitle){
				if(length>0){
					currentThread.setPostTitle(new String(ch,start,length).trim());
				
				}
				inGotTitle = true;
			}
			else if(inReachedText && !inGotText){
				buff.append(new String(ch,start,length));
			}
		}
		else if(type.equals(BoardType.WAKABA)){
			if(inReachedAuthor && !inGotAuthor){
				currentThread.setThreadAuthor(new String(ch,start,length));
				inGotAuthor = true;
			}
			else if(inReachedTitle && !inGotTitle){
				if(length>0){
					currentThread.setPostTitle(new String(ch,start,length).trim());
				
				}
				inGotTitle = true;
			}
			else if(inReachedText && !inGotText){
				buff.append(new String(ch, start, length));
			}
		}
		else if(type.equals(BoardType.KUSABAX)){
			if(inReachedAuthor && (!inGotAuthor)){
				if(length > 0){
				currentThread.setThreadAuthor(new String(ch, start, length));
				}
				inGotAuthor = true;
			}
			else if(inReachedTitle && (!inGotTitle)){
				if(length>0){
					currentThread.setPostTitle(new String(ch,start,length).trim());
				
				}
				inGotTitle=true;
			}
			else if(inReachedText && (!inGotText)){
				buff.append(new String(ch, start, length));
			}
		}
		else if(type.equals(BoardType.FOURARCHIVE)){
			if(inNewThread && inReachedAuthor && (!inGotAuthor)){
				if(length > 0){
				currentThread.setThreadAuthor(new String(ch, start, length));
				}
				inGotAuthor = true;
			}
			else if(inNewThread && inReachedTitle && !inGotTitle){
				buff.append(new String(ch, start, length));
			}
			else if(inNewThread && inReachedText && !inGotText){
				buff.append(new String(ch, start, length));
			}
		}
		
    }
	
	@Override
	public void endElement(String namespaceURI, String localName, String qName){
		if(inReachedText && localName.equals("br")){
			buff.append("\n");
		}
		
		if(type.equals(BoardType.FOURTWENTYCHAN)){
			if(!inGotPostInfo && inReachedPostForm){
				if(localName.equals("form")){
					inGotPostInfo=true;
				}
			}
			else{
				if(inThumbDiv && localName.equals("div")){
					inThumbDiv = false;
				}
				if(inReachedText && localName.equals("blockquote")){
					inGotText = true;
					String temp = buff.toString();
					currentThread.setPostText(StringEscapeUtils.unescapeHtml(temp));
				}
				if(localName.equals("form") && inForm){
					inForm = false;
					////Log.e("Handler","Ended Form");
				}
			}
		}	
		else if(type.equals(BoardType.SEVENCHAN)){
			if(inReachedPostForm && !inGotPostInfo){
				if(localName.equals("form")){
					inGotPostInfo=true;
				}
			}
			else{
				if(inThumbDiv && localName.equals("div")){
					inThumbDiv = false;
				}
				if(inReachedText && localName.equals("p")){
					inGotText = true;
					String temp = buff.toString();
					currentThread.setPostText(StringEscapeUtils.unescapeHtml(temp));
				}
				if(localName.equals("form") && inForm){
					inForm = false;
					////Log.e("Handler","Ended Form");
				}
			}
		}
		
		else if(type.equals(BoardType.FOURCHAN)|| type.equals(BoardType.NEWFOURCHAN) || type.equals(BoardType.NEWFOURCHANv2))
		{
			if(!inGotPostInfo){
				if(inReachedPostForm && localName.equals("form")){
					inGotPostInfo = true;
				}
			}
			else{
				if(localName.equals("body")){
					////Log.e("PARSING", "phew");
				}
	
				if(localName.equals("span"))
				{
					if(inReachedTitle){
						inGotTitle=true;
						String temp = buff.toString();
						currentThread.setPostTitle(StringEscapeUtils.unescapeHtml(temp));
						inReachedTitle=false;
						//Log.e("NEWFOURCHAN", "setting post title: "+currentThread.getPostTitle());
					}
					if(inReachedAuthor){
						inGotAuthor = true;
						String temp = buff.toString();
						temp = StringEscapeUtils.unescapeHtml(temp);
						temp = temp.trim();
						////Log.e("BOARDLISTHANDLER","GOT AUTHOR: "+temp);
						currentThread.setThreadAuthor(temp);
						inReachedAuthor=false;
						//Log.e("NEWFOURCHAN", "setting post author: "+currentThread.getThreadAuthor());
					}
				}
			
				if(inReachedText && localName.equals("blockquote")){
					inGotText = true;
					String temp = buff.toString();
					currentThread.setPostText(StringEscapeUtils.unescapeHtml(temp));
					//Log.e("NEWFOURCHAN", "setting post text: "+currentThread.getPostText());
	
				}
				
				
				
			}
		}else if(type.equals(BoardType.TWOCHAN)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					inGotPostInfo = true;
				}
			}else{
				if(inReachedText && !inGotText && localName.equals("blockquote")){
					inGotText = true;
					String temp = buff.toString();
					currentThread.setPostText(StringEscapeUtils.unescapeHtml(temp));
				}
				
			}
		}else if(type.equals(BoardType.WAKABA)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					inGotPostInfo = true;
				}
			}
			else{
				if(inReachedText && !inGotText && localName.equals("blockquote")){
					inGotText = true;
					String temp = buff.toString();
					currentThread.setPostText(StringEscapeUtils.unescapeHtml(temp));
					////Log.e("WAKABA","got text");
				}
				
				if(inGotText && localName.equals("hr")){
					inNewThread = false;
					inGotThumb = false;
					inReachedPreAuthor = false;
					inReachedAuthor = false;
					inGotAuthor = false;
					inReachedText = false;
					inGotText = false;
					inReachedThreadUrl = false;
					inGotThreadURL = false;
					inReachedPreTitle = false;
					inReachedTitle = false;
					inGotTitle = false;
					inThumbDiv = false;
					currentThread.setBtype(this.type);
					m_threads.add(currentThread);
					////Log.e("WAKABA","added thread");
				}
			}
			
		}
		else if(type.equals(BoardType.KUSABAX)){
			if(inReachedPostForm && !inGotPostInfo){
				if(localName.equals("form")){
					inGotPostInfo=true;
				}
			}
			else{
				if(inThumbDiv && localName.equals("div")){
					inThumbDiv = false;
				}
				if(localName.equals("form") && inForm){
					inForm = false;
					////Log.e("Handler","Ended Form");
				}
				if(inReachedText && localName.equals("blockquote")){
					inGotText = true;
					String temp = buff.toString();
					currentThread.setPostText(StringEscapeUtils.unescapeHtml(temp));
					inNewThread = false;
					inGotThumb = false;
					inReachedAuthor = false;
					inGotAuthor = false;
					inReachedText = false;
					inGotText = false;
					inOpDescription = false;
					inGotThreadURL = false;
					////Log.e("Thread", currentThread.getThumbImageUrl());
					currentThread.setBtype(this.type);
					m_threads.add(currentThread);
					////Log.e("KUSABAX","addedthread");
				}
			}
		}
		else if(type.equals(BoardType.FOURARCHIVE)){
			if((inReachedTitle || inReachedText) && localName.equals("br")){
				if(buff != null){
					buff.append("\n");
				}
				////Log.e("4chanarchive","appended newline");
			}
			if(inReachedText && localName.equals("blockquote")){
				String temp = buff.toString();
				currentThread.setPostText(temp);
				inGotText=true;
				////Log.e("4chanarchive","finished text");
			}
			if(inReachedTitle && localName.equals("span")){
				String temp = buff.toString();
				currentThread.setPostTitle(temp);
				inGotTitle = false;
				inNewThread = false;
				inGotThumb = false;
				inReachedAuthor = false;
				inGotAuthor = false;
				inReachedText = false;
				inReachedTitle = false;
				inGotText = false;
				inOpDescription = false;
				inGotThreadURL = false;
				inGotFirstPost = true;
				////Log.e("Thread", currentThread.getThumbImageUrl());
				currentThread.setBtype(this.type);
				m_threads.add(currentThread);
				////Log.e("4chanarchive","addedthread");
			}

		}
	}
}
