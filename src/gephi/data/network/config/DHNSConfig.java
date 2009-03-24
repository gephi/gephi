package gephi.data.network.config;

public class DHNSConfig
{
	public enum ViewType {SINGLE, HIERARCHY};
	private ViewType viewType;

	public DHNSConfig()
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
