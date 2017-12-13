angular.module("SnowplowApp")
.service("zoneCellDataModelService", function(){
	var self = this;
	
	/**********************************************************************
	 * ZoneCell object reference
	 * 
	 * {
	 * coordinates : {x:0,y:0},
	 * snowVolume : 0,
	 * obstruction : false,
	 * plowZone : false,
	 * plowedSnowZone : false,
	 * parkZone : false
	 * }
	 **********************************************************************/

	this.zoneCellArr = [];
	this.zoneCellMap = {};
	this.zoneCellUpdateSequence = 0;
	
	this.updateZoneCells = function(updateZoneCellArr){
		//console.log("Updating zone cells: " + updateZoneCellArr.length);
		updateZoneCellArr.forEach(function(zoneCell){
			self.zoneCellMap[self.generateKeyFromZoneCell(zoneCell)] = zoneCell;
		});
		self.zoneCellArr = Object.values(self.zoneCellMap);
		self.zoneCellUpdateSequence++;
	};
	
	/**************************************************
	 * Returns an INDEX object
	 * Args x and y should be mm units
	 * Returns an object containing the cell index
	 **************************************************/
	this.coordsToCellIdx = function(x,y){
		var xIdx = Math.floor(x/100);
		var yIdx = Math.floor(y/100);
		return { x:xIdx, y:yIdx };
	}
	
	/**************************************************
	 * Returns an OBJECT
	 * Args xCoord and yCoord should be mm units
	 * returns the index of the cell where point is
	 **************************************************/
	this.getZoneCellFromCoords = function(x, y){
		var cellIdxObj = self.coordsToCellIdx(x,y);
		return self.zoneCellMap[self.generateKey(cellIdxObj.x, cellIdxObj.y)];
	}
	
	/************************************************************************************
	 * Generate cell index pairs for all cells in the same square meter as input coords
	 * input: [1,1][-1,-1]; output 
	 ************************************************************************************/
	this.getM2ZoneCellIdxArrFromCoords = function(xCoord, yCoord){
		//console.log("input x,y: " + xCoord + ", " + yCoord);
		var zoneCellArr = [];
		var inCellIdxObj = self.coordsToCellIdx(xCoord, yCoord);
		//console.log("index x,y: " + inCellIdxObj.x + ", " + inCellIdxObj.y);
		var minXIdx = (inCellIdxObj.x>=0)?(inCellIdxObj.x - (inCellIdxObj.x%10)): (((inCellIdxObj.x+1) - ((inCellIdxObj.x+1)%10))-10); //(inCellIdxObj.x - (inCellIdxObj.x%10)) - 10;
		var minYIdx = (inCellIdxObj.y>=0)?(inCellIdxObj.y - (inCellIdxObj.y%10)): (((inCellIdxObj.y+1) - ((inCellIdxObj.y+1)%10))-10);//[-1,-10]=-1, [-11, -20]=-20 ... 
		//console.log("iterating X: " + minXIdx + " to " + (minXIdx+10));
		//console.log("iterating Y: " + minYIdx + " to " + (minYIdx+10));
		for(var x=minXIdx; x < (minXIdx+10); x++){
			for(var y=minYIdx; y < (minYIdx+10); y++){
				zoneCellArr.push({x:x,y:y});
			}
		}
		return zoneCellArr;
	}
	
	/************************************************************************************
	 * For each zoneCell index, update the associated property with the new value
	 * If the corresponding zoneCell object does not yet exist, then initialize it
	 * 
	 * isResetAll = if true, then resets all flags on the object to false before 
	 * updating propValue
	 ************************************************************************************/
	this.setZoneCellProp = function(zoneCellIdxArr, propName, propValue, isResetAll){
		var updateArr = []; //Array of all modified/created zoneCellObj to transmit to the server
		zoneCellIdxArr.forEach(function(idxObj){
			//Fetch the zoneCellObj if it already exists
			var zoneCellObj = self.zoneCellMap[self.generateKey(idxObj.x, idxObj.y)];
			//if not exists, initialize it and add it to the map and list
			if(!zoneCellObj){
				zoneCellObj = self.initZoneCellObj(idxObj);
				
			}
			//if isResetAll is true, then flip all flags to false
			if(isResetAll){
				zoneCellObj.snowVolume = 0;
				zoneCellObj.obstruction = false;
				zoneCellObj.plowZone = false;
				zoneCellObj.plowedSnowZone = false;
				zoneCellObj.parkZone = false;
			}
			//set the update property
			zoneCellObj[propName] = propValue;
			updateArr.push(zoneCellObj);
		});
		//Update the server
		return updateArr;
	};
	
	/**************************************************
	 * Initialize zoneCell Object
	 **************************************************/
	this.initZoneCellObj = function(zoneCellIdxObj){
		var zoneCellObj = {
			coordinates : zoneCellIdxObj,
			snowVolume : 0,
			obstruction : false,
			plowZone : false,
			plowedSnowZone : false,
			parkZone : false
		};
		self.zoneCellMap[self.generateKey(zoneCellIdxObj.x, zoneCellIdxObj.y)] = zoneCellObj;
		self.zoneCellArr.push(zoneCellObj);
		return zoneCellObj;
	};
	
	/**************************************************
	 * Args xCoord and yCoord should be decimeter units
	 **************************************************/
	this.getZoneCell = function(x, y){
		return self.zoneCellMap[self.generateKey(x,y)];
	}
	
	this.generateKeyFromZoneCell = function(zoneCell){
		return self.generateKey(zoneCell.coordinates.x, zoneCell.coordinates.y);
	}
	
	this.generateKey = function(x, y){
		return x+"~"+y;
	}
	
	/**************************************************
	 * Returns an array of INDEX object
	 * Args xCoord and yCoord should be mm units
	 * returns the indexes of all cells 
	 **************************************************/
//	this.getZoneCellIdxArrFromCoords = function(x1Coord, y1Coord, x2Coord, y2Coord){
//		var zoneCellArr = [];
//		var cell1IdxObj = self.coordsToCellIdx(x1Coord, y1Coord);
//		var cell2IdxObj = self.coordsToCellIdx(x2Coord, y2Coord);
//		
//		var xmin = (cell1IdxObj.x < cell2IdxObj.x)?cell1IdxObj.x:cell2IdxObj.x;
//		var xmax = (cell1IdxObj.x > cell2IdxObj.x)?cell1IdxObj.x:cell2IdxObj.x;
//		var ymin = (cell1IdxObj.y < cell2IdxObj.y)?cell1IdxObj.y:cell2IdxObj.y;
//		var ymax = (cell1IdxObj.y > cell2IdxObj.y)?cell1IdxObj.y:cell2IdxObj.y;
//		
//		
//		for(var x=xmin; x <= xmax; x++){
//			for(var y=ymin; y <= ymax; y++){
//				zoneCellArr.push({x:x,y:y});
//			}
//		}
//		return zoneCellArr;
//	}
}); 