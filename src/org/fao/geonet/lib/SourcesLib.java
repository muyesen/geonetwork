//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.XmlRequest;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

//=============================================================================

public class SourcesLib
{
	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void update(Dbms dbms, String uuid, String name, boolean isLocal) throws SQLException
	{
		String query = "SELECT isLocal FROM Sources WHERE uuid=?";

		List list = dbms.select(query, uuid).getChildren();

		if (list.size() == 0)
		{
			query = "INSERT INTO Sources(uuid, name, isLocal) VALUES(?,?,?)";
			dbms.execute(query, uuid, name, isLocal ? "y" : "n");
		}
		else
		{
			Element rec = (Element) list.get(0);

			if (isLocal || "n".equals(rec.getChildText("islocal")))
			{
				query = "UPDATE Sources SET name=? WHERE uuid=?";
				dbms.execute(query, name, uuid);
			}
		}
	}

	//---------------------------------------------------------------------------

	public void delete(Dbms dbms, String uuid) throws SQLException
	{
		dbms.execute("DELETE FROM Sources WHERE uuid=?", uuid);
	}

	//---------------------------------------------------------------------------

	public void copyLogo(ServiceContext context, String icon, String uuid)
	{
		File src = new File(context.getAppPath() + icon);
		File des = new File(context.getAppPath() +"images/logos", uuid +".gif");

		try
		{
			FileInputStream  is = new FileInputStream (src);
			FileOutputStream os = new FileOutputStream(des);

			BinaryFile.copy(is, os, true, true);
		}
		catch (IOException e)
		{
			//--- we ignore exceptions here, just log them

			context.warning("Cannot copy CSW icon -> "+e.getMessage());
			context.warning(" (C) Source : "+ src);
			context.warning(" (C) Destin : "+ des);
		}
	}

	//---------------------------------------------------------------------------

	public void copyUnknownLogo(ServiceContext context, String uuid)
	{
		copyLogo(context, "/images/unknown-logo.gif", uuid);
	}

	//---------------------------------------------------------------------------

	public void retrieveLogo(ServiceContext context, String host, int port, String servlet, String uuid)
	{
		String logo = uuid +".gif";

		XmlRequest req = new XmlRequest(host, port);
		Lib.net.setupProxy(context, req);
		req.setAddress("/"+ servlet + Geonet.Path.LOGOS + logo);

		File logoFile = new File(context.getAppPath() + Geonet.Path.LOGOS + logo);

		try
		{
			req.executeLarge(logoFile);
		}
		catch (IOException e)
		{
			context.warning("Cannot retrieve logo file from : "+ host+":"+port);
			context.warning("  (C) Logo  : "+ logo);
			context.warning("  (C) Excep : "+ e.getMessage());

			logoFile.delete();

			copyUnknownLogo(context, uuid);
		}
	}
}

//=============================================================================

