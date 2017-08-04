angular.module("SnowplowApp")
.service("shutdownAjaxService", function($http){
	var self = this;
	
	this.shutdownServer = function(){
		return $http({
			method: 'POST',
			url: '/shutdown'
		})
	};
}); 