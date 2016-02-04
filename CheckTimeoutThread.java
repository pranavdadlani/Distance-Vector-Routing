import java.util.Map.Entry;

public class CheckTimeoutThread extends Thread {

	public void run() {
		while (true) {
			for (Entry<String, Integer> entry : SenderThread.neigborTimeout.entrySet()) {
				if (entry.getValue() == 20) {
					System.out.println("Router " + SenderThread.portToip(entry.getKey()) + " is down");
				}
			}

		}

	}
}
