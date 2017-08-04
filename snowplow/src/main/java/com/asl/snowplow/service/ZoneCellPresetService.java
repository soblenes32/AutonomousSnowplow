package com.asl.snowplow.service;

import java.awt.Point;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.model.ZoneCell;
import com.asl.snowplow.service.websocket.ZoneCellWebsocketService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ZoneCellPresetService {
	
	@Inject
	WorldState worldState;
	
	@Inject
	ZoneCellWebsocketService zoneCellWebsocketService;
	
	private static final String PRESET_LOCAL_DIRECTORY = "/home/pi/snowplow/zonecellpresets";
	
	@PostConstruct
	private void init(){
		//Create the preset local directory if it doesn't exist
		File file = new File(PRESET_LOCAL_DIRECTORY);
		file.mkdirs();
	}
	
	public List<String> listPresets(){
		File directory = new File(PRESET_LOCAL_DIRECTORY);
		return Arrays.asList(directory.listFiles()).stream()
			.map(f->f.getName())
			.collect(Collectors.toList());
	}
	
	public void savePresetToFile(String presetName){
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(new File(PRESET_LOCAL_DIRECTORY+"/"+presetName), worldState.getZoneCellMap().values());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void loadPresetFromFile(String presetName){
		ObjectMapper mapper = new ObjectMapper();
		try {
			TypeReference<List<ZoneCell>> typeRef = new TypeReference<List<ZoneCell>>() {};
			List<ZoneCell> zcList = mapper.readValue(new File(PRESET_LOCAL_DIRECTORY+"/"+presetName), typeRef);
			Map<Point, ZoneCell> zoneCellMap = new HashMap<>();
			for(ZoneCell zc: zcList) {
				zoneCellMap.put(zc.getCoordinates(), zc);
			}
			worldState.setZoneCellMap(zoneCellMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		zoneCellWebsocketService.sendZoneCellUpdate();
	}
	
}
