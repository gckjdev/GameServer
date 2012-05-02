package com.orange.gameserver.draw.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DrawAction {
	
	public static int DRAW_ACTION_TYPE_DRAW = 0;
	public static int DRAW_ACTION_TYPE_CLEAN = 1;
	
	final int actionType;
	final float width;
	final int color;
	final List<Integer> pointList;
	
	public DrawAction(){
		this.actionType = DRAW_ACTION_TYPE_CLEAN;
		this.width = 0;
		this.color = 0;
		this.pointList = Collections.emptyList();
	}
	
	public DrawAction(float width, int color, List<Integer> pointList){
		this.actionType = DRAW_ACTION_TYPE_DRAW;
		this.width = width;
		this.color = color;
		this.pointList = new ArrayList<Integer>(pointList);
	}

}


