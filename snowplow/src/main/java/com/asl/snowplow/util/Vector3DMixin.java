package com.asl.snowplow.util;

import org.apache.commons.math3.geometry.Space;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Vector3DMixin extends Vector3D{
	@JsonIgnore public abstract Space getSpace();
	@JsonIgnore public abstract Vector3D getZero();
	@JsonIgnore public abstract double getNorm1();
	@JsonIgnore public abstract double getNorm();
	@JsonIgnore public abstract double getNormSq();
	@JsonIgnore public abstract double getNormInf();
	@JsonIgnore public abstract double getAlpha();
	@JsonIgnore public abstract double getDelta();
	@JsonIgnore public abstract boolean isNaN();
	@JsonIgnore public abstract boolean isInfinite();

	@JsonCreator
	Vector3DMixin(@JsonProperty("x") double x,
			@JsonProperty("y") double y,
			@JsonProperty("z") double z) { 
		super(x,y,z); 
	}
}
