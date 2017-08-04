angular.module("SnowplowApp")
.service("zoneCellPresetAjaxService", function($http){
	var self = this;
	
	this.loadPreset = function(presetName){
		return $http({
			method: 'GET',
			url: '/zonecell/preset/load/'+presetName
		});
	};
	
	this.savePreset = function(presetName){
		return $http({
			method: 'GET',
			url: '/zonecell/preset/save/'+presetName
		});
	};
	
	this.listPresets = function(){
		return $http({
			method: 'GET',
			url: '/zonecell/preset'
		});
	};
}); 