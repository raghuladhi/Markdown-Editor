/**
 * 
 */
var iframedoc;
var text_area = document.getElementById("text_area");
var rendered_html = document.getElementById("rendered_html");
var iframe = document.getElementById("rendered_text");
var btn = document.getElementById("btn");
function exec(){
	var input_text = document.getElementById("text_area").value;
	fetch('http://localhost:8081/Markdown/render',{
		method:'POST',
		body:input_text,
		header:{
			'content-type':'text/plain'
		}
	})
	.then(function(response) {
		console.log("Successful request");
	    response.text()
	    	.then(function(text){
	    		
	    		rendered_html.innerHTML = text;
	    		iframe.setAttribute("srcdoc",text);
	    	});
	    
	})
	.catch(function(err){
		console.log("error is "+err);
	});
	
	
}