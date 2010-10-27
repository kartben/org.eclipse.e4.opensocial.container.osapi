/**
 * Copyright (c) 2010 Sierra Wireless Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Contributors:
 *      Benjamin Cabe, Sierra Wireless - initial API and implementation
 */
package org.eclipse.e4.opensocial.container.osapi.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.roster.IRosterGroup;
import org.eclipse.ecf.presence.roster.IRosterItem;
import org.eclipse.ecf.presence.roster.RosterEntry;
import org.eclipse.ecf.presence.service.IPresenceService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

public class EcfPresence {
	public class ImageServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			String userId = req.getParameter("id");
			for (IPresenceService ips : _presenceServices) {
				Collection items = ips.getRosterManager().getRoster()
						.getItems();
				for (Object o : items) {
					IRosterItem rosterItem = (IRosterItem) o;
					if (findContactAndReturnImgData(userId, rosterItem, resp))
						break;
				}
			}
		}

		private boolean findContactAndReturnImgData(String userId,
				IRosterItem rosterItem, HttpServletResponse resp)
				throws IOException {
			if (rosterItem instanceof IRosterEntry) {
				if (userId.equals(((IRosterEntry) rosterItem).getUser().getID()
						.toExternalForm())) {
					resp.getOutputStream().write(
							((RosterEntry) rosterItem).getPresence()
									.getPictureData());
					resp.getOutputStream().flush();
					return true;
				}
			} else if (rosterItem instanceof IRosterGroup) {
				boolean found;
				for (Object o : ((IRosterGroup) rosterItem).getEntries()) {
					found = findContactAndReturnImgData(userId,
							(IRosterItem) o, resp);
					if (found)
						return true;
				}
				return false;

			}
			return false; // should not happen anyways
		}
	}

	private HttpService _httpService;
	private Servlet _servlet = new ImageServlet();
	private List<IPresenceService> _presenceServices = new ArrayList<IPresenceService>();

	protected void activate(ComponentContext cctx) {
		_httpService = (HttpService) cctx.locateService("httpService");

		try {
			_httpService
					.registerServlet("/getContactImg", _servlet, null, null);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (NamespaceException e) {
			e.printStackTrace();
		}
	}

	protected void deactivate(ComponentContext cctx) {
		_httpService = null;
	}

	protected void addPresenceService(IPresenceService ips) {
		_presenceServices.add(ips);
	}

	protected void removePresenceService(IPresenceService ips) {
		_presenceServices.remove(ips);
	}
}
