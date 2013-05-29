/**
  #%L
  Xcode Maven Plugin
  %%
  Copyright (C) 2012 SAP AG
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  **/
  
$(document).ready(function() {

	var lightbox_image = function() {
	  var img = $(this).clone();
	  img.width("auto");
	  img.height("auto");
	  
	  var div = $(document.createElement('div'));
	  div.append(img);
	  
	  var desc=img.attr("title");
	  if(desc) {
	    var descDiv = $(document.createElement('div'));
		descDiv.text(desc);
		descDiv.css("background-color", "black");
		descDiv.css("color", "white");
		descDiv.css("padding-left", "5px");
		descDiv.css("font-weight", "bolder");
		descDiv.css("font-size", "11pt");
	    div.append(descDiv);
      }
	  
	  div.lightbox_me({
        centered: true,
		onClose: function() { 
          $(div).remove();
        }
      });
	};

	var images = $('.screenshot');
	$.each(images, function(index, image) {
	  $(image).css("border-style", "solid");
	  $(image).css("border-width", "1px");
	  $(image).css("border-color", "#0088CC");
	  $(image).css("padding", "5px");
	  $(image).css("cursor", "pointer");
	  $(image).click(lightbox_image);
	});
	
});