package com.orange.gameserver.robot;

import com.orange.gameserver.draw.utils.GameLog;
import com.orange.gameserver.robot.client.RobotClient;
import com.orange.gameserver.robot.manager.RobotManager;

public class RobotService {

	
	// thread-safe singleton implementation
    private static RobotService service = new RobotService();     
    private RobotService(){		
	} 	    
    public static RobotService getInstance() { 
    	return service; 
    }
    
    RobotManager robotManager = RobotManager.getInstance();

    public void startNewRobot(int sessionId) {
    	RobotClient client = robotManager.allocNewClient(sessionId); 
    	if (client == null){
    		GameLog.info(sessionId, "start new robot but no robot client available");
    		return;
    	}
    	
    	client.run();
	}
	public void finishRobot(RobotClient robotClient) {
		robotManager.deallocClient(robotClient);
	} 	
    
    
}
