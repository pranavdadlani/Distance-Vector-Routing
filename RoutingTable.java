import java.io.Serializable;

public class RoutingTable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String to;
	public String via;
	public int cost;
	public RoutingTable (String to,String via,int cost) {

		this.to=to;
		this.via=via;
		this.cost=cost;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getVia() {
		return via;
	}
	public void setVia(String via) {
		this.via = via;
	}
	public int getCost() {
		return cost;
	}
	public void setCost(int cost) {
		this.cost = cost;
	}
	
	
	
	
}
