angular.module("SnowplowApp")
.service("modelService", function($http, $q){
	var self = this;
	
	this.controlMode = 0; //[0=off, 1=manual, 2=auto]
	
	
	
//	/*************************************************************************
//	 * The logged-in user's data
//	 *************************************************************************/
//	this.myData = {
//		userT:null, //User's account object
//		userProfileT:null, //User's profile object
//		fileList:[], //Metadata for all files that the user has uploaded (does not include file data)
//		applicationList:[], //All rotation applications that the user has submitted
//		profileReminded:false, //Flag to ensure that profile reminder only presents once
//		insitutionProgramCoordinatorList:[], //Only for users in or requested role PROGRAM_COORDINATOR. List of InstutionProgCoordinatorT that user is assigned to
//		siteAdminList: [] //Only for users in or requested role SITE_ADMINISTRATOR. List of siteAdministratorT that user is assigned to
//	};
//	
//	/*************************************************************************
//	 * "Static" data for select controls and lookups
//	 *************************************************************************/
//	this.states = null; //State list with abbrev and name
//	this.institutions = []; //Array of all institution obj with id and name
//	this.instPrograms = []; //Array of all institution programs with instId and prog name
//	this.hpLocations = []; //Flat array of site and departments
//	this.nestedSites = {}; //Index of siteName -> department array
//	this.specialties = []; //Array of string specialties
//	this.courseNames = []; //Array of string course names
//	
//	/*************************************************************************
//	 * Context selections for managing application UI state and presentation
//	 *************************************************************************/
//	this.rotationSummary = null; //Reference to the most recent rotation selected by the user in the "find rotation" module
//	this.isLeftNavOpenMobile = false; //For mobile devices, default left nav to open
//	
//	/*************************************************************************
//	 * Returns url pointing at specified user's avatar (for img src element)
//	 *************************************************************************/
//	this.userAvatarUrl = function(userProfileT){
//		if(!userProfileT || !userProfileT.imageFileId) return null;
//		return (userProfileT.imageFileId)?"files/"+userProfileT.imageFileId:null;
//	}
//	
//	/*************************************************************************
//	 * Returns url pointing at logged-in user's avatar (for img src element)
//	 *************************************************************************/
//	this.avatarUrl = function(){
//		return self.userAvatarUrl(self.myData.userProfileT);
//	}
//	
//	/*************************************************************************
//	 * All data that must be eager-loaded into the browser session
//	 *************************************************************************/
//	var initDataLoad = function(){
//		return $q.all([
//			userAjaxService.getMyUser().then(function(response){ //Load current user object
//				self.myData.userT = response.data;
//				if(!self.myData.userT.requestedRole){
//					self.myData.userT.requestedRole = self.myData.userT.role;
//				}
//			}).then(function(){
//				//After loading user data, conditionally load ROLE-specific data
//				
//				//If institute coordinator, load 
//				if(self.myData.userT.role == "ROLE_INSTITUTE_COORDINATOR" || self.myData.userT.requestedRole == "ROLE_INSTITUTE_COORDINATOR"){
//					institutionProgCoordinatorAjaxService.getMyInstitutionProgCoordinatorT().then(function(response){
//						self.myData.insitutionProgramCoordinatorList = response._embedded.ipc;
//					});
//				}
//				
//				//If site administrator, load
//				if(self.myData.userT.role == "ROLE_SITE_ADMINISTRATOR" || self.myData.userT.requestedRole == "ROLE_SITE_ADMINISTRATOR"){
//					siteAdministratorAjaxService.getSiteAdministratorTByUserId(self.myData.userT.userId).then(function(response){
//						self.myData.siteAdminList = response.data;
//					}, function(error){ console.dir(error); });
//				}
//				
//			}),
//			userAjaxService.getMyUserProfile().then(function(response){ //Load current user profile object
//				self.myData.userProfileT = response.data;
//			}),
//			rotationAjaxService.getMyRotationApplications().then(function(response){ //Load current user rotation applications
//				self.myData.applicationList = response.data;
//			}),
//			$http.get("data/states.json").then(function(response) { //Load list of states
//				self.states = response.data;
//		    }),
//		    $http.get("data/courses.json").then(function(response) { //Load list of courses
//				self.courseNames = response.data;
//		    }),
//		    $http.get("data/specialties.json").then(function(response) { //Load list of specialties
//				self.specialties = response.data;
//		    }),
//		    $http.get("programs").then(function(response) { //Load list of clinical programs
//		    	self.instPrograms = response.data;
//		    	self.institutions = [];
//		    	var instAssocArr = {};
//		    	if(self.instPrograms && Array.isArray(self.instPrograms)){
//		    		self.instPrograms.forEach(function(instProg){
//		    			instAssocArr[instProg.institutionId] = {institutionId:instProg.institutionId, institutionName:instProg.institutionName};
//		    		});
//		    		Object.keys(instAssocArr).forEach(function(key){
//		    			self.institutions.push(instAssocArr[key]);
//		    		});
//		    	}
//		    }),
//		    $http.get("data/academic-credentials.json").then(function(response) { //Load list of all academic credentials
//				self.academicCredentialOptions = response.data;
//		    }),
//		    $http.get("site").then(function(response){ //Load list of HP clinical sites
//				self.hpLocations = response.data._embedded.site;
//				var ns = self.nestedSites;
//				self.hpLocations.forEach(function(loc){
//					ns[loc.clinicalSiteName.toUpperCase()] = ns[loc.clinicalSiteName.toUpperCase()]||[];
//					ns[loc.clinicalSiteName.toUpperCase()].push(loc);
//				});
//		    }),
//		    fileAjaxService.listFiles().then(function(response){ //Load list of user's uploaded files
//		    	self.myData.fileList = response.data;
//		    })
//		]);
//	};
//	
//	/*************************************************************************
//	 * Fetch institutions by search string
//	 *************************************************************************/
//	this.filterInstitutions = function(searchStr){
//		return self.institutions.filter(function(inst){
//			return (inst.institutionName.toLowerCase().indexOf(searchStr.toLowerCase()) > -1);
//		});
//	};
//	
//	/*************************************************************************
//	 * Fetch programs by search string
//	 *************************************************************************/
//	this.filterPrograms = function(instId, searchStr){
//		if(!instId || !searchStr) return null;
//		return self.instPrograms.filter(function(prog){
//			return (prog.institutionId == instId) && (prog.programName.toLowerCase().indexOf(searchStr.toLowerCase()) > -1);
//		});
//	};
//	
//	/*************************************************************************
//	 * Fetch programs by institution
//	 *************************************************************************/
//	this.filterProgramsByInst = function(instId){
//		if(!instId) return null;
//		return self.instPrograms.filter(function(prog){
//			return (prog.institutionId == instId && prog.programName != "");
//		});
//	};
//	
//	/*************************************************************************
//	 * Fetch programs by institution
//	 *************************************************************************/
//	this.getProgramById = function(institutionProgramId){
//		if(!self.instPrograms) return null;
//		return self.instPrograms.find(function(prog){
//			return (prog.institutionProgramId == institutionProgramId);
//		});
//	};
//	
//	/*************************************************************************
//	 * Fetch clinics by search string
//	 *************************************************************************/
//	this.getClinicById = function(clinicId){
//		return self.hpLocations.find(function(loc){
//			return (loc.clinicalSiteId == clinicId);
//		});
//	};
//
//	/*************************************************************************
//	 * Promise callback for session-data-dependent modules to check
//	 *************************************************************************/
//	this.initDataLoadPromise = initDataLoad();
}); 