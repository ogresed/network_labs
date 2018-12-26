import node.Child;
import node.Node;

public class Application {
    private static final int DEFAULT_LOSS_PERCENTAGE =  20;

    public static void main(String[] args) {
        //check number of arguments
        int numberOfArgs = args.length;
        if(numberOfArgs <= 2 || numberOfArgs == 4) {
            System.out.println("Enter:\n1) self name 2) loss percentage 3) self port" +
                    "\nIf you're not root: 4) parent's host and 5) parent's port");
            return;
        }
        try {
            //load arguments
            String myName = args[0];
            int lossPercentage = Math.abs(Math.round(Float.parseFloat(args[1]))) % 100;
            lossPercentage = lossPercentage != 0 ? lossPercentage : DEFAULT_LOSS_PERCENTAGE;
            int myPort = Integer.parseInt(args[2]);
            //check arguments and create node
            Node node;
            if (numberOfArgs >= 5) {
                String parentsHost = args[3];
                int parentsPort = Integer.parseInt(args[4]);
                node = new Child(myName, lossPercentage, myPort, parentsHost, parentsPort);
            }
            else {
                node = new Node(myName, lossPercentage, myPort);
            }
            //start working
            node.start();
        }
        catch (NumberFormatException e) {
            System.out.println("Enter argument(s) as number");
        }
    }
}
