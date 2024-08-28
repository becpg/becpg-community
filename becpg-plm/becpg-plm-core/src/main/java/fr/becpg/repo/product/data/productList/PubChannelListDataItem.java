/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.data.productList;

import java.util.Date;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bp:pubChannelList")
public class PubChannelListDataItem extends BeCPGDataObject {

	private static final long serialVersionUID = 1L;
	
	private String batchId;
	
	private Date publishedDate;
	
	private String status;
	
	private String error;
	
	private String action;
	
	private Date modifiedDate;
	
	private NodeRef channel;

	@AlfProp
	@AlfQname(qname = "bp:pubChannelListBatchId")
	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	@AlfProp
	@AlfQname(qname = "bp:pubChannelListPublishedDate")
	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}

	@AlfProp
	@AlfQname(qname = "bp:pubChannelListStatus")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@AlfProp
	@AlfQname(qname = "bp:pubChannelListError")
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@AlfProp
	@AlfQname(qname = "bp:pubChannelListAction")
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@AlfProp
	@AlfQname(qname = "bp:pubChannelListModifiedDate")
	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "bp:pubChannelListChannel")
	@DataListIdentifierAttr
	public NodeRef getChannel() {
		return channel;
	}

	public void setChannel(NodeRef channel) {
		this.channel = channel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(action, batchId, channel, error, modifiedDate, publishedDate, status);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PubChannelListDataItem other = (PubChannelListDataItem) obj;
		return Objects.equals(action, other.action) && Objects.equals(batchId, other.batchId) && Objects.equals(channel, other.channel)
				&& Objects.equals(error, other.error) && Objects.equals(modifiedDate, other.modifiedDate)
				&& Objects.equals(publishedDate, other.publishedDate) && Objects.equals(status, other.status);
	}

	@Override
	public String toString() {
		return "PubChannelListDataItem [batchId=" + batchId + ", publishedDate=" + publishedDate + ", status=" + status + ", error=" + error
				+ ", action=" + action + ", modifiedDate=" + modifiedDate + ", channel=" + channel + "]";
	}

	
}
