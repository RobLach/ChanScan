package hexdojo.android.chanscanlite;

import hexdojo.android.chanscanlite.ChanScan.BoardType;

public class BoardThread {
	String thumbImageURL;
	String openThreadURL;
	String threadAuthor;
	String postText;
	String postTitle = "";
	int	   numPosts = 1;
	int    numImages = 1;


	BoardType btype;

	
	public String getPostTitle() {
		return postTitle;
	}

	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}
	
	public BoardType getBtype() {
		return btype;
	}

	public void setBtype(BoardType btype) {
		this.btype = btype;
	}

	public BoardThread(BoardType type){
		this.btype = type;
		threadAuthor = new String("Anonymous");
	}
	
	public String getPostText() {
		return postText;
	}

	public void setPostText(String postText) {
		this.postText = postText;
	}

	public String getThreadAuthor() {
		return threadAuthor;
	}

	public void setThreadAuthor(String threadAuthor) {
		this.threadAuthor = threadAuthor;
	}

	public String getOpenThreadURL() {
		return openThreadURL;
	}

	public void setOpenThreadURL(String openThreadURL) {
		this.openThreadURL = openThreadURL;
	}


	
	public void setThumbImageURL(String imageURL){
		this.thumbImageURL = imageURL;
	}
	
	public String getThumbImageUrl(){
		return thumbImageURL;
	}

	public int getNumPosts() {
		return numPosts;
	}

	public void setNumPosts(int numPosts) {
		this.numPosts = numPosts;
	}

	public int getNumImages() {
		return numImages;
	}

	public void setNumImages(int numImages) {
		this.numImages = numImages;
	}
}
