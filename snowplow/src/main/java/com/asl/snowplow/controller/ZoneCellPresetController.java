package com.asl.snowplow.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.asl.snowplow.service.ZoneCellPresetService;

@Controller
@RequestMapping("/zonecell/preset")
public class ZoneCellPresetController {
	
	@Inject
	ZoneCellPresetService zoneCellPresetService;
	
	@RequestMapping(method=RequestMethod.GET)
	public @ResponseBody List<String> listPresets() {
		return zoneCellPresetService.listPresets();
	}

	@RequestMapping(value="/load/{presetName}", method=RequestMethod.GET)
	public @ResponseBody List<String> loadPreset(@PathVariable("presetName") String presetName) {
		zoneCellPresetService.loadPresetFromFile(presetName, false);
		return zoneCellPresetService.listPresets();
	}
	
	@RequestMapping(value="/save/{presetName}", method=RequestMethod.GET)
	public @ResponseBody List<String> savePreset(@PathVariable("presetName") String presetName) {
		System.out.println("saving preset: " + presetName);
		zoneCellPresetService.savePresetToFile(presetName);
		return zoneCellPresetService.listPresets();
	}
}