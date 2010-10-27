package org.eclipse.e4.opensocial.container.osapi.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.web.BrowserRPCHandler;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresence.Mode;
import org.eclipse.ecf.presence.roster.IRoster;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.roster.IRosterGroup;
import org.eclipse.ecf.presence.roster.IRosterItem;
import org.eclipse.ecf.presence.service.IPresenceService;
import org.eclipse.swt.browser.Browser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class OsapiPeopleGetHandler implements BrowserRPCHandler {

	@Override
	public Object handle(Browser browser, Object[] arguments) {
		JSONObject queryParams = null;
		try {
			queryParams = new JSONObject(((String) arguments[1]));
			String userId = (String) queryParams.get("userId");
			String groupId = (String) queryParams.get("groupId");

			IPresenceService presenceService = getPresenceService();

			if (presenceService == null) {
				JSONObject obj = new JSONObject();
				obj.put("error", "No contact provider available");
				return obj.toString();
			}

			IRoster roster = presenceService.getRosterManager().getRoster();

			if ("@viewer".equals(userId) && "@self".equals(groupId)) {
				// return createJSONUser(roster.getUser()).toString();
				return "";
			}

			if ("@owner".equals(userId) && "@self".equals(groupId)) {
				// at the moment we assume user==owner...
				// return createJSONUser(roster.getUser()).toString();
				return "";
			}

			if ("@viewer".equals(userId) && "@friends".equals(groupId)) {
				List<IRosterEntry> users = new ArrayList<IRosterEntry>();
				for (Object o : roster.getItems()) {
					IRosterItem item = (IRosterItem) o;
					fillUsersList(users, item);
				}
				return createJSONUsers(users).toString();
			}

			return null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param users
	 * @param item
	 */
	private void fillUsersList(List<IRosterEntry> users, IRosterItem item) {
		if (item instanceof IRosterEntry) {
			IRosterEntry entry = (IRosterEntry) item;
			if (IPresence.Type.UNAVAILABLE != entry.getPresence().getType())
				users.add(entry);
		} else if (item instanceof IRosterGroup) {
			for (Object o : ((IRosterGroup) item).getEntries()) {
				fillUsersList(users, (IRosterItem) o);
			}
		}
	}

	/**
	 * @param rosterEntry
	 * @return JSON representation of the user associated to the given
	 *         IRosterEntry
	 * @throws JSONException
	 */
	private JSONObject createJSONUser(IRosterEntry rosterEntry)
			throws JSONException {
		JSONObject obj = new JSONObject();
		IUser user = rosterEntry.getUser();
		obj.put("displayName", user.getName());
		obj.put("nickName", user.getNickname());

		String networkPresence = "OFFLINE";
		if (rosterEntry.getPresence().getMode() == Mode.AVAILABLE)
			networkPresence = "AVAILABLE";
		if (rosterEntry.getPresence().getMode() == Mode.AWAY)
			networkPresence = "AWAY";
		if (rosterEntry.getPresence().getMode() == Mode.CHAT)
			networkPresence = "CHAT";
		if (rosterEntry.getPresence().getMode() == Mode.DND)
			networkPresence = "DND";
		if (rosterEntry.getPresence().getMode() == Mode.EXTENDED_AWAY)
			networkPresence = "XA";
		if (rosterEntry.getPresence().getMode() == Mode.INVISIBLE)
			networkPresence = "OFFLINE";
		obj.put("networkPresence", networkPresence);

		obj.put("thumbnailUrl", "http://localhost/getContactImg?id="
				+ user.getID().toExternalForm());

		return obj;
	}

	/**
	 * @param users
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createJSONUsers(List<IRosterEntry> users)
			throws JSONException {
		JSONObject obj = new JSONObject();
		JSONArray usersArray = new JSONArray();
		for (IRosterEntry rosterEntry : users) {
			usersArray.put(createJSONUser(rosterEntry));
		}

		obj.put("sorted", false);
		obj.put("filtered", false);
		obj.put("totalResults", usersArray.length());
		obj.put("list", usersArray);

		return obj;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	private IPresenceService getPresenceService() {
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass())
				.getBundleContext();
		ServiceReference sr = bundleContext
				.getServiceReference(IPresenceService.class.getName());
		if (sr != null)
			return (IPresenceService) bundleContext.getService(sr);
		return null;
	}

}
