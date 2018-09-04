package hexdojo.android.chanscanlite;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;

public class PostingInfo {
		public String actionURL;
		public String encodeType;
		public List<NameValuePair> inputPairs;
		public String fileRequestName;
		public String captchaTestURL;
		public String captchaImageURL;
		
	public PostingInfo() {
		actionURL = "";
		encodeType = "";
		inputPairs = new ArrayList<NameValuePair>();
		fileRequestName = "";
		captchaImageURL = "";
		captchaTestURL = "";
	}
}

