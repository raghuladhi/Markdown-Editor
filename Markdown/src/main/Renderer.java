package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Renderer
 */
@WebServlet("/Renderer")
public class Renderer extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Renderer() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		//getting the text
		StringBuilder rawText = getText(request);
		StringBuilder htmlText = new StringBuilder();
	
		//headers 
		rawText = parseHeaders(rawText);
		
		
		//lists
		rawText = parseList(rawText);
			

		
		//parsing __ , //, $$, ** 
		rawText = emphasis(rawText);

	
		//for hyperlinks
		htmlText = parseLink(rawText);


		
		//System.out.println(htmlText);
		response.setContentType("text/html");
		response.getWriter().write(htmlText.toString());	
	
	}
	

	private StringBuilder parseHeaders(StringBuilder rawText) {
		String head, body;
		
		String rawTextString = rawText.toString();
		Pattern pattern = Pattern.compile("([#]+)\\ (.+)",Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(rawText);
		while(matcher.find()){
			head=matcher.group(1);
			body=matcher.group(2);
			rawTextString = rawTextString.replaceFirst("([#]+)\\ (.+)","<h"+head.length()+">"+body+"</h"+head.length()+">" ); 
		}
		rawText = appendText(rawText, rawTextString);
		return rawText;
	}

	private StringBuilder emphasis(StringBuilder rawText) {
		int i,boldFlag=0,italicFlag=0,strikeFlag=0,underlineFlag=0,paraFlag;
		char text[] =rawText.toString().toCharArray();
		StringBuilder htmlText = new StringBuilder();
		
		for(i=0;i<text.length;i++) {
			
			switch(text[i])
			{
				case '*':
					
					
					
					if(i+1<text.length &&text[i+1]=='*' && boldFlag==0) {
						if( rawText.indexOf("**",i+2)!=-1 ) {
						htmlText.append("<b>");
						boldFlag=1;
						i++;
						}
						else {
							htmlText.append("**");
							i++;
						}
					}
					else if(i+1<text.length &&text[i+1]=='*' && boldFlag==1 ) {
						htmlText.append("</b>");
						boldFlag=0;
						i++;
					}
					else {
						htmlText.append("*");
					}
					break;
				case '$':
					if(i+1<text.length && text[i+1]=='$' && strikeFlag==0) {
						if(rawText.indexOf("$$",i+2)!=-1 ) {
						htmlText.append("<s>");
						strikeFlag=1;
						i++;
						}
						else {
							rawText.append("$$");
							i++;
						}
					}
					else if(i+1<text.length && text[i+1]=='$' && strikeFlag==1 ) {
						htmlText.append("</s>");
						strikeFlag=0;
						i++;
					}
					else {
						htmlText.append("$");
					}
					break;
				case '_':
					if(i+1<text.length && text[i+1]=='_' && underlineFlag==0) {
						if(rawText.indexOf("__",i+2)!=-1) {
						htmlText.append("<u>");
						underlineFlag=1;
						i++;
						}
						else
						{
							htmlText.append("__");
							i++;
						}
					}
					else if(i+1<text.length && text[i+1]=='_' && underlineFlag==1 ) {
						htmlText.append("</u>");
						underlineFlag=0;
						i++;
					}
					else {
						htmlText.append("_");
					}
					break;
				case '/':
					if(i-1>=0 && text[i-1]!=':'){
						if(i+1<text.length && text[i+1]=='/' && italicFlag==0) {
							if( rawText.indexOf("//",i+2)!=-1 ) {
							htmlText.append("<i>");
							italicFlag=1;
							i++;
							}
							else{
								htmlText.append("//");
								i++;
							}
						}
						else if(i+1<text.length &&text[i+1]=='/' && italicFlag==1 ) {
							htmlText.append("</i>");
							italicFlag=0;
							i++;
						}
						else{
							htmlText.append("/");
						}
						
					}
					else if(i-1==-1){
						if(i+1<text.length && text[i+1]=='/' && italicFlag==0) {
							if( rawText.indexOf("//",i+2)!=-1 ) {
							htmlText.append("<i>");
							italicFlag=1;
							i++;
							}
							else{
								htmlText.append("//");
								i++;
							}
						}
						else if(i+1<text.length &&text[i+1]=='/' && italicFlag==1 ) {
							htmlText.append("</i>");
							italicFlag=0;
							i++;
						}
						else{
							htmlText.append("/");
						}
					}
					else{
						htmlText.append("/");
					}
					break;
			
					
				case '\n':
					paraFlag=0;
					
					if(i+1<text.length && text[i+1]=='\n'){
						paraFlag=1;
						htmlText.append("<p>");
						i++;
					}
					if(i+1<text.length && text[i+1]!='\n' && paraFlag==0 && i-1>=0 && text[i-1]!='>'){
						htmlText.append("<br>");
					}
					break;
				case ' ':
					htmlText.append("&nbsp;"); 
				default: 
					htmlText.append(text[i]);
					break;
					
			}
		}
	
		return htmlText;
	}

	private StringBuilder parseList(StringBuilder rawText) {
		String rawTextString;
		Pattern pattern = null;
		Matcher matcher = null;
		int i, maxPrefixLength=0,t=0;
		
		//unordered
		
		//finding max limit
		pattern = Pattern.compile("^((\\ +)\\*\\ ){1}.+\\n",Pattern.MULTILINE);
		matcher = pattern.matcher(rawText);
		while(matcher.find()){
			String prefix = matcher.group(2);
			
			if(prefix.length()>maxPrefixLength){
				maxPrefixLength = prefix.length();
			}
		}
				
		//Inserting ul tag
		for(i=1;i<=maxPrefixLength;i++){
			
			pattern = Pattern.compile("(^(\\ {"+i+"}\\*\\ ){1}.+\\n(^(\\ {"+i+",}\\*\\ ){1}.+\\n)*)+",Pattern.MULTILINE);
			matcher = pattern.matcher(rawText);
			if(i==1){
				rawTextString = matcher.replaceAll("<ul>\n"+"$1"+"</ul>\n");
			}
			else{
				rawTextString = matcher.replaceAll("<ul>\n"+"$1"+"</ul>\n</li>\n");
			}
			rawText = appendText(rawText, rawTextString);
		}
		//inserting li tag
		pattern = Pattern.compile("^(\\ +\\*\\ ){1}(.+\\n)(<ul>)",Pattern.MULTILINE);
		matcher = pattern.matcher(rawText);
		rawTextString = matcher.replaceAll("<li>"+"$2"+"<ul>");
		rawText = appendText(rawText, rawTextString);
		
		pattern = Pattern.compile("^(\\ +\\*\\ ){1}(.+\\n)",Pattern.MULTILINE);
		matcher = pattern.matcher(rawText);
		rawTextString = matcher.replaceAll("<li>"+"$2"+"</li>");
		rawText = appendText(rawText, rawTextString);

		
		//ordered
		//finding max limit
		maxPrefixLength = 0;	
		pattern = Pattern.compile("^((\\ +)[0-9]+\\.\\ ){1}.+\\n",Pattern.MULTILINE);
		matcher = pattern.matcher(rawText);
		while(matcher.find()){
			String prefix = matcher.group(2);
			
			if(prefix.length()>maxPrefixLength){
				maxPrefixLength = prefix.length();
			}
		}
		//inserting ol tag
		for(i=1;i<=maxPrefixLength;i++){
			
			pattern = Pattern.compile("(^(\\ {"+i+"}[0-9]+\\.\\ ){1}.+\\n(^(\\ {"+i+",}[0-9]+\\.\\ ){1}.+\\n)*)+",Pattern.MULTILINE);
			matcher = pattern.matcher(rawText);
			if(i==1){
				rawTextString = matcher.replaceAll("<ol>\n"+"$1"+"</ol>\n");
			}
			else{
				rawTextString = matcher.replaceAll("<ol>\n"+"$1"+"</ol>\n</li>\n");
			}
			rawText = appendText(rawText, rawTextString);
		}

		//inserting li tag
		pattern = Pattern.compile("^(\\ +[0-9]+\\.\\ ){1}(.+\\n)(<ol>)",Pattern.MULTILINE);
		matcher = pattern.matcher(rawText);
		rawTextString = matcher.replaceAll("<li>"+"$2"+"<ol>");
		rawText = appendText(rawText, rawTextString);
		
		pattern = Pattern.compile("^(\\ +[0-9]+\\.\\ ){1}(.+\\n)",Pattern.MULTILINE);
		matcher = pattern.matcher(rawText);
		rawTextString = matcher.replaceAll("<li>"+"$2"+"</li>");
		rawText = appendText(rawText, rawTextString);

		
		return rawText;
	}

	private StringBuilder parseLink(StringBuilder rawText) {
		
		int linkCount=0,start,end,i;
		String rawTextString;
		ArrayList<String> links =new ArrayList<String>();
		Pattern pattern = Pattern.compile("\\[.*?\\]\\([a-zA-Z0-9\\.\\@\\:\\|\\/\\?\\-;:&=\\+]+\\)");
		Matcher matcher = pattern.matcher(rawText);
		
		while(matcher.find()){
			linkCount++;
			start=matcher.start();
			end=matcher.end();
			String linkText = "<a href=\"";
			String temp=rawText.substring(start,end);
		
			//actual link
			for(i=temp.indexOf('(')+1;i<temp.indexOf(')');i++) {
				linkText = linkText + temp.charAt(i);
			}
			linkText = linkText +"\" target=\"_blank\">";
			//text
			for(i=temp.indexOf('[')+1;i<temp.indexOf(']');i++) {
				linkText = linkText+temp.charAt(i);
				
			}
			linkText = linkText+"</a>";
			links.add(linkText);
			
		
		}
			
			rawTextString =rawText.toString();
			for(i=0;i<linkCount;i++){
				rawTextString = rawTextString.replaceFirst("\\[.*?\\]\\([a-zA-Z0-9\\.\\@\\:\\|\\/\\?\\-;:&=\\+]+\\)", links.get(i));
			}
			
			rawText = appendText(rawText, rawTextString);
			
		return rawText;
	}

	private StringBuilder getText( HttpServletRequest request) {
		int value;
		StringBuilder rawText = new StringBuilder();
		BufferedReader bufferedreader = null;
		try {
			bufferedreader = request.getReader();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
		try {
			while((value=bufferedreader.read())!=-1){
				
				rawText.append((char)value);
				
				
			}
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		
		
		
		
		return rawText;
	}

	private StringBuilder appendText(StringBuilder rawText, String string){
		if(rawText.length()>0){
			rawText = new StringBuilder();
			rawText.append(string);
		}
		return rawText;
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}