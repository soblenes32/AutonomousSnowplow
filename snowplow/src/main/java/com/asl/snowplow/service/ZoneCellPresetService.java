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

import org.springframework.beans.factory.annotation.Value;
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
	
	//@Inject
	//ZoneCellWebsocketService zoneCellWebsocketService;
	
	@Inject
	ClientFetchQueueService clientFetchQueueService;
	
	@Value("${snowplow.config.zonecellpresetpath}")
	String zonecellPresetDefaultPath;
	
	@Value("${snowplow.config.zonecellpresetdir}")
	String zonecellPresetDir;
	
	@PostConstruct
	private void init(){
		loadPresetFromFile(zonecellPresetDefaultPath);
	}
	
	public List<String> listPresets(){
		File directory = new File(zonecellPresetDir);
		return Arrays.asList(directory.listFiles()).stream()
			.map(f->f.getName())
			.collect(Collectors.toList());
	}
	
	public void savePresetToFile(String presetName){
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(new File(zonecellPresetDir+"/"+presetName), worldState.getZoneCellMap().values());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public boolean loadPresetFromFile(String fullFilePath){
		return loadPresetFromFile(fullFilePath, true);
	}
	
	public boolean loadPresetFromFile(String fileName, boolean isFullPath){
		//Append the full path if only the file was provided
		String fullFilePath = fileName;
		if(!isFullPath) {
			fullFilePath = zonecellPresetDir +fullFilePath;
		}
		//Determine if config file exists
		File f = new File(fullFilePath);
		if(!f.exists()) {
			System.out.println("Unable to locate a zonecell preset file at " + fullFilePath);
			return false;
		}
				
		ObjectMapper mapper = new ObjectMapper();
		try {
			TypeReference<List<ZoneCell>> typeRef = new TypeReference<List<ZoneCell>>() {};
			List<ZoneCell> zcList = mapper.readValue(f, typeRef);
			Map<Point, ZoneCell> zoneCellMap = new HashMap<>();
			for(ZoneCell zc: zcList) {
				zoneCellMap.put(zc.getCoordinates(), zc);
			}
			worldState.setZoneCellMap(zoneCellMap);
		} catch (Exception e) {
			System.out.println("*IMPORTANT* - NOT loading default zonecell file: " + zonecellPresetDir + " because the file was not found.");
			e.printStackTrace();
		}
		//zoneCellWebsocketService.sendZoneCellUpdate();
		clientFetchQueueService.setZoneCellList(worldState.getZoneCellMap().values());
		
		System.out.println("Zonecell presets were loaded. Size: " + worldState.getZoneCellMap().size());
		return true;
	}
	
}
