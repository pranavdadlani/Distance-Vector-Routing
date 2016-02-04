import java.awt.DisplayMode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class SenderThread extends Thread {
	public DatagramSocket socket;

	public final ArrayList<RoutingTable> table;
	public String source;
	public HashMap<String, Integer> neigborCostMap;
	public ArrayList<String> neigbors;
	public ArrayList<String> network;
	Scanner sc;
	public static int count = 0;
	public boolean globalFlag = false;
	public static Object lock = new Object();

	public static String portToip(String ip) {
		int i = Integer.parseInt(ip);
		return (((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + (i & 0xFF));
	}

	SenderThread(String port, ArrayList<String> neigborPorts, ArrayList<String> network) {
		sc = new Scanner(System.in);
		try {
			socket = new DatagramSocket(Integer.parseInt(port));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.network = network;
		source = port;
		this.neigbors = neigborPorts;
		neigborCostMap = new HashMap<String, Integer>();
		table = new ArrayList<RoutingTable>();
		for (int index = 0; index < neigborPorts.size(); index++) {
			RoutingTable initialTable = new RoutingTable(neigbors.get(index), neigbors.get(index), 1);
			table.add(initialTable);
			neigborCostMap.put(neigbors.get(index), 1);
			// neigborTimeout.put(neigbors.get(index), 0);
		}

		System.out.println("Initital Routing Table for port " + source);

		for (int index = 0; index < table.size(); index++) {
			for (int j = 0; j < network.size(); j++) {
				System.out.println("FROM->" + network.get(j));
				System.out.println("TO-> " + portToip(table.get(index).getTo()));
				System.out.println("VIA-> " + portToip(table.get(index).getVia()));
				System.out.println("COST-> " + table.get(index).getCost());
				System.out.println();
			}

		}
		System.out.println("-------------------");
		new Thread(new Runnable() {
			public void run() {

				try {

					byte[] incomingData = new byte[1024];
					DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);

					while (true) {

						try {

							count++;

							socket.receive(incomingPacket);

							byte[] data = incomingPacket.getData();
							ByteArrayInputStream in = new ByteArrayInputStream(data);
							ObjectInputStream is = new ObjectInputStream(in);

							ArrayList<RoutingTable> receivedTable = (ArrayList<RoutingTable>) is.readObject();

							int portOfSenderTable = incomingPacket.getPort();
							/*
							 * for (String c : neigborTimeout.keySet()) { if
							 * (c.equals(portOfSenderTable))
							 * neigborTimeout.put(c, 0); else if
							 * (neigborTimeout.get(c) == 15) {
							 * System.out.println("Router with port number " + c
							 * + " is dead"); } else neigborTimeout.put(c,
							 * neigborTimeout.get(c) + 1);
							 * 
							 * }
							 */
							int tempCount = 0;
							for (int q = 0; q < neigbors.size(); q++) {
								if (!neigbors.get(q).equals(String.valueOf(portOfSenderTable)))
									tempCount++;

							}
							if (tempCount == neigbors.size())
								continue;

							for (int index = 0; index < receivedTable.size(); index++) {
								if (receivedTable.get(index).getTo().equals(port)) {
									continue;

								} else {
									boolean flag = false;

									int dist1 = neigborCostMap.get(String.valueOf(portOfSenderTable));

									int dist2 = receivedTable.get(index).getCost();

									for (int jindex = 0; jindex < table.size(); jindex++) {
										if (receivedTable.get(index).getTo().equals(table.get(jindex).getTo())) {
											flag = true;
											int tempCost1 = table.get(jindex).getCost();
											if (dist1 + dist2 < tempCost1) {

												table.get(jindex).setVia(String.valueOf(portOfSenderTable));
												table.get(jindex).setCost(dist1 + dist2);

												displayTable();

											}

										}

									}
									if (flag == false) {
										RoutingTable tempTable = new RoutingTable(receivedTable.get(index).getTo(),
												String.valueOf(portOfSenderTable), dist1 + dist2);
										table.add(tempTable);

										displayTable();

									}

								}
							}
							synchronized (lock) {

								if (globalFlag == false && count == 10) {
									globalFlag = true;
									count = 0;

									lock.notify();
									try {
										lock.wait();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}

							}

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			private void displayTable() {
				for (int kindex = 0; kindex < table.size(); kindex++) { //
					for (int j = 0; j < network.size(); j++) {
						System.out.println("FROM->" + network.get(j));
						System.out.println("TO->" + portToip(table.get(kindex).getTo()));
						System.out.println("VIA->" + portToip(table.get(kindex).getVia()));
						System.out.println("COST->" + table.get(kindex).getCost());
						System.out.println();
					}
				}
				System.out.println("--------------------------");

			}
		}).start();

		new Thread(new Runnable() {

			public void run() {

				synchronized (lock) {

					try {
						lock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					System.out.println("Add or remove neigbor(Enter add or remove)..No to do neither");
					String choice = sc.next();

					System.out.println("enter neigbor port to add/remove");
					String po = sc.next();
					String value = RipMain.getPort(po);
					System.out.println("Given value" + value);

					if (choice.equals("add")) {

						for (int index = 0; index < table.size(); index++) {
							// already present
							if (table.get(index).getTo().equals(value)) {
								neigborCostMap.put(value, 1);
								neigbors.add(value);

								table.get(index).setCost(1);
								table.get(index).setVia(value);
								System.out.println("1add");
								for (int kindex = 0; kindex < table.size(); kindex++) { //
									for (int j = 0; j < network.size(); j++) {
										System.out.println("FROM->" + network.get(j));
										System.out.println("TO->" + portToip(table.get(kindex).getTo()));
										System.out.println("VIA->" + portToip(table.get(kindex).getVia()));
										System.out.println("COST->" + table.get(kindex).getCost());
										System.out.println();
									}
								}
								System.out.println("--------------------------");
							}

						}

					} else if (choice.equals("remove")) {
						for (int index = 0; index < table.size(); index++) {
							// already present

							if (table.get(index).getTo().equals(value)) {

								neigbors.remove(value);
								neigborCostMap.remove(value);
								table.get(index).setCost(16);
								table.get(index).setVia(value);
								System.out.println("Remove");
								for (int kindex = 0; kindex < table.size(); kindex++) { //
									for (int j = 0; j < network.size(); j++) {
										System.out.println("FROM->" + network.get(j));
										System.out.println("TO->" + portToip(table.get(kindex).getTo()));
										System.out.println("VIA->" + portToip(table.get(kindex).getVia()));
										System.out.println("COST->" + table.get(kindex).getCost());
										System.out.println();
									}
								}
								System.out.println("--------------------------");
								// lock.notify();

							}

						}
					}
					lock.notifyAll();

				}
			}

		}).start();

	}

	public void run() {

		while (true) {
			// System.out.println("receiver thread");
			for (int index = 0; index < neigbors.size(); index++) {
				// System.out.println("sending...");
				try {

					InetAddress ipaddress = InetAddress.getByName("localhost");
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(table);
					os.close();
					byte[] data = outputStream.toByteArray();
					DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipaddress,
							Integer.valueOf(neigbors.get(index)));
					socket.send(sendPacket);

				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				this.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
