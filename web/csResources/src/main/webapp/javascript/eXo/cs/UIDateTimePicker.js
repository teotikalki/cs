UIDateTimePicker = function(calendarId) {
	this.calendarId = calendarId ;
  this.dateField = null ;
  this.currentDate = null ; 	// Datetime value base of selectedDate for displaying calendar below
  														// if selectedDate is invalid, currentDate deals with system time;
  this.selectedDate = null ; //Datetime value of input date&time field
  this.months = ['January','February','March','April','May','June','July','August','September','October','November','December'] ;
  this.weekdays = ['S','M','T','W','T','F','S'] ;
}

UIDateTimePicker.prototype.init = function(field, isDisplayTime) {
	this.isDisplayTime = isDisplayTime ;
	if (this.dateField) {
		this.dateField.parentNode.style.position = '' ;
	}
	this.dateField = field ;
	if (!document.getElementById(this.calendarId)) this.create() ;
  field.parentNode.insertBefore(document.getElementById(this.calendarId), field) ;
  this.show() ;
}

UIDateTimePicker.prototype.create = function() {
	var clndr = document.createElement("div") ;
	clndr.id = this.calendarId ;
	clndr.style.position = "absolute" ;
	if (document.all) {
		clndr.innerHTML = "<div class='UICalendarComponent'><iframe id='" + this.calendarId + "IFrame' src='javascript:false;' frameBorder='0' scrolling='no'></iframe><div style='position: absolute'></div></div>" ;
	} else {
		clndr.innerHTML = "<div class='UICalendarComponent'><div style='position: absolute; width: 100%;'></div></div>" ;
	}
	document.body.appendChild(clndr) ;
} ;

UIDateTimePicker.prototype.show = function() {
	document.onmousedown = new Function('eXo.cs.UIDateTimePicker.hide()') ;
  var str = this.dateField.getAttribute("format") ;
  str = str.replace(/d{2}/,"(\\d{1,2}\\") ;
  str = str.replace(/M{2}/,"\\d{1,2}\\") ;
  str = str.replace(/y{2,4}/,"\\d{1,4})") ;
  if(this.isDisplayTime) {
    str = str.replace(/\s+/,"\\s*") ;
    str = str.replace(/H{2}/,"(\\d{1,2}\\") ;
    str = str.replace(/m{2}/,"\\d{1,2}\\") ;
    str = str.replace(/s{2}/,"\\d{1,2})") ;    
  }
  str = "^" + str + "?$" ;
	re = new RegExp(str,'i') ;
  this.selectedDate = new Date() ;
	if (re.test(this.dateField.value)) {
	  var dateParts = this.dateField.value.split(" ") ;
    var spLine = dateParts[0].match(/\W{1}/) ;
	  var arr = dateParts[0].split(spLine) ;
    var type = this.getTypeFormat() ;
    var month = 0 ;
    var date = 0 ;
    switch(type) {
      case 0 :
      case 1 : 
        date = arr[0] ;
        month = arr[1] ;
      break ;
      case 2 :
      case 3 : 
        date = arr[1] ;
        month = arr[0] ;
      break ;
      default : 
        date = arr[0] ;
        month = arr[1] ;
    }
	  this.selectedDate.setDate(parseInt(date,10)) ;
	  this.selectedDate.setMonth(parseInt(month,10) - 1) ;
	  this.selectedDate.setFullYear(parseInt(arr[2],10)) ;
	  if (dateParts.length > 1 && dateParts[dateParts.length - 1] != "") {
	  	spLine = dateParts[dateParts.length - 1].match(/\W{1}/) ;
      arr = dateParts[dateParts.length - 1].split(spLine) ;
	  	this.selectedDate.setHours(arr[0], 10) ;
	  	this.selectedDate.setMinutes(arr[1], 10) ;
	  	this.selectedDate.setSeconds(arr[2], 10) ;
	  }
	}
	this.currentDate = new Date(this.selectedDate.valueOf()) ;
  var clndr = document.getElementById(this.calendarId) ;
  clndr.firstChild.lastChild.innerHTML = this.renderCalendar() ;
  var x = 0 ;
  var y = this.dateField.offsetHeight ;
  with (clndr.firstChild.style) {
  	display = 'block' ;
	  left = x + "px" ;
	  top = y + "px" ;
  }
	
		var drag = document.getElementById("blockCaledar");
		var innerWidth = drag.offsetWidth;
		drag.onmousedown = function(evt) {
			var event = evt || window.event;
			event.cancelBubble = true;
			drag.style.position = "absolute";
			drag.style.width = innerWidth + "px";
			eXo.core.DragDrop.init(null, drag, drag, event);
	 	}
	var primary = eXo.core.DOMUtil.findAncestorById(this.dateField, "UIECMSearch");
	if (primary && eXo.core.Browser.isFF()) {
			calendar = clndr.firstChild;
			calendar.style.top = "0px";
			calendar.style.left = this.dateField.offsetLeft - this.dateField.offsetWidth - 32 + "px";
	}
	
} ;

