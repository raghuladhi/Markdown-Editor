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

		
		
		//lists
		rawText = parseList(rawText);
			

		//for hyperlink
		rawText = parseLink(rawText);
		
		//parsing __ , //, $$, ** 
		htmlText = emphasis(rawText);

		
		//System.out.println(htmlText);
		response.setContentType("text/html");
		response.getWriter().write(htmlText.toString());	
	
	}
	

	private StringBuilder emphasis(StringBuilder rawText) {
		int i,boldFlag=0,italicFlag=0,strikeFlag=0,underlineFlag=0,paraFlag;
		char text[] =rawText.toString().toCharArray();
		StringBuilder htmlText = new StringBuilder();
		
		for(i=0;i<text.length;i++) {
			
			switch(text[i])
			{
				case '*':
					if(i+1!=rawText.length() &&text[i+1]=='*' && boldFlag==0) {
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
					else if(i+1!=rawText.length() &&text[i+1]=='*' && boldFlag==1 ) {
						htmlText.append("</b>");
						boldFlag=0;
						i++;
					}
					else {
						htmlText.append("*");
					}
					break;
				case '$':
					if(i+1!=rawText.length() && text[i+1]=='$' && strikeFlag==0) {
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
					else if(i+1!=rawText.length() && text[i+1]=='$' && strikeFlag==1 ) {
						htmlText.append("</s>");
						strikeFlag=0;
						i++;
					}
					else {
						htmlText.append("$");
					}
					break;
				case '_':
					if(i+1!=rawText.length() && text[i+1]=='_' && underlineFlag==0) {
						if(rawText.indexOf("__",i+2)!=-1 ) {
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
					else if(i+1!=rawText.length() && text[i+1]=='_' && underlineFlag==1 ) {
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
						if(i+1!=rawText.length() && text[i+1]=='/' && italicFlag==0) {
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
						else if(i+1!=rawText.length() &&text[i+1]=='/' && italicFlag==1 ) {
							htmlText.append("</i>");
							italicFlag=0;
							i++;
						}
						else{
							htmlText.append("/");
						}
						
					}
					else if(i-1==-1){
						if(i+1!=rawText.length() && text[i+1]=='/' && italicFlag==0) {
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
						else if(i+1!=rawText.length() &&text[i+1]=='/' && italicFlag==1 ) {
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
					
					if(i+1!=text.length && text[i+1]=='\n'){
						paraFlag=1;
						htmlText.append("<p>");
						i++;
					}
					if(i+1!=text.length && text[i+1]!='\n' && paraFlag==0 && i-1>=0 && text[i-1]!='>'){
						htmlText.append("<br>");
					}
					break;
				default:
					htmlText.append(text[i]);
					break;
					
			}
		}
	
		return htmlText;
	}

	private StringBuilder parseList(StringBuilder rawText) {
		String rawTextString,test1,test2;
		int count=0,listCount=0;
		//unordered
		StringBuilder temp = new StringBuilder();

		
		
		
		
		
		
		
		temp.append(rawText);
		Pattern pattern = Pattern.compile("(^[^a-zA-Z0-9]*(\\ \\*\\ )+[a-zA-Z0-9\\ \\.\\:\\;\\/\\*\\$\\_]+\\n)+",Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(rawText);
		
		
		while(matcher.find()){
			
			temp.insert((matcher.start())+(count*11), "<ul>\n");
			temp.insert((matcher.end())+(count*11)+5, "\n</ul>");
			count++;
		
		}
		pattern = Pattern.compile("^[^a-zA-Z0-9]*(\\ \\*\\ )+([a-zA-Z0-9\\ \\.\\:\\;\\/\\*\\$\\_]+\\n)",Pattern.MULTILINE);
		matcher = pattern.matcher(temp);
		test1 = matcher.replaceAll("<li>"+"$2"+"</li>");
		//System.out.println(test1);
				
				
				
	

		if(rawText.length()>0){
			rawText = new StringBuilder();
			rawText.append(test1);
		}
	
		//ordered
		count=0;
		temp = new StringBuilder();
		temp.append(rawText);
		pattern = Pattern.compile("(^[^a-zA-Z]*(\\ [0-9]+\\.\\ )+[a-zA-Z0-9\\ \\.\\:\\;\\/\\*\\$\\_]+\\n)+",Pattern.MULTILINE);
		matcher = pattern.matcher(rawText);
		
		
		while(matcher.find()){
			
			temp.insert((matcher.start())+(count*11), "<ol>\n");
			temp.insert((matcher.end())+(count*11)+5, "\n</ol>");
			count++;
		}
	
		pattern = Pattern.compile("^[^a-zA-Z]*(\\ [0-9]+\\.\\ )+([a-zA-Z0-9\\ \\.\\:\\;\\/\\*\\$\\_]+\\n)",Pattern.MULTILINE);
		matcher = pattern.matcher(temp);
		test2 = matcher.replaceAll("<li>"+"$2"+"</li>");
		
		
		
		
	//	System.out.println(test2);
		if(rawText.length()>0){
			rawText = new StringBuilder();
			rawText.append(test2);
		}
		return rawText;
	}

	private StringBuilder parseLink(StringBuilder rawText) {
		
		int linkCount=0,start,end,i;
		String rawTextString;
		ArrayList<String> links =new ArrayList<String>();
		Pattern pattern = Pattern.compile("\\[.*?\\]\\([a-z|A-Z|0-9|\\.|\\:|/]*\\)");
		Matcher matcher = pattern.matcher(rawText);
		
		while(matcher.find()){
			linkCount++;
			start=matcher.start();
			end=matcher.end();
			
			String linkText = "<a target =\"_blank\" href=\"";
			String temp=rawText.substring(start,end);
		
			//actual link
			for(i=temp.indexOf('(')+1;i<temp.indexOf(')');i++) {
				linkText = linkText + temp.charAt(i);
			}
			linkText = linkText +"\">";
			//text
			for(i=temp.indexOf('[')+1;i<temp.indexOf(']');i++) {
				linkText = linkText+temp.charAt(i);
				
			}
			linkText = linkText+"</a>";
			links.add(linkText);
			
		
		}
			
			rawTextString =rawText.toString();
			for(i=0;i<linkCount;i++){
				rawTextString = rawTextString.replaceFirst("\\[.*?\\]\\([a-z|A-Z|0-9|\\.|\\:|/]*\\)", links.get(i));
			}
			
			if(rawText.length()>0){
				rawText = new StringBuilder();
				rawText.append(rawTextString);
			}
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}