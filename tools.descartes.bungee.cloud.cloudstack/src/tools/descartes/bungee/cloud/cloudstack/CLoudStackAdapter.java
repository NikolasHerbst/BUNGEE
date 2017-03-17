package tools.descartes.bungee.cloud.cloudstack;

import java.io.File;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.w3c.dom.Document;

import net.datapipe.CloudStack.CloudStackAPI;
import tools.descartes.bungee.utils.FileUtility;

public class CLoudStackAdapter {

	private static final String SERVER4 = "7cf7f418-a77e-48b8-a6c7-02a411a3520e";
	private static final String SERVER3 = "4a117928-baf8-43c2-9132-31a0d121bc7d";
	private static final String RUNNING = "Running";
	private static final String STOPPED = "Stopped";
	private static final String DATA = "coco-data";
	private static final String PRESENTATION = "coco-presentation";
	private static final String BUSINESS = "coco-business";
	private CloudStackAPI client;

	public CLoudStackAdapter(File file) {

		Properties cloudProperties = FileUtility.loadProperties(file);

		String secret = cloudProperties.getProperty("secret");
		String apikey = cloudProperties.getProperty("apiKey");
		String apiURL = cloudProperties.getProperty("apiURL");

		this.client = new CloudStackAPI(apiURL, secret, apikey);

	}

	/**
	 * https://cloudstack.apache.org/api/apidocs-4.8/root_admin/
	 * listVirtualMachines.html
	 * 
	 * @param tag
	 *            of the VMs
	 * @return List of VMs according the group
	 * @throws Exception
	 */
	public ArrayList<VirtualMachine> getAllVms(String tag) throws Exception {
		HashMap<String, String> options = new HashMap<>();
		if (!tag.equals("")) {
			options.put("name", tag);
		}
		Document doc = client.listVirtualMachines(options);

		ArrayList<VirtualMachine> vms = VMStates.read(doc);

		return vms;
	}

	public boolean startVM(int amount, String tag) {

		try {
			ArrayList<VirtualMachine> vms = getAllVms(tag);
			ArrayList<VirtualMachine> vms_stopped = new ArrayList<VirtualMachine>();

			for (VirtualMachine vm : vms) {
				if (vm.getState().equals(STOPPED)) {
					vms_stopped.add(vm);
				}
			}
			if (vms_stopped.size() < amount) {
				return false;
			}
			for (int i = 0; i < amount; i++) {
				switch (tag) {
				case DATA:
					client.startVirtualMachine(vms_stopped.get(i).getId(), SERVER3);
					break;
				case PRESENTATION:
					client.startVirtualMachine(vms_stopped.get(i).getId(), SERVER3);
					break;

				case BUSINESS:
					client.startVirtualMachine(vms_stopped.get(i).getId(), SERVER4);
					break;
				default:
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}

		return true;
	}

	public boolean stopVM(int amount, String tag) {

		try {
			ArrayList<VirtualMachine> vms = getAllVms(tag);
			ArrayList<VirtualMachine> vms_running = new ArrayList<VirtualMachine>();

			for (VirtualMachine vm : vms) {
				if (vm.getState().equals(RUNNING)) {
					vms_running.add(vm);
				}
			}
			if (vms_running.size() < amount) {
				return false;
			}
			for (int i = 0; i < amount; i++) {
				client.stopVirtualMachine(vms_running.get(i).getId(), new HashMap<String, String>());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}

		return true;
	}

	public static void main(String[] args) {

		CLoudStackAdapter csa = new CLoudStackAdapter(new File(FileUtility.FILE_LOCATION, "propertyFiles/cloudstack.prop"));
		
		csa.startVM(4, csa.PRESENTATION);
		csa.startVM(3, csa.DATA);
		csa.startVM(2, csa.BUSINESS);
		
	}

}