UIDateTimePicker.prototype.hide = function() {
  if (this.dateField) {
    document.getElementById(this.calendarId).firstChild.style.display = 'none' ;
    this.dateField = null ;
  }
 	document.onmousedown = null ;
} ;

UIDateTimePicker.prototype.getTypeFormat = function() {
  var dateMask = ["dd/MM/yyyy","dd-MM-yyyy","MM/dd/yyyy","MM-dd-yyyy"] ;
  var dateTimeFormat = this.dateField.getAttribute("format") ;
  var dateFormat = (this.isDisplayTime)?dateTimeFormat.split(' ')[0].trim() : dateTimeFormat ;
  var len = dateMask.length ;
  for(var i = 0 ; i < len ; i ++) {
    if (dateMask[i] == dateFormat) return i ;
  }
  return false ;
}

/* TODO: Move HTML code to a javascript template file (.jstmpl) */
UIDateTimePicker.prototype.renderCalendar = function() {
  var dayOfMonth = 1 ;
  var validDay = 0 ;
  var startDayOfWeek = this.getDayOfWeek(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, dayOfMonth) ;
  var daysInMonth = this.getDaysInMonth(this.currentDate.getFullYear(), this.currentDate.getMonth()) ;
  var clazz = null;
	var table = '<div id="blockCaledar" class="BlockCalendar">' ;
	table += 		'<div class="UICalendar" onmousedown="event.cancelBubble = true">' ;
	table += 		'	<table class="MonthYearBox">' ;
	table += 		'	  <tr>' ;
	table += 		'			<td class="MonthButton"><a class="PreviousMonth" href="javascript:eXo.cs.UIDateTimePicker.changeMonth(-1);"></a></td>' ;
	table += 		'			<td class="YearButton"><a class="PreviousYear" href="javascript:eXo.cs.UIDateTimePicker.changeYear(-1);"></a></td>' ;
	table += 		'			<td><span style="color:#f89302;">' + this.months[this.currentDate.getMonth()] + '</span> - <span>' + this.currentDate.getFullYear() + '</span></td>' ;
	table += 		'			<td class="YearButton"><a class="NextYear" href="javascript:eXo.cs.UIDateTimePicker.changeYear(1);"></a></td>' ;
	table += 		'			<td class="MonthButton"><a class="NextMonth" href="javascript:eXo.cs.UIDateTimePicker.changeMonth(1);"></a></td>' ;
	table += 		'		</tr>' ;
	table += 		'	</table>' ;
	table += 		'	<div style="margin-top: 6px;padding: 0px 5px;">' ;
	table += 		'		<table>' ;
	table += 		'			<tr>' ;
	table += 		'				<td><font color="red">' + this.weekdays[0] + '</font></td><td>' + this.weekdays[1] + '</td><td>' + this.weekdays[2] + '</td><td>' + this.weekdays[3] + '</td><td>' + this.weekdays[4] + '</td><td>' + this.weekdays[5] + '</td><td>' + this.weekdays[6] + '</td>' ;
	table += 		'			</tr>' ;
	table += 		'		</table>' ;
	table += 		'	</div>' ;
	table += 		'	<div class="CalendarGrid">' ;
	table += 		'	<table>' ;
  for (var week=0; week < 6; week++) {
    table += "<tr>";
    for (var dayOfWeek=0; dayOfWeek < 7; dayOfWeek++) {
      if (week == 0 && startDayOfWeek == dayOfWeek) {
        validDay = 1;
      } else if (validDay == 1 && dayOfMonth > daysInMonth) {
        validDay = 0;
      }
      if (validDay) {
        if (dayOfMonth == this.selectedDate.getDate() && this.currentDate.getFullYear() == this.selectedDate.getFullYear() && this.currentDate.getMonth() == this.selectedDate.getMonth()) {
          clazz = 'Current';
        } else if (dayOfWeek == 0 || dayOfWeek == 6) {
          clazz = 'Weekend';
        } else {
          clazz = 'Weekday';
        }

        table = table + "<td><a class='"+clazz+"' href=\"javascript:eXo.cs.UIDateTimePicker.setDate("+this.currentDate.getFullYear()+","+(this.currentDate.getMonth() + 1)+","+dayOfMonth+")\">"+dayOfMonth+"</a></td>" ;
        dayOfMonth++ ;
      } else {
        table = table + "<td class='empty'><div>&nbsp;</div></td>" ;
      }
    }
    table += "</tr>" ;
  }		
	table += 		'		</table>' ;
	table += 		'	</div>' ;
	if (this.isDisplayTime) {
		table += 		'	<div class="CalendarTimeBox">' ;
		table += 		'		<div class="CalendarTimeBoxR">' ;
		table += 		'			<div class="CalendarTimeBoxM"><span><input class="InputTime" size="2" maxlength="2" value="' + this.currentDate.getHours() + '" onkeyup="eXo.cs.UIDateTimePicker.setHour(this)" >:<input size="2" class="InputTime" maxlength="2" value="' + this.currentDate.getMinutes() + '" onkeyup = "eXo.cs.UIDateTimePicker.setMinus(this)">:<input size="2" class="InputTime" maxlength="2" value="' + this.currentDate.getSeconds() + '" onkeyup = "eXo.cs.UIDateTimePicker.setSeconds(this)"></span></div>' ;
		table += 		'		</div>' ;
		table += 		'	</div>' ;
	}
	table += 		'</div>' ;
	table += 		'</div>' ;
	return table ;
} ;

