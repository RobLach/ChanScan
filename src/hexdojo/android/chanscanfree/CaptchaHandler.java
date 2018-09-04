package hexdojo.android.chanscanfree;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class CaptchaHandler extends DefaultHandler{
	String captchaImageURL;
	public List<NameValuePair> inputPairs;
	String captchaResponse;
	
	Boolean hitResponse = false;

	public String getCaptchResponse() {
		return captchaResponse;
	}

	public void setCaptchResponse(String captchResponse) {
		this.captchaResponse = captchResponse;
	}

	public List<NameValuePair> getInputPairs() {
		return inputPairs;
	}

	public void setInputPairs(List<NameValuePair> inputPairs) {
		this.inputPairs = inputPairs;
	}

	public String getCaptchaImageURL() {
		return captchaImageURL;
	}

	public void setCaptchaImageURL(String captchaImageURL) {
		this.captchaImageURL = captchaImageURL;
	}

	public CaptchaHandler(){
		captchaImageURL = "";
		inputPairs = new ArrayList<NameValuePair>();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(localName.equals("img")){
			String att = attributes.getValue("src");
			if(att!=null){
				captchaImageURL =  new String("http://www.google.com/recaptcha/api/"+att);
			}
		}
		if(localName.equals("input")){
			String att = attributes.getValue("name");
			String att2 = attributes.getValue("value");
			if(att!= null){
				if(att2 != null){
					inputPairs.add(new BasicNameValuePair(att, att2));
				}else{
					inputPairs.add(new BasicNameValuePair(att,""));
				}
			}
		}
		if(localName.equals("textarea")){
			hitResponse=true;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
	
		if(localName.equals("textarea")){
			hitResponse=false;
		}
	}
	
	@Override
    public void characters(char ch[], int start, int length) {
		if(hitResponse){
			captchaResponse = new String(ch, start, length);
			Log.e("CaptchaResponse",captchaResponse);
		}
	}
	
	
}
