angular.module("SnowplowApp")
.controller("AnchorConfigCtrl", function($scope, $mdDialog, $mdToast, telemetryDataModelService,telemetryAjaxService){
	$scope.tdms = telemetryDataModelService;
	$scope.selectedPreset = null;
	
	$scope.presets = [
		{
			name:"Home",
			anchors:[
				{name:"0x681C", position:{x:"0", y:"0", z:"1"}},
				{name:"0x6165", position:{x:"3000", y:"0", z:"1"}},
				{name:"0x6169", position:{x:"0", y:"3000", z:"1"}},
				{name:"0x6879", position:{x:"3000", y:"3000", z:"1"}}]
		}
	];
	
	$scope.updateToPreset = function(){
		$scope.selectedPreset.anchors.forEach(function(presetAnchor){
			var anchor = $scope.tdms.anchorArr.find(function(anchor){
				return anchor.name == presetAnchor.name;
			});
			if(anchor){
				anchor.position.x = presetAnchor.position.x;
				anchor.position.y = presetAnchor.position.y;
				anchor.position.z = presetAnchor.position.z;
			}else{
				console.log("unable to find a matching anchor!");
			}
		});
	};
	
	
	$scope.formSubmit = function(){
		telemetryAjaxService.setAnchorsManual(telemetryDataModelService.anchorArr).then(function(response){
			$mdToast.show($mdToast.simple().textContent('Anchors updating. Please wait...').hideDelay(3000));
		}, function(err){
			console.dir(err); 
			$mdToast.show($mdToast.simple().textContent('Anchor update failed').hideDelay(3000));
		});
		$mdDialog.hide();
	}
});