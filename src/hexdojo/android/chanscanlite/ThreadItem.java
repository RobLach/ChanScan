package hexdojo.android.chanscanlite;

public class ThreadItem  {
	String 	postername;
	String 	imageURL;
	String 	thumbURL;
	String 	postText;
	String 	date;
	String	commentID;
	boolean	isAd;
	
	public ThreadItem(){
		postername = "";
		imageURL = "";
		thumbURL = "";
		postText = "";
		date = "";
		commentID = "";
		isAd=false;
	}
	
	public String getCommentID() {
		return commentID;
	}

	public void setCommentID(String commentID) {
		this.commentID = commentID;
	}
	
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public String getPostername() {
		return postername;
	}
	public void setPostername(String poster) {
		this.postername = poster;
	}
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String image) {
		this.imageURL = image;
		if(this.imageURL.contains("ichan.org")){
			this.imageURL = this.imageURL.substring(this.imageURL.indexOf("http://ichan.org"),this.imageURL.length());
		}
	}
	public String getThumbURL() {
		return thumbURL;
	}
	public void setThumbURL(String thumb) {
		this.thumbURL = thumb;
	}
	public String getPostText() {
		return postText;
	}
	public void setPostText(String post) {
		this.postText = post;
	}
}
