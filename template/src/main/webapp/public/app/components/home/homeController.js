angular.module("CTTApp")
.controller("HomeCtrl", function($scope, $location, modelService){
	$scope.sidenav = {url: "app/components/sidenav/sidenav.html"};
	
	modelService.initDataLoadPromise.then(function(){
		//If user is ROLE_STUDENT and address field is not filled out, then prompt user to do profile
		if(modelService.myData.userT.role == "ROLE_STUDENT" && !modelService.myData.userProfileT.address1 && !modelService.myData.profileReminded){
			modelService.myData.profileReminded = true;
			$location.path("/profile");
		}
	});
});