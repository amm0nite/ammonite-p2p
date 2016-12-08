

// ===========
// === app ===
// ===========

App = function() {
	this.active_section = "";
	this.sections = {};
};

app = new App();

app.init = function() {
	$("#f2f_menu a").click(function() {
		app.activate($(this).attr('data-section'), $(this).html());
	});
};
app.initSections = function() {
	for (n in app.sections) {
		app.sections[n].init();
	}
};
app.activate = function(section_name) {
	var section = app.sections[section_name];
	app.active_section = section.name;
	setTimeout("app.sections."+section.name+".update(true)", 0);

	$("#f2f_menu a").each(function() {
		if ($(this).attr('data-section') == section.name) {
			$(this).parent("li").addClass("active");
		}
		else {
			$(this).parent("li").removeClass("active");
		}
	});

	$('#f2f_title').html(section.title);
	$('.f2f_section').hide();
	$('#f2f_section_'+section.name).show();
};
app.update = function() {
	app.sections[app.active_section].update();
};

// ===============
// === section ===
// ===============

Section = function(name, title) {
	this.name = name;
	this.title = title;
	this.data = null;
	app.sections[name] = this;

	this.init = function() {};
	this.update = function() {};
	this.write = function() {};
}

// ===============
// === friends ===
// ===============

var friendsSection = new Section('friends', 'Friends');
friendsSection.init = function() {
	document.getElementById('friends_upload').addEventListener(
		'change',
		function(evt) {
			//Retrieve the first File from the FileList object
			var f = evt.target.files[0];
			if (f) {
				var r = new FileReader();
				r.onload = function(e) {
					var contents = e.target.result;
					$('#f2f_section_friends input[name=identity]').attr('value', contents);
				}
				r.readAsBinaryString(f);
			}
			else {
				alert("Failed to load file");
			}
		},
		false
	);
	$("#f2f_section_friends form").submit(function() {
		var data = {};
		data.identity = $(this).children('input[name=identity]').attr('value');
		data.address = $(this).children('input[name=address]').attr('value');
		$.post("service/friendship", data, function(data) { 
			app.sections.friends.update();
		});
		return false;
	});
	$(".friends_button_delete").live("click", function() {
		var data = {};
		data.address = $(this).attr('data-address');
		$.ajax({
			type: 'DELETE',
			url: 'service/friendship',
			data: data,
			success: function() {
				app.sections.friends.update();
			}
		});
	});
};
friendsSection.update = function() {
	$.getJSON('service/friendship', function(res) {
		app.sections.friends.data = res;
		app.sections.friends.write();
	});
}
friendsSection.write = function() {
	var html = "";
	var data = this.data;
	html += "<tr><th>ID</th><th>Nickname</th><th>Host</th><th>Port</th><th colspan=\"2\">Options</th></tr>";
	for(var i=0; i<data.length; i++) {
		f = data[i];
		html += "<tr>"
			html += "<td>"+_e(f.id)+"</td>";
			html += "<td>"+_e(f.nickname)+"</td>";
			html += "<td>"+_e(f.host)+"</td>";
			html += "<td>"+_e(f.port)+"</td>";
			html += '<td><button data-id="'+_e(f.host)+':'+_e(f.port)+'" class="friends_button_chat btn btn-mini">Chat</a></td>';
			html += '<td><button data-id="'+_e(f.host)+':'+_e(f.port)+'" class="friends_button_delete btn btn-mini btn-danger">Delete</button></td>';
		html += "</tr>";
	}
	$('#f2f_section_friends table').first().html(html);
};

// ============
// === chat ===
// ============

var chatSection = new Section('chat', 'Chat');
chatSection.init = function() {
	$("#f2f_section_chat form").submit(function() {
		var input = $(this).children('input[name=text]');
		var data = {};
		data.text = input.attr('value');
		data.range = app.sections.chat.active_section;
		
		$.post("service/chat", data, function(data) { 
			app.sections.chat.update();
			input.attr('value', '');
		});
		return false;
	});

	$("#chat_section_menu a").click(function() {
		app.sections.chat.activate($(this).attr('data-chat-section'));
	});

	this.activate('public');
};
chatSection.update = function() {
	var data = {};
	data.range = this.active_section;

	$.getJSON('service/chat', data, function(res) {
		app.sections.chat.data = res;
		app.sections.chat.write();
	});
};
chatSection.write = function() {
	var html = "";
	var data = this.data;
	for(var i=0; i<data.length; i++) {
		s = data[data.length-(i+1)];
		var d = new Date(parseInt(s.time));
		var displayDate = d.getHours()+':'+d.getMinutes()+':'+d.getSeconds();
		html += "<tr>";
			html += "<td>"+displayDate+"</td>";
			html += "<td>"+_e(s.hops)+"</td>";
			html += "<td>"+_e(s.author)+"</td>";
			html += "<td>"+_e(s.text)+"</td>";
		html += "</tr>";
	}
	$('#f2f_section_chat table').first().html(html);
};
chatSection.activate = function(section_name) {
	this.active_section = section_name;

	$("#chat_section_menu a").each(function() {
		if ($(this).attr('data-chat-section') == section_name) {
			$(this).parent("li").addClass("active");
		}
		else {
			$(this).parent("li").removeClass("active");
		}
	});

	this.update();
};

// ================
// === settings ===
// ================

var settingsSection = new Section('settings', 'Settings');
settingsSection.init = function() {
	$("#settings_button_save").click(function() {
		var button = $(this);
		button.removeClass('btn-primary').addClass('btn-warning').html('Saving...');
		data = {};
		data.nickname = $('#settings_nickname').attr('value');
		$.post("service/identity", data, function(data) { 
			app.sections.settings.update(true);
			button.removeClass('btn-warning').addClass('btn-success').html('Saved');
			setTimeout("$('#settings_button_save').removeClass('btn-success').addClass('btn-primary').html('Save');", 1000);
		});
	});
	$("#settings_button_reset").click(function() {
		app.sections.settings.update(true);
	});
};
settingsSection.update = function(rly) {
	if (rly) {
		$.getJSON('service/identity', function(res) {
			app.sections.settings.data = res;
			app.sections.settings.write();
		});
	}
};
settingsSection.write = function() {
	$('#settings_nickname').attr('value', _e(this.data.nickname));
};

// ============
// === main ===
// ============

$(document).ready(function() {
	app.init();
	app.initSections();
	
	app.activate('friends');
	var timer = setInterval(app.update, 1000);
});

// =============
// === tools ===
// =============

_e = function(str) {
	return htmlEntities(str);
}
htmlEntities = function(str) {
    return String(str)
    	.replace(/&/g, '&amp;')
    	.replace(/</g, '&lt;')
    	.replace(/>/g, '&gt;')
    	.replace(/"/g, '&quot;');
};