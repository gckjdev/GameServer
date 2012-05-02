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

    ExecutorService executor = Executors.newFixedThreadPool(1);
    
    RobotManager robotManager = RobotManager.getInstance();

    public void startNewRobot(int sessionId) {
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