UIDateTimePicker.prototype.changeMonth = function(change) {
	this.currentDate.setMonth(this.currentDate.getMonth() + change) ;
  var clndr = document.getElementById(this.calendarId) ;
  clndr.firstChild.lastChild.innerHTML = this.renderCalendar() ;
} ;

UIDateTimePicker.prototype.changeYear = function(change) {
  this.currentDate.setFullYear(this.currentDate.getFullYear() + change) ;
  this.currentDay = 0 ;
  var clndr = document.getElementById(this.calendarId) ;
  clndr.firstChild.lastChild.innerHTML = this.renderCalendar() ;
} ;

UIDateTimePicker.prototype.setDate = function(year, month, day) {	
  if (this.dateField) {
    if (month < 10) month = "0" + month ;
    if (day < 10) day = "0" + day ;
    var dateString = this.dateField.getAttribute("format") ;
    yearString = new String(dateString.match(/y{2,4}/)) ;
    year = year.toString() ;
    if(yearString.length < 4) year = year.charAt(year.length - 2) + year.charAt(year.length - 1) ;
    dateString = dateString.replace(/d{2}/, day) ;
    dateString = dateString.replace(/M{2}/, month) ;
    dateString = dateString.replace(/y{2,4}/, year) ;
    this.currentHours = new Date().getHours() ;
    this.currentMinutes = new Date().getMinutes() ;
    this.currentSeconds = new Date().getSeconds() ;
    if (this.isDisplayTime) {
      var currentHours = (this.currentHours < 10) ? "0" + this.currentHours : this.currentHours;
      var currentMinutes = (this.currentMinutes < 10) ? "0" + this.currentMinutes : this.currentMinutes;
      var currentSeconds = (this.currentSeconds < 10) ? "0" + this.currentSeconds : this.currentSeconds;
      dateString = dateString.replace(/H{2}/, currentHours) ;
      dateString = dateString.replace(/m{2}/, currentMinutes) ;
      dateString = dateString.replace(/s{2}/, currentSeconds) ;
    } else {
      dateString = dateString.split(' ') ;
      this.dateField.value = dateString[0] ;
      this.hide() ;
      return ;
    }
    
    this.dateField.value = dateString ;
    this.hide() ;
  }
  return ;
} ;

