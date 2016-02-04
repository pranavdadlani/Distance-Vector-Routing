import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class RipMain {

	public static String getPort(String ip) {
		String[] ipNos = ip.split("\\.");
	//	System.out.println(ip);
		int temp1 = (Integer.parseInt(ipNos[2])) << 8;
		int temp2 = (Integer.parseInt(ipNos[3]));
		int Port = temp1 + temp2;
		return String.valueOf(Port);
	}

	

	public static void main(String[] args) {

		String source = null;
		ArrayList<String> neigborPorts = new ArrayList<String>();
		ArrayList<String> network = new ArrayList<String>();
		
		File file = new File(args[0]);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] text = line.split(":");
				if (text[0].equals("ADDRESS")) {
					source = getPort(text[1]);

				} else if (text[0].equals("NEIGHBOR")) {
					neigborPorts.add(getPort(text[1]));

				} else if (text[0].equals("NETWORK")) {
					network.add(text[1]);

				}

			}

			reader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	

		
		//for (int index = 0; index < 4; index++) {
			new SenderThread(source,neigborPorts,network).start();
			
			// ipaddress[index] is the port
		//}

	}
}
