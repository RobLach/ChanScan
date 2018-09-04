package hexdojo.android.chanscanlite;

import hexdojo.android.chanscanlite.ChanScan.BoardType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//import android.util.Log;


public class ThreadListHandler extends DefaultHandler{
	
	private ArrayList<ThreadItem> m_comments;
	private BoardType m_boardType;
	private ThreadItem currentItem;
	private PostingInfo postingInfo;
	

	//Flags
	Boolean gotFirstPost = false;
	Boolean inHitFullImage = false;
	Boolean inGotFullImage = false;
	Boolean inReachedImageDiv = false;
	Boolean inGotThumb = false;
	Boolean inReachedAuthor = false;
	Boolean inReachedNewComment = false;
	Boolean inGotAuthor = false;
	Boolean inReachedPostText = false;
	Boolean inGotPostText = false;
	Boolean inReachedPostTitle = false;
	Boolean inGotPostTitle = false;
	Boolean inReachedPostDate = false;
	Boolean inGotPostDate = false;
	Boolean inReachedPreTitle = false;
	Boolean inReachedPreAuthor = false;
	
	//PostInfo Flags
	Boolean inGotPostInfo = false;
	Boolean inReachedPostForm = false;
	
	StringBuffer buff = null;
	String pos = null;
	String threadURL;
	
	public ArrayList<ThreadItem> getM_comments() {
		return m_comments;
	}

	public void setM_comments(ArrayList<ThreadItem> mComments) {
		m_comments = mComments;
	}
	
	public PostingInfo getPostingInfo() {
		return postingInfo;
	}

	public void setPostingInfo(PostingInfo postingInfo) {
		this.postingInfo = postingInfo;
	}

