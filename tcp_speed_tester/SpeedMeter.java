import java.util.TimerTask;

class SpeedMeter extends TimerTask {
    private long startTime;
    private long previousTime;
    private long previouslyRead;
    private RunnableServer runnableServer;

    SpeedMeter(RunnableServer runnableServer) {
        this.startTime = System.currentTimeMillis();
        this.runnableServer = runnableServer;
        this.previousTime = this.startTime;
    }

    @Override
    public void run() {
        long totalRead = runnableServer.getTotalRead();
        if (totalRead != 0) {
            long currentTime = System.currentTimeMillis();
            //here byte per millisecond
            double instantaneousSpeed = (double) (totalRead - previouslyRead) / (double) (currentTime - previousTime);
            System.out.println("Instantaneous speed: " + Math.round((instantaneousSpeed)) +" byte per millisecond");
            double averageSpeed = (double) totalRead / (double) (currentTime - startTime);
            System.out.println("Average speed: " + Math.round((averageSpeed)) +" byte per millisecond");
            previouslyRead = totalRead;
            previousTime = currentTime;
        }
    }
}