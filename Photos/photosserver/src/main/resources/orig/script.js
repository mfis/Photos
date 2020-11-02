function videoOn(index) {
	document.getElementById('vidImgDiv' + index).style.display = 'none';
	document.getElementById('vidTagDiv' + index).style.display = 'block';
	var video = document.getElementById('video' + index);
	video.controls = true;
	var source = document.createElement('source');
	source.setAttribute('src', obj.baseURL + obj.pictures[index].name);
	source.setAttribute('type', 'video/mp4');
	video.appendChild(source);
	video.play();
}

function videoOff(index) {
	var video = document.getElementById("video" + index);
	video.pause();
	while (video.firstChild) {
		video.firstChild.src =""; 
		video.removeChild(video.firstChild);
		video.load();
	}
	document.getElementById('vidImgDiv' + index).style.display = 'block';
	document.getElementById('vidTagDiv' + index).style.display = 'none';
}

function getPreviewHeight(){
	return (document.documentElement.clientHeight / 4 * 3) + 'px';
}

function getPreviewWidth(){
	return (document.documentElement.clientWidth / 4 * 3) + 'px';
}

function showgallery() {
	
	for (var i = 0; i < obj.pictures.length; i++) {
		var elemFigure = document.createElement("FIGURE");
		elemFigure.id = "figure" + i;
		elemFigure.onclick = function(z) {
			var id = z.target.id
			console.log(z.target);
			var index = id.split("-")[1];
			start(index);
		}
		var elemA = document.createElement("A");
		elemA.id = "imgA-" + i;
		var elemImg = document.createElement("IMG");
		elemImg.id = "img-" + i;
		var srcString;
		if(obj.pictures[i].name.endsWith(".mp4")){
			var caption = obj.pictures[i].name.substring(0, obj.pictures[i].name.length-4); 
			srcString = obj.baseURL + "tn_" + caption + ".jpg";
		}else{
			srcString = obj.baseURL + "tn_" + obj.pictures[i].name;
		}
		
		elemImg.src = 'data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw=='; 
		elemImg.setAttribute("data-src", srcString);
		
		elemImg.height = obj.pictures[i].tnh;
		elemImg.width = obj.pictures[i].tnw;
		
		if(obj.pictures[i].tnh > obj.pictures[i].tnw){
			elemImg.className = "portrait b-lazy";
		}else{
			elemImg.className = "b-lazy";
		}

		var elemDiv = document.createElement("DIV");
		elemDiv.id = "elemDiv-" + i;
		elemDiv.className = "thumbnailDiv";
		elemDiv.appendChild(elemImg);
		var l = Math.min(obj.pictures[i].tnh, obj.pictures[i].tnw);
		elemDiv.style.width = l + "px";
		elemDiv.style.height = l + "px";
		
		elemA.appendChild(elemDiv);
		elemFigure.appendChild(elemA);
		document.getElementById("pictures").appendChild(elemFigure);
	}

}

function start(indexToStart) {

	var pswpElement = document.querySelectorAll('.pswp')[0];
	var options = {
		history : false,
		index : parseInt(indexToStart),
		isClickableElement: function(el) {
			var x = (el.tagName === 'A' || el.tagName === 'IMG' || el.tagName === 'VIDEO') && el.className === 'psVideoElement';
		    return x;
		}
	};
	actObjectIndex = indexToStart;

	var gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default,
			obj.pictures, options);
	gallery.init();
	
	gallery.listen('afterChange', function() { 
		prevObjectIndex = actObjectIndex;
		actObjectIndex = gallery.getCurrentIndex();
		if(prevObjectIndex>-1){
			if(obj.pictures[prevObjectIndex].name.endsWith(".mp4")){
				videoOff(prevObjectIndex);
			}
		}
	});
	
	gallery.listen('close', function() {
		if(gallery.getCurrentIndex()>-1){
			if(obj.pictures[gallery.getCurrentIndex()].name.endsWith(".mp4")){
				videoOff(gallery.getCurrentIndex());
			}
		}
		actObjectIndex = -1;
		prevObjectIndex = -1;
		document.ontouchmove = function (e) { return true; }
	});
	
	document.ontouchmove = function (e) { e.preventDefault() }
	
	 window.onresize = function(event) {
		if(gallery.getCurrentIndex()>-1){
			if(obj.pictures[gallery.getCurrentIndex()].name.endsWith(".mp4")){
				var w = getPreviewWidth();
				var h = getPreviewHeight();
				document.getElementById('vidImg' + gallery.getCurrentIndex()).style.maxHeight = h;
				document.getElementById('vidImg' + gallery.getCurrentIndex()).style.maxWidth = w;
				document.getElementById('video' + gallery.getCurrentIndex()).style.maxHeight = h;
				document.getElementById('video' + gallery.getCurrentIndex()).style.maxWidth = w;				
			}
		}
	 };
	
};