	public ThreadListHandler(ArrayList<ThreadItem> comments, BoardType bType, PostingInfo pInfo, String threadURL){
		this.m_comments = comments;
		this.m_boardType = bType;
		this.postingInfo = pInfo;		
		this.threadURL = threadURL;
		//Log.e("THREADLISTHANDLER", "CREATED");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if(m_boardType == BoardType.NEWFOURCHANv2){
			//Log.e("Getting", "Qname: " +qName+ " Localname: "+localName);
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
				else if(inReachedPostForm && qName.equals("img")){
					String att = atts.getValue("src");
					if(att != null){
						//att = att.replace("api.recaptcha.net", "www.google.com/recaptcha/api");
						//att = "http:".concat(att);	
						postingInfo.captchaTestURL=att;
					}
				}
			}
			else if(!gotFirstPost && inGotPostInfo){
				if(qName.equals("form")){
					String att = atts.getValue("name");
					if(att != null && att.equals("delform")){
						currentItem = new ThreadItem();
					}
				}
				else if((!inHitFullImage) && qName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("fileText")){
						inHitFullImage = true;
					}
				}
				else if(inHitFullImage && (!inGotFullImage) && qName.equals("a")){
					String att = atts.getValue("href");
					if(att != null){
						inGotFullImage = true;
						if(att.contains("http")){
							currentItem.setImageURL(att);
						}
						else{
							currentItem.setImageURL("http:".concat(att));
						}
					}
				}
				else if(inGotFullImage && (!inGotThumb) && qName.equals("img")){
					String att = atts.getValue("src");
					if(att != null){
						inGotThumb = true;
						if(att.contains("http")){
							currentItem.setThumbURL(att);
						}
						else{
							currentItem.setThumbURL("http:".concat(att));
						}
					}
				}
				else if(qName.equals("input")){
					String att = atts.getValue("name");
					String att2 = atts.getValue("type");
					if(att != null && att2.contains("checkbox")){
						currentItem.setCommentID(att);
					}
				}
				else if(inGotThumb && (!inReachedAuthor) && qName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("name")){
						buff = new StringBuffer("");
						inReachedAuthor = true;
					}
				}
				else if(inGotAuthor && (!inReachedPostText) && qName.equals("blockquote")){
					inReachedPostText = true;
					buff = new StringBuffer("");
				}
			}else if(inGotPostInfo){
				if((!inReachedNewComment) && qName.equals("div")){
					String att = atts.getValue("class");
					if(att != null && att.contains("postContainer")){
						inReachedNewComment = true;
						currentItem = new ThreadItem();
					}
				}
				else if(qName.equals("input")){
					String att = atts.getValue("name");
					String att2 = atts.getValue("type");
					String att3 = atts.getValue("value");
					if(att != null && att2.contains("checkbox") && att3.contains("delete")){
						currentItem.setCommentID(att);
					}
				}
				else if(inReachedNewComment && (!inGotAuthor) && qName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("name")){
						buff = new StringBuffer("");
						inReachedAuthor = true;
					}
				}
				else if(inGotAuthor && (!inHitFullImage) && qName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("fileText")){
						inHitFullImage = true;
					}
				}
				if(inHitFullImage && (!inGotFullImage) && qName.equals("a")){
					String att = atts.getValue("href");
					if(att != null && att.contains("jpg") || att.contains("gif") || att.contains("png")){
						inGotFullImage = true;
						if(att.contains("http")){
							currentItem.setImageURL(att);
						}
						else{
							currentItem.setImageURL("http:".concat(att));
						}
					}
				}
				if(inGotFullImage && (!inGotThumb) && qName.equals("img")){
					String att = atts.getValue("src");
					if(att != null && att.contains("jpg") || att.contains("gif") || att.contains("png")){
						inGotThumb = true;
						if(att.contains("http")){
							currentItem.setThumbURL(att);
						}
						else{
							currentItem.setThumbURL("http:".concat(att));
						}
					}
				}

				if(inGotAuthor && (!inReachedPostText) && qName.equals("blockquote")){
					inReachedPostText = true;
					buff = new StringBuffer("");
				}
			}
		}	
		else if(m_boardType == BoardType.NEWFOURCHAN){
			//Log.e("Getting", "Qname: " +qName+ " Localname: "+localName);
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
			else if(!gotFirstPost && inGotPostInfo){
				if(qName.equals("form")){
					String att = atts.getValue("name");
					if(att != null && att.equals("delform")){
						currentItem = new ThreadItem();
					}
				}
				else if((!inHitFullImage) && qName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("fileText")){
						inHitFullImage = true;
					}
				}
				else if(inHitFullImage && (!inGotFullImage) && qName.equals("a")){
					String att = atts.getValue("href");
					if(att != null){
						inGotFullImage = true;
						if(att.contains("http")){
							currentItem.setImageURL(att);
						}
						else{
							currentItem.setImageURL("http:".concat(att));
						}
					}
				}
				else if(inGotFullImage && (!inGotThumb) && qName.equals("img")){
					String att = atts.getValue("src");
					if(att != null){
						inGotThumb = true;
						if(att.contains("http")){
							currentItem.setThumbURL(att);
						}
						else{
							currentItem.setThumbURL("http:".concat(att));
						}
					}
				}
				else if(qName.equals("input")){
					String att = atts.getValue("name");
					String att2 = atts.getValue("type");
					if(att != null && att2.contains("checkbox")){
						currentItem.setCommentID(att);
					}
					
				}
				else if(inGotThumb && (!inReachedAuthor) && qName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("name")){
						buff = new StringBuffer("");
						inReachedAuthor = true;
					}
				}
				else if(inGotAuthor && (!inReachedPostText) && qName.equals("blockquote")){
					inReachedPostText = true;
					buff = new StringBuffer("");
				}
			}else if(inGotPostInfo){
				if((!inReachedNewComment) && qName.equals("div")){
					String att = atts.getValue("class");
					if(att != null && att.contains("postContainer")){
						inReachedNewComment = true;
						currentItem = new ThreadItem();
					}
				}
				else if(qName.equals("input")){
					String att = atts.getValue("name");
					String att2 = atts.getValue("type");
					String att3 = atts.getValue("value");
					if(att != null && att2.contains("checkbox") && att3.contains("delete")){
						currentItem.setCommentID(att);
					}
				}
				else if(inReachedNewComment && (!inGotAuthor) && qName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("name")){
						buff = new StringBuffer("");
						inReachedAuthor = true;
					}
				}
				else if(inGotAuthor && (!inHitFullImage) && qName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("fileText")){
						inHitFullImage = true;
					}
				}
				if(inHitFullImage && (!inGotFullImage) && qName.equals("a")){
					String att = atts.getValue("href");
					if(att != null && att.contains("jpg") || att.contains("gif") || att.contains("png")){
						inGotFullImage = true;
						if(att.contains("http")){
							currentItem.setImageURL(att);
						}
						else{
							currentItem.setImageURL("http:".concat(att));
						}
					}
				}
				if(inGotFullImage && (!inGotThumb) && qName.equals("img")){
					String att = atts.getValue("src");
					if(att != null && att.contains("jpg") || att.contains("gif") || att.contains("png")){
						inGotThumb = true;
						if(att.contains("http")){
							currentItem.setThumbURL(att);
						}
						else{
							currentItem.setThumbURL("http:".concat(att));
						}
					}
				}

				if(inGotAuthor && (!inReachedPostText) && qName.equals("blockquote")){
					inReachedPostText = true;
					buff = new StringBuffer("");
				}
			}
		}	
	else if(m_boardType.equals(BoardType.SEVENCHAN)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					String action = atts.getValue("action");
					String enctype = atts.getValue("enctype");
					String id = atts.getValue("id");
					if(id!=null && id.equals("posting_form") && enctype!=null && action!=null){
						postingInfo.actionURL = action;
						postingInfo.encodeType = enctype;
						inReachedPostForm = true;
						//Log.e("7ChanHandler","Hit Posting Form");
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
					//Log.e("7ChanHandler","HitValuePair");
				}
			}else{
				if(!gotFirstPost){
				//	Log.e("7ChanHandler","Getting first post");
					if(localName.equals("form")){
						String name = atts.getValue("id");
						if(name != null && name.equals("delform")){
							currentItem = new ThreadItem();
							inReachedNewComment = true;
						//	Log.e("7ChanHandler","Hit New Comment");
						}
					}else if(inReachedNewComment && localName.equals("div")){
						String att = atts.getValue("class");
						if(att != null && att.equals("post_thumb")){
							inHitFullImage = true;
						//	Log.e("7ChanHandler","Hit Full Image");
						}
					}else if(inHitFullImage && (!inGotFullImage) && localName.equals("a")){
						String href = atts.getValue("href");
						if(href!=null && href.contains("http")){
							currentItem.setImageURL(href);
							inGotFullImage=true;
						//	Log.e("7ChanHandler","Got Full Image");
						}
					}else if(inGotFullImage && (!inGotThumb) && localName.equals("img")){
						String src = atts.getValue("src");
						String att = atts.getValue("class");
						if(src!= null && att != null && att.equals("thumb")){
							currentItem.setThumbURL(src);
							inGotThumb=true;
						//	Log.e("7ChanHandler","Got Thumb");
						}
						
					}else if(inGotThumb && (localName.equals("input"))){
						String type = atts.getValue("type");
						String value = atts.getValue("value");
						if(type!=null && value != null && type.equals("checkbox")){
							currentItem.setCommentID(value);
						//	Log.e("7ChanHandler","Got comment id");
						}
					}else if(inGotThumb && (!inReachedAuthor) && localName.equals("span")){
						String att = atts.getValue("class");
						if(att!=null && att.equals("postername")){
							inReachedAuthor = true;
						//	Log.e("7ChanHandler","Reached Author");
						}
					}else if((!inReachedPostText) && localName.equals("p")){
						String att = atts.getValue("class");
						if(att != null && att.equals("message")){
							inReachedPostText = true;
							buff = new StringBuffer();
						//	Log.e("7ChanHandler","Hit Start of Message");
						}
					}
				}else{
					if((!inReachedNewComment) && localName.equals("div")){
						String name = atts.getValue("class");
						if(name != null && name.equals("post")){
							currentItem = new ThreadItem();
							inReachedNewComment = true;
						//	Log.e("7ChanHandler","Hit New Comment");
						}
					}else if(inReachedNewComment && (localName.equals("input"))){
						String type = atts.getValue("type");
						String value = atts.getValue("value");
						if(type!=null && value != null && type.equals("checkbox")){
							currentItem.setCommentID(value);
						//	Log.e("7ChanHandler","Got comment id");
						}
					}else if((!inReachedAuthor) && (!inGotAuthor)&& localName.equals("span")){
						String att = atts.getValue("class");
						if(att!=null && att.equals("postername")){
							inReachedAuthor = true;
						//	Log.e("7ChanHandler","Reached Author");
						}
					}else if((!inHitFullImage) && localName.equals("div")){
						String att = atts.getValue("class");
						if(att != null && att.equals("post_thumb")){
							inHitFullImage = true;
						//	Log.e("7ChanHandler","Hit Full Image");
						}
					}else if(inHitFullImage && (!inGotFullImage) && localName.equals("a")){
						String href = atts.getValue("href");
						if(href!=null && href.contains("http")){
							currentItem.setImageURL(href);
							inGotFullImage=true;
						//	Log.e("7ChanHandler","Got Full Image");
						}
					}else if(inGotFullImage && (!inGotThumb) && localName.equals("img")){
						String src = atts.getValue("src");
						String att = atts.getValue("class");
						if(src!= null && att != null && att.equals("thumb")){
							currentItem.setThumbURL(src);
							inGotThumb=true;
						//	Log.e("7ChanHandler","Got Thumb");
						}
					}else if((!inReachedPostText) && localName.equals("p")){
						String att = atts.getValue("class");
						if(att != null && att.equals("message")){
							inReachedPostText = true;
							buff = new StringBuffer();
						//	Log.e("7ChanHandler","Hit Start of Message");
						}
					}
				}
			}
		}
		else if(m_boardType == BoardType.FOURCHAN){
			//Log.e("Getting", "Qname: " +qName+ " Localname: "+localName);
			if(!inGotPostInfo){
				if(qName.equals("form")){
					String att = atts.getValue("name");
					String att2 = atts.getValue("action");
					String att3 = atts.getValue("enctype");
					//Log.e("Getting", "Atts " + att + " " + att2 + " " + att3);
					if(att != null && att2 != null && att3 != null && att.equals("post")){
						postingInfo.actionURL=att2;
						postingInfo.encodeType=att3;
						inReachedPostForm=true;
					}
				}
				else if(inReachedPostForm && (qName.equals("input") || qName.equals("textarea"))){
					String att = atts.getValue("name");
					String att2 = atts.getValue("value");
					String att3 = atts.getValue("type");
					//Log.e("Getting", "Atts " + att + " " + att2 + " " + att3);
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
				}else if(inReachedPostForm && qName.equals("iframe")){
					String att = atts.getValue("src");
					if(att != null){
						//att = att.replace("api.recaptcha.net", "www.google.com/recaptcha/api");
						att = "http:".concat(att);	
						postingInfo.captchaTestURL=att;
					}
				}
			}
			else if(!gotFirstPost && inGotPostInfo){
				if(qName.equals("form")){
					String att = atts.getValue("name");
					if(att != null && att.equals("delform")){
						currentItem = new ThreadItem();
					}
				}
				else if((!inHitFullImage) && qName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("filesize")){
						inHitFullImage = true;
					}
				}
				else if(inHitFullImage && (!inGotFullImage) && qName.equals("a")){
					String att = atts.getValue("href");
					if(att != null){
						inGotFullImage = true;
						if(att.contains("http")){
							currentItem.setImageURL(att);
						}
						else{
							currentItem.setImageURL("http:".concat(att));
						}
					}
				}
				else if(inGotFullImage && (!inGotThumb) && qName.equals("img")){
					String att = atts.getValue("src");
					if(att != null){
						inGotThumb = true;
						if(att.contains("http")){
							currentItem.setThumbURL(att);
						}
						else{
							currentItem.setThumbURL("http:".concat(att));
						}
					}
				}
				else if(inGotThumb && (!inReachedAuthor) && qName.equals("input")){
					String att = atts.getValue("name");
					if(att != null){
						currentItem.setCommentID(att);
					}
					
				}
				else if(inGotThumb && (!inReachedAuthor) && qName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("postername")){
						inReachedAuthor = true;
					}
				}
				else if(inGotAuthor && (!inReachedPostText) && qName.equals("blockquote")){
					inReachedPostText = true;
					buff = new StringBuffer("");
				}
			}else if(inGotPostInfo){
				if((!inReachedNewComment) && qName.equals("td")){
					String att = atts.getValue("nowrap");
					if(att != null && att.equals("nowrap")){
						inReachedNewComment = true;
						currentItem = new ThreadItem();
					}
				}
				else if(inReachedNewComment && (!inReachedAuthor) && qName.equals("input")){
					String att = atts.getValue("name");
					if(att != null){
						currentItem.setCommentID(att);
					}
				}
				else if(inReachedNewComment && (!inGotAuthor) && qName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("commentpostername")){
						inReachedAuthor = true;
					}
				}
				else if(inGotAuthor && (!inHitFullImage) && qName.equals("span")){
					String att = atts.getValue("class");
					if(att != null && att.equals("filesize")){
						inHitFullImage = true;
					}
				}
				if(inHitFullImage && (!inGotFullImage) && qName.equals("a")){
					String att = atts.getValue("href");
					if(att != null && att.contains("jpg") || att.contains("gif") || att.contains("png")){
						inGotFullImage = true;
						if(att.contains("http")){
							currentItem.setImageURL(att);
						}
						else{
							currentItem.setImageURL("http:".concat(att));
						}
					}
				}
				if(inGotFullImage && (!inGotThumb) && qName.equals("img")){
					String att = atts.getValue("src");
					if(att != null && att.contains("jpg") || att.contains("gif") || att.contains("png")){
						inGotThumb = true;
						if(att.contains("http")){
							currentItem.setThumbURL(att);
						}
						else{
							currentItem.setThumbURL("http:".concat(att));
						}
					}
				}

				if(inGotAuthor && (!inReachedPostText) && qName.equals("blockquote")){
					inReachedPostText = true;
					buff = new StringBuffer("");
				}
			}
		}
		else if(m_boardType == BoardType.FOURTWENTYCHAN){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					String id = atts.getValue("id");
					String action = atts.getValue("action");
					String enctype = atts.getValue("enctype");
					if(id!=null && id.equals("postform")){
						if(action != null){
							String baseURL = "";
							try {
								baseURL = "http://"+(new URL(this.threadURL).getHost());
							} catch (MalformedURLException e) {
							//	Log.e("BoardListHandler", "balls");
							}
							postingInfo.actionURL = baseURL + action;
							//Log.e("420chanAction",postingInfo.actionURL);
						}
						if(enctype != null){
							postingInfo.encodeType = enctype;
						}
						inReachedPostForm = true;
					//	Log.e("420chanHandler","Reached Post Form");
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
				if(inGotPostInfo){
					if(localName.equals("div")){
						String att = atts.getValue("class");
						if(att != null && att.equals("thread_header")){
							currentItem = new ThreadItem();
						//	Log.e("420chanHandler","new Thread Item");
						}
					}
					else if(localName.equals("td")){
						String att = atts.getValue("class");
						String id = atts.getValue("id");
						if(att != null && att.equals("reply") && id != null){
							currentItem = new ThreadItem();
						//	Log.e("420chanHandler","new Thread Item");
						}
					}
					else if(localName.equals("input")){
						String type = atts.getValue("type");
						String value = atts.getValue("value");
						String name = atts.getValue("name");
						if(type != null && type.equals("checkbox")
							&& value != null && name!=null && name.equals("delete")){
							currentItem.setCommentID(value);
						//	Log.e("420chanHandler","Set comment ID: " + currentItem.getCommentID());
							
						}
					}
					else if(localName.equals("span")){
						String att = atts.getValue("class");
						if(att != null && (att.equals("postername") || att.equals("commentpostername"))){
							inReachedAuthor = true;
						}else if(att != null && att.equals("inbetween")){
							inReachedPostDate = true;
						}else if(att != null && att.equals("filesize")){
							inHitFullImage = true;
						}
					}
					else if(inHitFullImage && !inGotFullImage && localName.equals("a")){
						String att = atts.getValue("href");
						if(att != null){
							String baseURL = "";
							try {
								baseURL = "http://"+(new URL(this.threadURL).getHost());
							} catch (MalformedURLException e) {
						//		Log.e("BoardListHandler", "balls");
							}
							currentItem.setImageURL(baseURL+att);
						//	Log.e("420chanHandler","Set image URL " + currentItem.getImageURL());
							inGotFullImage = true;
						}
					}else if(inGotFullImage && !inGotThumb && localName.equals("img")){
						String src = atts.getValue("src");
						String classy = atts.getValue("class");
						if(src != null && classy != null && classy.equals("thumb")){
							String baseURL = "";
							try {
								baseURL = "http://"+(new URL(this.threadURL).getHost());
							} catch (MalformedURLException e) {
						//		Log.e("BoardListHandler", "balls");
							}
							currentItem.setThumbURL(baseURL+src);
						//	Log.e("420chanHandler","set thumb source: "+currentItem.getThumbURL());
							inGotThumb = true;
						}
					}else if(inGotAuthor && localName.equals("blockquote")){
						inReachedPostText = true;
					//	Log.e("420chanHandler","hit reached blockquote");
						buff = new StringBuffer();
					}
				}
			}
		}else if(m_boardType.equals(BoardType.TWOCHAN)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
				//	Log.e("2chanHandler","Started Reply Form");
					String att = atts.getValue("method");
					String att2 = atts.getValue("action");
					String att3 = atts.getValue("enctype");
					if(	att != null && att.equals("POST") && 
						att2 != null && att2.equals("futaba.php?guid=on") && 
						att3 != null && att3.equals("multipart/form-data")){
						postingInfo.actionURL=threadURL + att2;
						postingInfo.encodeType=att3;
						inReachedPostForm=true;
					}
				}
				else if(inReachedPostForm && (localName.equals("input") || localName.equals("textarea"))){
					String att = atts.getValue("name");
					String att2 = atts.getValue("value");
					String att3 = atts.getValue("type");
					if(att != null){
					//	Log.e("2chanHandler","Reply Form Got "+ att);
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
				if(!gotFirstPost){
					if(localName.equals("form") && !inReachedNewComment){
						String name = atts.getValue("method");
							if(name != null && name.equals("POST")){
								inReachedNewComment = true;
						//		Log.e("2chanHandler","Started Form");
								currentItem = new ThreadItem();
							}
					}
					else if (inReachedNewComment && (!inHitFullImage) && localName.equals("small")){
						inHitFullImage = true;
					//	Log.e("2chanHandler","smalls");
					}
					else if(inHitFullImage && (!inGotThumb) && localName.equals("a")){
						String href = atts.getValue("href");
						if(href!=null && href.contains("http")){
							inGotFullImage = true;
							currentItem.setImageURL(href);
					//		Log.e("2chanhandler","Got full image");
						}
					}
					else if(inGotFullImage &&(!inGotThumb) && localName.equals("img")){
						String src = atts.getValue("src");
						if(src != null && src.contains("http")){
							currentItem.setThumbURL(src);
							inGotThumb = true;
							inReachedImageDiv = false;
				//			Log.e("2chanHandler","Set image url to "+src);
						}
					}
					else if(inGotThumb && localName.equals("input")){
						String type = atts.getValue("type");
						String name = atts.getValue("name");
						if(type != null && type.equals("checkbox") && name != null){
							currentItem.setCommentID(name);
						}
					}
					else if(!inReachedPreAuthor && inGotThumb && localName.equals("font")){
						String color = atts.getValue("color");
						if(color != null && color.contains("117743")){
							inReachedPreAuthor=true;
					//		Log.e("2chanhandler","reached pre author");
						}
					}
					else if(inReachedPreAuthor && (!inGotAuthor) && localName.equals("b")){
						inReachedAuthor = true;
				//		Log.e("2chanhandler","reached author");
					}
					else if(inGotAuthor && !inReachedPostText && localName.equals("blockquote")){
						inReachedPostText = true;
						buff = new StringBuffer();
				//		Log.e("2chanhandler", "hit post text");
					}
				}else{
					if(localName.equals("tr") && !inReachedNewComment){

								inReachedNewComment = true;
					//			Log.e("2chanHandler","Started New comment");
								currentItem = new ThreadItem();
					}
					else if(inReachedNewComment && localName.equals("input")){
						String type = atts.getValue("type");
						String name = atts.getValue("name");
						if(type != null && type.equals("checkbox") && name != null){
							currentItem.setCommentID(name);
						}
					//	Log.e("2chanHandler","got commend id");
					}
					else if(!inReachedPreAuthor && inReachedNewComment  && localName.equals("font")){
						String color = atts.getValue("color");
						if(color != null && color.contains("117743")){
							inReachedPreAuthor=true;
					//		Log.e("2chanhandler","reached pre author");
						}
					}
					else if(inReachedPreAuthor && (!inGotAuthor) && localName.equals("b")){
						inReachedAuthor = true;
					//	Log.e("2chanhandler","reached author");
					}
					else if(inGotAuthor && !inReachedPostText && localName.equals("blockquote")){
						inReachedPostText = true;
						buff = new StringBuffer();
					//	Log.e("2chanhandler", "hit post text");
					}
					else if (inGotAuthor && (!inHitFullImage) && localName.equals("small")){
						inHitFullImage = true;
					//	Log.e("2chanHandler","smalls");
					}
					else if(inHitFullImage && (!inGotThumb) && localName.equals("a")){
						String href = atts.getValue("href");
						if(href!=null && href.contains("http")){
							inGotFullImage = true;
							currentItem.setImageURL(href);
					//		Log.e("2chanhandler","Got full image");
						}
					}
					else if(inGotFullImage &&(!inGotThumb) && localName.equals("img")){
						String src = atts.getValue("src");
						if(src != null && src.contains("http")){
							currentItem.setThumbURL(src);
							inGotThumb = true;
							inReachedImageDiv = false;
					//		Log.e("2chanHandler","Set image url to "+src);
						}
					}
				}
			}
		}else if(m_boardType.equals(BoardType.WAKABA)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					String action = atts.getValue("action");
					String enctype = atts.getValue("enctype");
					String id = atts.getValue("id");
					if(id!=null && id.equals("postform") && action!=null && enctype!=null){
						postingInfo.encodeType = enctype;
						String baseURL = "";
						try {
							baseURL = "http://"+(new URL(this.threadURL).getHost());
						} catch (MalformedURLException e) {
				//			Log.e("BoardListHandler", "balls");
						}
						postingInfo.actionURL = baseURL + action;
				//		Log.e("WAKABA","Got "+postingInfo.actionURL);	
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
				if(!gotFirstPost){
					if (!inReachedNewComment && !inGotAuthor && localName.equals("span")){
						String att = atts.getValue("class");
						if(att != null && att.equals("filesize")){
							inReachedNewComment = true;
							currentItem = new ThreadItem();
				//			Log.e("WAKABA","new thread found");
						}
					}
					else if(inReachedNewComment && !inGotFullImage && localName.equals("a")){
						String href = atts.getValue("href");
						if(href!=null && href.contains("/")){
							String baseURL = "";
							try {
								baseURL = "http://"+(new URL(this.threadURL).getHost());
							} catch (MalformedURLException e) {
					//			Log.e("BoardListHandler", "balls");
							}
							currentItem.setImageURL(baseURL+ href);
							inGotFullImage = true;
					//		Log.e("WAKABA","hit got full image");
						}
					}
					else if(inGotFullImage && !inGotThumb && localName.equals("img")){
						String url = atts.getValue("src");
						String target = atts.getValue("class");
						if(target!= null && target.equals("thumb") && url != null){
							String baseURL = "";
							try {
								baseURL = "http://"+(new URL(this.threadURL).getHost());
							} catch (MalformedURLException e) {
					//			Log.e("BoardListHandler", "balls");
							}
							currentItem.setThumbURL(baseURL+ url);
							inGotThumb = true;
					//		Log.e("WAKABA","hit got thumb");
						}
					}
					else if(inGotThumb && localName.equals("input")){
						String type = atts.getValue("type");
						String value = atts.getValue("value");
						if(type != null && type.equals("checkbox") && value !=null){
							currentItem.setCommentID(value);
						}
					}
					else if(inGotThumb && (!inGotAuthor || !inGotPostTitle) && localName.equals("span")){
						String att = atts.getValue("class");
						if(att != null && !inReachedPostTitle && att.contains("title")){
							inReachedPostTitle = true;
				//			Log.e("WAKABA","hit reached title");
						}
						else if(att != null && !inReachedAuthor && att.contains("name")){
							inReachedAuthor = true;
				//			Log.e("WAKABA","hit reached author");
						}
					}
					else if(localName.equals("blockquote")){
						inReachedPostText = true;
						buff = new StringBuffer();
				//		Log.e("WAKABA","reached text");
					}
				}
				else{
					if (!inReachedNewComment && !inGotAuthor && localName.equals("td")){
						String att = atts.getValue("class");
						if(att != null && att.equals("reply")){
							inReachedNewComment = true;
							currentItem = new ThreadItem();
				//			Log.e("WAKABA","new thread found");
						}
					}
					if(inReachedNewComment && localName.equals("input")){
						String type = atts.getValue("type");
						String value = atts.getValue("value");
						if(type != null && type.equals("checkbox") && value !=null){
							currentItem.setCommentID(value);
						}
					}
					if(!inGotAuthor && localName.equals("span")){
						String att = atts.getValue("class");
						if(att != null && !inReachedAuthor && att.equals("commentpostername")){
							inReachedAuthor = true;
				//			Log.e("WAKABA","hit reached author");
						}
					}
				if(/*inGotAuthor &&*/ !inGotFullImage && localName.equals("a")){
					String href = atts.getValue("href");
					if(href!=null && href.contains("/")){
						String baseURL = "";
						try {
							baseURL = "http://"+(new URL(this.threadURL).getHost());
						} catch (MalformedURLException e) {
					//		Log.e("BoardListHandler", "balls");
						}
						currentItem.setImageURL(baseURL+ href);
						inGotFullImage = true;
					//	Log.e("WAKABA","hit got full image");
					}
				}
				if(/*inGotFullImage &&*/ !inGotThumb && localName.equals("img")){
					String url = atts.getValue("src");
					String target = atts.getValue("class");
					if(target!= null && target.equals("thumb") && url != null){
						String baseURL = "";
						try {
							baseURL = "http://"+(new URL(this.threadURL).getHost());
						} catch (MalformedURLException e) {
					//		Log.e("BoardListHandler", "balls");
						}
						currentItem.setThumbURL(baseURL+ url);
						inGotThumb = true;
					//	Log.e("WAKABA","hit got thumb");
					}
				}
					if(localName.equals("blockquote")){
						inReachedPostText = true;
						buff = new StringBuffer();
					//	Log.e("WAKABA","reached text");
					}
				}
			}
		}
		else if(m_boardType.equals(BoardType.KUSABAX)){
			if(!inGotPostInfo){
				if(localName.equals("form")){
					String action = atts.getValue("action");
					String enctype = atts.getValue("enctype");
					String id = atts.getValue("id");
					if(id!=null && id.equals("postform") && action!=null && enctype!=null){
						postingInfo.encodeType = enctype;
						postingInfo.actionURL = action;
						//Log.e("KUSABAX","Got "+postingInfo.actionURL);	
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
				if(!gotFirstPost){
					if(!inReachedNewComment && localName.equals("form")){
						String name = atts.getValue("id");
						if(name != null && name.equals("delform")){
							currentItem = new ThreadItem();
							inReachedNewComment = true;
							//Log.e("KUSABAX","Hit New Comment");
						}
					}else if(inReachedNewComment && (!inGotFullImage) && localName.equals("a")){
						String href = atts.getValue("href");
						if(href!=null){
							if(href.contains("http")){
								if(href.contains("bucks")){
									String poo = href.substring(10);
									poo = poo.substring(poo.indexOf("http"));
									currentItem.setImageURL(poo);
								}
								else{
									currentItem.setImageURL(href);
								}
							}
							else{
								String baseURL = "";
								try {
									baseURL = "http://"+(new URL(this.threadURL).getHost());
								} catch (MalformedURLException e) {
									//Log.e("BoardListHandler", "balls");
								}
								currentItem.setImageURL(baseURL+ href);
							}
							inGotFullImage=true;
							//Log.e("KUSABAX","Got Full Image");
						}
					}else if(inGotFullImage && (!inGotThumb) && localName.equals("img")){
						String src = atts.getValue("src");
						String att = atts.getValue("class");
						if(src!= null && att != null && att.equals("thumb")){
							if(src.contains("http")){
								currentItem.setThumbURL(src);
							}
							else{
								String baseURL = "";
								try {
									baseURL = "http://"+(new URL(this.threadURL).getHost());
								} catch (MalformedURLException e) {
								//	Log.e("BoardListHandler", "balls");
								}
								currentItem.setThumbURL(baseURL+ src);
							}
							inGotThumb=true;
							//Log.e("KUSABAX","Got Thumb");
						}
						
					}else if(inGotThumb && (localName.equals("input"))){
						String type = atts.getValue("type");
						String value = atts.getValue("value");
						if(type!=null && value != null && type.equals("checkbox")){
							currentItem.setCommentID(value);
							//Log.e("KUSABAX","Got comment id");
						}
					}else if(inGotThumb && (!inReachedAuthor) && localName.equals("span")){
						String att = atts.getValue("class");
						if(att!=null && att.contains("name")){
							inReachedAuthor = true;
							//Log.e("KUSABAX","Reached Author");
						}
					}else if((!inReachedPostText) && localName.equals("blockquote")){
							inReachedPostText = true;
							buff = new StringBuffer();
							//Log.e("KUSABAX","Hit Start of Message");
					}
				}else{
					if((!inReachedNewComment) && localName.equals("tr")){
							currentItem = new ThreadItem();
							inReachedNewComment = true;
							//Log.e("KUSABAX","Hit New Comment");
					}else if(inReachedNewComment && (localName.equals("input"))){
						String type = atts.getValue("type");
						String value = atts.getValue("value");
						if(type!=null && value != null && type.equals("checkbox")){
							currentItem.setCommentID(value);
							//Log.e("KUSABAX","Got comment id");
						}
					}else if((!inReachedAuthor) && (!inGotAuthor)&& localName.equals("span")){
						String att = atts.getValue("class");
						if(att!=null && att.contains("name")){
							inReachedAuthor = true;
							//Log.e("KUSABAX","Reached Author");
						}
					}else if((!inHitFullImage) && localName.equals("span")){
						String att = atts.getValue("class");
						if(att != null && att.equals("filesize")){
							inHitFullImage = true;
							//Log.e("KUSABAX","Hit Full Image");
						}
					}else if(inHitFullImage && (!inGotFullImage) && localName.equals("a")){
						String href = atts.getValue("href");
						if(href!=null){
							if(href.contains("http")){
								if(href.contains("bucks")){
									String poo = href.substring(10);
									poo = poo.substring(poo.indexOf("http"));
									currentItem.setImageURL(poo);
								}
								else{
									currentItem.setImageURL(href);
								}
							}
							else{
								String baseURL = "";
								try {
									baseURL = "http://"+(new URL(this.threadURL).getHost());
								} catch (MalformedURLException e) {
								//	Log.e("BoardListHandler", "balls");
								}
								currentItem.setImageURL(baseURL+ href);
							}
							inGotFullImage=true;
							//Log.e("KUSABAX","Got Full Image");
						}
					}else if(inGotFullImage && (!inGotThumb) && localName.equals("img")){
						String src = atts.getValue("src");
						String att = atts.getValue("class");
						if(src!= null && att != null && att.equals("thumb")){
							if(src.contains("http")){
								currentItem.setThumbURL(src);
							}
							else{
								String baseURL = "";
								try {
									baseURL = "http://"+(new URL(this.threadURL).getHost());
								} catch (MalformedURLException e) {
									//Log.e("BoardListHandler", "balls");
								}
								currentItem.setThumbURL(baseURL+ src);
							}
							inGotThumb=true;
							//Log.e("KUSABAX","Got Thumb");
						}
					}else if((!inReachedPostText) && localName.equals("blockquote")){
							inReachedPostText = true;
							buff = new StringBuffer();
							//Log.e("KUSABAX","Hit Start of Message");
					}
				}
			}
			
		}
		else if (m_boardType.equals(BoardType.FOURARCHIVE)){
			if(inGotPostInfo){
				if(!gotFirstPost){
					if(localName.equals("span") && !inReachedNewComment){
						String att = atts.getValue("class");
						if(att!=null && att.equals("filesize")){
							currentItem = new ThreadItem();
							inReachedNewComment = true;
					//		Log.e("FOURARCHIVE","Created new threaditem");
						}
					}
					if(inReachedNewComment){
						if(!inGotFullImage && localName.equals("a")){
							String att = atts.getValue("href");
							if(att != null && att.contains("image")){
								currentItem.setImageURL(att);
								inGotFullImage = true;
					//			Log.e("FOURARCHIVE","GOT FULL IMAGE: "+att);
							}
						}
						if(!inGotThumb && localName.equals("img")){
							String att = atts.getValue("src");
							if(att!=null && att.contains("image")){
								currentItem.setThumbURL(att);
								inGotThumb = true;
					//			Log.e("FOURARCHIVE","GOT THUMB: "+att);
							}
						}
						if(localName.equals("input")){
							String att = atts.getValue("type");
							String att2 = atts.getValue("name");
							if(att != null && att2!=null && att.contains("checkbox")){
								currentItem.setCommentID(att2);
					//			Log.e("FOURARCHIVE","GOT COMMENTID: "+att2);
							}
						}
						if(!inReachedAuthor && !inGotAuthor && localName.equals("span")){
							String att = atts.getValue("class");
							if(att!=null && att.contains("name")){
								inReachedAuthor = true;
					//			Log.e("FOURARCHIVE","REACHED AUTHOR");
							}
						}
						if(!inReachedPostText && !inGotPostText && localName.equals("blockquote")){
							inReachedPostText = true;
					//		Log.e("FOURARCHIVE","REACHED TEXT");
							buff = new StringBuffer();
						}
					}
				}
				else{
					if(!inReachedNewComment){
						if(localName.equals("input")){
							String att = atts.getValue("type");
							String att2 = atts.getValue("name");
							if(att!= null && att2!=null && att.contains("checkbox")){
								currentItem = new ThreadItem();
								currentItem.setCommentID(att2);
								inReachedNewComment = true;
					//			Log.e("4CHANARCHIVE","HIT NEW COMMENT");
					//			Log.e("4CHANARCHIVE","Got ID: "+att2);
							}
						}
					}
					else{
						if(!inReachedAuthor && !inGotAuthor && localName.equals("span")){
							String att = atts.getValue("class");
							if(att!=null && att.contains("name")){
								inReachedAuthor = true;
					//			Log.e("4CHANARCHIVE","Hit author");
							}
						}
						if(!inGotFullImage && localName.equals("a")){
							String att = atts.getValue("href");
							if(att!=null && att.contains("http") && (att.contains("jpg")||att.contains("jpeg")||att.contains("png")||att.contains("gif")||att.contains("bmp"))){
								currentItem.setImageURL(att);
					//			Log.e("4CHANARCHIVE","Got Full image: "+att);
								inGotFullImage=true;
							}
						}
						if(inGotFullImage && !inGotThumb && localName.equals("img")){
							String att = atts.getValue("src");
							if(att!=null && att.contains("http")){
								currentItem.setThumbURL(att);
					//			Log.e("4ChanArchive","Got Thumb URL: "+att);
								inGotThumb = true;
							}
						}
						if(!inReachedPostText && localName.equals("blockquote")){
							buff = new StringBuffer();
							inReachedPostText = true;
						}
						
					}
				}
			}
		}
	}

	@Override
    public void characters(char ch[], int start, int length) {
		if(m_boardType.equals(BoardType.FOURCHAN) || m_boardType.equals(BoardType.NEWFOURCHAN)  || m_boardType == BoardType.NEWFOURCHANv2 ){
			if(inReachedAuthor && (!inGotAuthor)){
				currentItem.setPostername(new String(ch, start, length));
				inGotAuthor = true;
			}
			if(inReachedPostText && (!inGotPostText)){
				String te = new String(ch, start, length);
				buff.append(te);
			}
			pos = new String(ch, start, length);
			pos = pos.trim();
			if(pos.matches(".{0,40}\\d{2}.\\d{2}.\\d{2,4}.{3,9}\\d{2}.\\d{2}")){
				currentItem.setDate(pos);
			//	Log.e("4chan",pos);
			}
		}else if(m_boardType.equals(BoardType.FOURTWENTYCHAN)){
			if(inReachedAuthor && !inGotAuthor){
				currentItem.setPostername(new String(ch, start, length));
			//	Log.e("420chanHandler","Got postername: "+currentItem.getPostername());
				inGotAuthor = true;
			}
			if(!inGotPostDate){
				pos = new String(ch, start, length);
				pos = pos.trim();
				if(pos.matches(".{0,40}\\d{1,2}:\\d{1,2}:\\d{1,2}.{0,20}")){
			//		Log.e("40chanHandler", "regex match: " +pos);
					currentItem.setDate(pos);
					inGotPostDate = true;
				}
			}
			if(inReachedPostDate && !inGotPostDate){
				currentItem.setDate(new String(ch, start, length));
			//	Log.e("420chanHandler","Got post dat: "+currentItem.getDate());
				if(!currentItem.getDate().contains("posted")){
					inGotPostDate = true;
				}else{inReachedPostDate = false;}
			}
			if(inReachedPostText && !inGotPostText){
				buff.append(ch, start, length);
			}
		}else if(m_boardType.equals(BoardType.SEVENCHAN)){
			if(inReachedAuthor && !inGotAuthor){
				currentItem.setPostername(new String(ch, start, length));
				inGotAuthor = true;
				//Log.e("7chanHandler","Got Postername: "+currentItem.getPostername());
			}
			if(!inGotPostDate){
				pos = new String(ch, start, length);
				pos = pos.trim();
				if(pos.matches("\\W{0,30}\\d{1,2}\\/\\d{1,2}\\/\\d{1,2}\\(.{1,4}\\)\\d{1,2}\\:\\d{1,2}")){

					currentItem.setDate(pos);
					inGotPostDate = true;
					//Log.e("7ChanHandler","Got Post date: "+pos);
				}
			}
			if(inReachedPostText && (!inGotPostText)){
				buff.append(ch,start, length);
			}
		}else if(m_boardType.equals(BoardType.TWOCHAN)){
			if(inReachedAuthor && !inGotAuthor){
				currentItem.setPostername(new String(ch,start,length));
				inGotAuthor=true;
			}
			if(inGotAuthor && !inGotPostDate){
				String temp = new String(ch, start, length);
				if(temp.matches(".{1,40}\\d{1,2}\\/\\d{1,2}\\/\\d{1,2}\\(.{1,20}\\)\\d{1,2}\\:\\d{1,2}\\d{1,2}\\:\\d{1,2}.{1,40}")){
					temp = temp.trim();
					int endit = temp.indexOf(" ");
					temp = temp.substring(0, endit);
					currentItem.setDate(temp);
					inGotPostDate=true;
				}
			}
			else if(inReachedPostText && !inGotPostText){
				buff.append(new String(ch,start,length));
			}
		}
		else if(m_boardType.equals(BoardType.WAKABA)){
			if(inReachedAuthor && !inGotAuthor){
				currentItem.setPostername(new String(ch,start,length));
				if(currentItem.getPostername().length()<1){
					currentItem.setPostername("Anonymous");
				}
				inGotAuthor = true;
			//	Log.e("WAKABA","Got author: "+currentItem.getPostername());
			}
			if(inReachedPostText && !inGotPostText){
				buff.append(new String(ch, start, length));
			//	Log.e("WAKABA","added text");
			}
			if(inReachedNewComment && !inGotPostDate){
				String temp = new String(ch,start,length);
				if(temp.matches(".{1,40}\\d{1,2}\\/\\d{1,2}\\/\\d{1,2}\\(.{1,5}\\)\\d{1,2}:\\d{1,2}.{1,40}")){
					temp = temp.trim();
					currentItem.setDate(temp);
					inGotPostDate=true;
				}
			}
		}else if(m_boardType.equals(BoardType.KUSABAX)){
			if(inReachedAuthor && !inGotAuthor){
				currentItem.setPostername(new String(ch, start, length));
				inGotAuthor = true;
			//	Log.e("KUSABAX","Got Postername: "+currentItem.getPostername());
			}
			if(!inGotPostDate){
				pos = new String(ch, start, length);
				pos = pos.trim();
				if(pos.matches("\\W{0,30}\\d{1,2}\\/\\d{1,2}\\/\\d{1,2}\\(.{1,4}\\)\\d{1,2}\\:\\d{1,2}")){
					currentItem.setDate(pos);
					inGotPostDate = true;
			//		Log.e("KUSABAX","Got Post date: "+pos);
				}
			}
			if(inReachedPostText && (!inGotPostText)){
				buff.append(ch,start, length);
			}
		}
		else if(m_boardType.equals(BoardType.FOURARCHIVE)){
			if(inReachedAuthor && !inGotAuthor){
				currentItem.setPostername(new String(ch, start, length));
				inGotAuthor = true;
			//	Log.e("FOURARCHIVE","Got Postername: "+currentItem.getPostername());
			}
			if(inReachedPostText && (!inGotPostText)){
				buff.append(ch,start, length);
			}
			if(inGotAuthor && !inGotPostDate){
				pos = new String(ch, start, length);
				pos = pos.trim();
				if(pos.matches("\\d{1,2}/\\d{1,2}/\\d{1,2}\\(\\w{1,4}\\)\\d{1,2}:\\d{1,2}:\\d{1,2}")){
					currentItem.setDate(pos);
			//		Log.e("4CHANARCHIVE",pos);
					inGotPostDate=true;
				}
			}
		}
	}
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(m_boardType == BoardType.NEWFOURCHAN || m_boardType == BoardType.NEWFOURCHANv2){
			if(!inGotPostInfo){
				if(inReachedPostForm && qName.equals("form")){
					inGotPostInfo = true;
					
				}
			}
			if(inReachedPostText && localName.equals("br") ){
					buff.append("\n");
			}
			else if(!gotFirstPost && inGotPostInfo){
				if(inReachedPostText && (!inGotPostText) && qName.equals("blockquote")){
					inHitFullImage = false;
					inGotFullImage = false;
					inGotThumb = false;
					inReachedAuthor = false;
					inGotAuthor = false;
					inReachedPostText = false;
					inGotPostText = false;
					gotFirstPost = true;
					
					String temp = buff.toString();
					currentItem.setPostText(StringEscapeUtils.unescapeHtml(temp));
					m_comments.add(currentItem);
				}
			}
			else if(inGotPostInfo){
				if(inReachedPostText && (!inGotPostText) && qName.equals("blockquote")){
					inReachedNewComment = false;
					inHitFullImage = false;
					inGotFullImage = false;
					inGotThumb = false;
					inReachedAuthor = false;
					inGotAuthor = false;
					inReachedPostText = false;
					inGotPostText = false;
					
					String temp = buff.toString();
					currentItem.setPostText(StringEscapeUtils.unescapeHtml(temp));
					m_comments.add(currentItem);
				}
			}
		}else if(m_boardType == BoardType.SEVENCHAN){
			//Log.e("7ChanHandler","Ending: " + uri + " " + localName + " " + qName);
			if(!inGotPostInfo){
				if(inReachedPostForm && localName.equals("form")){
					inGotPostInfo = true;
					//Log.e("7chan","got post info = true");
				}
			}
			else if(/*!gotFirstPost &&*/ inGotPostInfo){
				//Log.e("7ChanHandler","already got post info");
				if(inReachedPostText && localName.equals("br") ){
					buff.append("\n");
					
				}
				if(localName.equals("p")){
					String temp = buff.toString();
					currentItem.setPostText(StringEscapeUtils.unescapeHtml(temp));
					
					inGotPostInfo = true;
					gotFirstPost = true;
					
					inGotAuthor = false;
					inGotFullImage = false;
					inGotPostDate = false;
					inGotPostText = false;
					inGotPostTitle = false;
					inGotThumb = false;
					inHitFullImage = false;
					inReachedAuthor = false;
					inReachedNewComment = false;
					inReachedPostDate = false;
					inReachedPostForm = false;
					inReachedPostText = false;
					inReachedPostTitle = false;
					
					m_comments.add(currentItem);
					//Log.e("7ChanHandler","PUT IT DOWN");
				}
			}
		}else if(m_boardType == BoardType.FOURCHAN){
			if(!inGotPostInfo){
				if(inReachedPostForm && qName.equals("form")){
					inGotPostInfo = true;
					
				}
			}
			if(inReachedPostText && localName.equals("br") ){
					buff.append("\n");
			}
			else if(!gotFirstPost && inGotPostInfo){
				if(inReachedPostText && (!inGotPostText) && qName.equals("blockquote")){
					inHitFullImage = false;
					inGotFullImage = false;
					inGotThumb = false;
					inReachedAuthor = false;
					inGotAuthor = false;
					inReachedPostText = false;
					inGotPostText = false;
					gotFirstPost = true;
					
					String temp = buff.toString();
					currentItem.setPostText(StringEscapeUtils.unescapeHtml(temp));
					m_comments.add(currentItem);
				}
			}
			else if(inGotPostInfo){
				if(inReachedPostText && (!inGotPostText) && qName.equals("blockquote")){
					inReachedNewComment = false;
					inHitFullImage = false;
					inGotFullImage = false;
					inGotThumb = false;
					inReachedAuthor = false;
					inGotAuthor = false;
					inReachedPostText = false;
					inGotPostText = false;
					
					String temp = buff.toString();
					currentItem.setPostText(StringEscapeUtils.unescapeHtml(temp));
					m_comments.add(currentItem);
				}
			}
		}else if(m_boardType.equals(BoardType.FOURTWENTYCHAN)){
			if(!inGotPostInfo && inReachedPostForm){
				if(localName.equals("form")){
					inGotPostInfo=true;
			//		Log.e("420chanHandler","Hit end postinfo");
				}
			}
			else{
				if(inReachedPostText && localName.equals("br") ){
					buff.append("\n");
				}
				if(inReachedPostText && !inGotPostText && localName.equals("blockquote")){
			//		Log.e("420chanHandler","Hit end blockquote");
					String temp = buff.toString();
					currentItem.setPostText(StringEscapeUtils.unescapeHtml(temp));
					m_comments.add(currentItem);
					
					gotFirstPost = true;
					inGotAuthor = false;
					inGotFullImage = false;
					inGotPostDate = false;
					inGotPostText = false;
					inGotThumb = false;
					inHitFullImage = false;
					inReachedAuthor = false;
					inReachedNewComment = false;
					inReachedPostDate = false;
					inReachedPostForm = false;
					inReachedPostText = false;
					inReachedPostTitle = false;
				}
			}
		}else if(m_boardType.equals(BoardType.TWOCHAN)){
			if(inReachedPostText && localName.equals("br") ){
				buff.append("\n");
			}
			if(!inGotPostInfo){
				if(localName.equals("form")){
					inGotPostInfo = true;
				}
			}else{
				if(inReachedPostText && !inGotPostText && localName.equals("blockquote")){
					gotFirstPost = true;
					inGotAuthor = false;
					inGotFullImage = false;
					inGotPostDate = false;
					inGotPostText = false;
					inGotThumb = false;
					inHitFullImage = false;
					inReachedAuthor = false;
					inReachedNewComment = false;
					inReachedPostDate = false;
					inReachedPostForm = false;
					inReachedPostText = false;
					inReachedPostTitle = false;
					inReachedPreTitle = false;
					inReachedPreAuthor = false;
					String temp = buff.toString();
					currentItem.setPostText(StringEscapeUtils.unescapeHtml(temp));
					m_comments.add(currentItem);
				}
			}
		}
		else if(m_boardType.equals(BoardType.WAKABA)){
			if(inReachedPostText && localName.equals("br") ){
				buff.append("\n");
			}
			if(!inGotPostInfo){
				if(localName.equals("form")){
					inGotPostInfo = true;
				}
			}
			else{
				if(inReachedPostText && !inGotPostText && localName.equals("blockquote")){
					inGotPostText = true;
					String temp = buff.toString();
					currentItem.setPostText(StringEscapeUtils.unescapeHtml(temp));
		//			Log.e("WAKABA","got text");
					gotFirstPost = true;
					inGotAuthor = false;
					inGotFullImage = false;
					inGotPostDate = false;
					inGotPostText = false;
					inGotThumb = false;
					inHitFullImage = false;
					inReachedAuthor = false;
					inReachedNewComment = false;
					inReachedPostDate = false;
					inReachedPostForm = false;
					inReachedPostText = false;
					inReachedPostTitle = false;
					inReachedPreTitle = false;
					inReachedPreAuthor = false;
					m_comments.add(currentItem);
		//			Log.e("WAKABA","added thread");
				}
			}
		}else if(m_boardType == BoardType.KUSABAX){
			if(inReachedPostText && localName.equals("br") ){
				buff.append("\n");
			}
			if(!inGotPostInfo){
				if(inReachedPostForm && localName.equals("form")){
					inGotPostInfo = true;
				}
			}
			else if(inGotPostInfo){
				if(localName.equals("blockquote")){
					String temp = buff.toString();
					currentItem.setPostText(StringEscapeUtils.unescapeHtml(temp));
					
					
					inGotPostInfo = true;
					gotFirstPost = true;
					
					inGotAuthor = false;
					inGotFullImage = false;
					inGotPostDate = false;
					inGotPostText = false;
					inGotPostTitle = false;
					inGotThumb = false;
					inHitFullImage = false;
					inReachedAuthor = false;
					inReachedNewComment = false;
					inReachedPostDate = false;
					inReachedPostForm = false;
					inReachedPostText = false;
					inReachedPostTitle = false;
					
					m_comments.add(currentItem);
		//			Log.e("KUSABAX","PUT IT DOWN");
				}
			}
		}else if(m_boardType.equals(BoardType.FOURARCHIVE)){
			if(!gotFirstPost && !inGotPostInfo){
				if(localName.equals("table")){
			//		Log.e("FOURARCHIVE","REACHED GOODNESS");
					currentItem = new ThreadItem();
					postingInfo.actionURL = 
					postingInfo.captchaImageURL = 
					postingInfo.captchaTestURL =
					postingInfo.encodeType =
					postingInfo.fileRequestName = "";
					inGotPostInfo = true;
				}
			}
			else if(inGotPostInfo){
				if(inReachedPostText && localName.equals("blockquote")){
					String temp = buff.toString();
					currentItem.setPostText(StringEscapeUtils.unescapeHtml(temp));
		//			Log.e("FOURARCHIVE","GOT TEXT");
					
					inGotPostInfo = true;
					gotFirstPost = true;
					
					inGotAuthor = false;
					inGotFullImage = false;
					inGotPostDate = false;
					inGotPostText = false;
					inGotPostTitle = false;
					inGotThumb = false;
					inHitFullImage = false;
					inReachedAuthor = false;
					inReachedNewComment = false;
					inReachedPostDate = false;
					inReachedPostForm = false;
					inReachedPostText = false;
					inReachedPostTitle = false;
					if(!currentItem.getPostername().equals("ad")){
						m_comments.add(currentItem);
					}
		//			Log.e("FOURARCHIVE","PYUT IT DOWN");
				}
				else if(inReachedPostText && localName.equals("br")){
					if(buff!=null){
						buff.append("\n");
					}
				}
			}
		}
	}
}
