package com.orange.gameserver.robot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    ExecutorService executor = Executors.newFixedThreadPool(RobotManager.MAX_ROBOT_USER);
    
    RobotManager robotManager = RobotManager.getInstance();
	
    public boolean isEnableRobot() {
		String robot = System.getProperty("config.robot");
		if (robot != null && !robot.isEmpty()){
			return (Integer.parseInt(robot) == 1);
		}
		return false; // default
	}	
	
    public void startNewRobot(int sessionId) {
    	if (!isEnableRobot()){
    		GameLog.info(sessionId, "Robot not enabled for launch");
    		return;
    	}
    	
    	RobotClient client = robotManager.allocNewClient(sessionId); 
    	if (client == null){
    		GameLog.info(sessionId, "start new robot but no robot client available");
    		return;
    	}
    	
    	executor.execute(client);
	}
    
	public void finishRobot(final RobotClient robotClient) {		
		executor.execute(new Runnable(){
			@Override
			public void run() {
				robotManager.deallocClient(robotClient);
				robotClient.stopClient();
			}		
		});
	} 	
    
    
}
