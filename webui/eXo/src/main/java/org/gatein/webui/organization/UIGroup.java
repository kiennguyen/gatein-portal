package org.gatein.webui.organization;

import org.exoplatform.services.organization.Group;
import org.gatein.commons.utils.HTMLEntityEncoder;

import java.io.Serializable;

public class UIGroup implements Serializable {

	private Group group;
	
	public UIGroup(Group group)
	{
		this.group = group;
	}
	
	public String getEncodedLabel()
	{
		return HTMLEntityEncoder.getInstance().encode(getLabel());
	}
	
	public String getLabel()
	{
		return group.getLabel();
	}

	public String getId()
	{
		return group.getId();
	}
}
