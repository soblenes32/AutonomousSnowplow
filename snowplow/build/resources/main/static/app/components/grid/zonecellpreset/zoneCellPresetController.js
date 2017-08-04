angular.module("SnowplowApp")
.controller("ZoneCellPresetCtrl", function($scope, $mdDialog, $mdToast, zoneCellPresetAjaxService){
	$scope.preset = {name:""};
	$scope.presetList = [];
	
	$scope.updatePresetList = function(){
		zoneCellPresetAjaxService.listPresets().then(function(response){
			$scope.presetList = response.data;
		});
	};
	$scope.updatePresetList();
	
	$scope.savePreset = function(){
		$scope.preset.name.replace(/\W/g, '');
		if(!$scope.preset.name || $scope.preset.name == "") return;
		
		zoneCellPresetAjaxService.savePreset($scope.preset.name).then(function(response){
			$mdToast.show($mdToast.simple().textContent('Preset saved').hideDelay(3000));
			$scope.presetList = response.data;
		}, function(){
			$mdToast.show($mdToast.simple().textContent('Failed to save preset').hideDelay(3000));
		});
	};
	
	$scope.loadPreset = function(presetName){
		zoneCellPresetAjaxService.loadPreset(presetName).then(function(){
			$mdToast.show($mdToast.simple().textContent('Preset loaded').hideDelay(3000));
		}, function(){
			$mdToast.show($mdToast.simple().textContent('Failed to load preset').hideDelay(3000));
		});
	};
});