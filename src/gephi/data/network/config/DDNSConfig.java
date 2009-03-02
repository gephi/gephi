package gephi.data.network.config;

public class DDNSConfig 
{
	public enum ViewType {SINGLE, HIERARCHY};
	private ViewType viewType;

	public DDNSConfig()
	{
		viewType = ViewType.SINGLE;
	}

	public ViewType getViewType() {
		return viewType;
	}

	public void setViewType(ViewType viewType) {
		this.viewType = viewType;
	}
}