UIDateTimePicker.prototype.setSeconds = function(object) {
		if(this.dateField) {
			var seconds = object.value;
			if (seconds >= 60) {
				object.value = seconds.substring(0,1);
				return;
			}
			if(seconds.length < 2) seconds = "0" + seconds;
			var timeString = this.currentDate.getHours() + ":" + this.currentDate.getMinutes() + ":" + seconds;
			this.currentDate.setSeconds(seconds);
			if(!this.currentDay) this.currentDay = this.currentDate.getDay();
			if(!this.currentMonth) this.currentMonth = this.currentDate.getMonth() + 1;
			if(!this.currentYear) this.currentYear = this.currentDate.getFullYear();
			if(this.isDisplayTime) timeString = this.currentDay + "/" + this.currentMonth + "/" + this.currentYear + " " + timeString;
			this.dateField.value = timeString;
	}
	return;
} ;

UIDateTimePicker.prototype.setMinus = function(object) {
		if(this.dateField) {
			var minus = object.value;
			if(minus >= 60){
				object.value = minus.substring(0,1);
				return;
			}
			if(minus.length < 2) minus = "0" + minus;
			this.currentDate.setMinutes(minus);
			var timeString = this.currentDate.getHours() + ":" + minus + ":" + this.currentDate.getSeconds();
			if(!this.currentDay) this.currentDay = this.currentDate.getDay();
			if(!this.currentMonth) this.currentMonth = this.currentDate.getMonth() + 1;
			if(!this.currentYear) this.currentYear = this.currentDate.getFullYear();
			if(this.isDisplayTime) timeString = this.currentDay + "/" + this.currentMonth + "/" + this.currentYear + " " + timeString;
			this.dateField.value = timeString;
	}
	return;
} ;

UIDateTimePicker.prototype.setHour = function(object) {
		if(this.dateField) {
			var hour = object.value;
			if (hour >= 24){
				object.value = hour.substring(0,1);	
				return;
			}
			if(hour.length < 2) hour = "0" + hour;
			this.currentDate.setHours(hour);
			var timeString = hour + ":" + this.currentDate.getMinutes() + ":" + this.currentDate.getSeconds();
			if(!this.currentDay) this.currentDay = this.currentDate.getDay();
			if(!this.currentMonth) this.currentMonth = this.currentDate.getMonth() + 1;
			if(!this.currentYear) this.currentYear = this.currentDate.getFullYear();
			if(this.isDisplayTime) timeString = this.currentDay + "/" + this.currentMonth + "/" + this.currentYear + " " + timeString;
			this.dateField.value = timeString;
	}
	return;
} ;

UIDateTimePicker.prototype.clearDate = function() {
  this.dateField.value = '' ;
  this.hide() ;
} ;

UIDateTimePicker.prototype.getDayOfWeek = function(year, month, day) {
  var date = new Date(year, month - 1, day) ;
  return date.getDay() ;
} ;

UIDateTimePicker.prototype.getDaysInMonth = function(year, month) {
	return [31, ((!(year % 4 ) && ( (year % 100 ) || !( year % 400 ) ))? 29:28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month];
} ;

eXo.cs.UIDateTimePicker = new UIDateTimePicker('UICalendarControl') ;