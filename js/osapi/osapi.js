var osapi = osapi || {};

osapi.people = function() {
	return {
		get: function(options) {
			result = gadgets.json.parse(e4RPC('osapi.people.get', gadgets.json.stringify(options)));
			return {
				execute: function(callback) { callback(result) ; } 
			};
		},
		
		getViewer: function(options) {
			options = options || {};
			options.userId = "@viewer";
			options.groupId = "@self";
			return osapi.people.get(options);
		},
		
		getViewerFriends: function(options) {
			options = options || {};
			options.userId = "@viewer";
			options.groupId = "@friends";
			return osapi.people.get(options);
		},
		
		getOwner: function(options) {
			options = options || {};
			options.userId = "@owner";
			options.groupId = "@self";
			return osapi.people.get(options);
		},
		
		getOwnerFriends: function(options) {
			options = options || {};
			options.userId = "@owner";
			options.groupId = "@friends";
			return osapi.people.get(options);
		}
	};
}();