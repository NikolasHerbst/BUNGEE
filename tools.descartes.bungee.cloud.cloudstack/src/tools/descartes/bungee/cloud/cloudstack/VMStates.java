/*******************************************************************************
Copyright 2016 André Bauer, Nikolas Herbst

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*******************************************************************************/

package tools.descartes.bungee.cloud.cloudstack;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VMStates {

	public static String formatheader = "%-36s   %-25s   %-12s   %-9s   %-7s   %-10s";
	public static String format = "%-36s   %-25s   %-12s   %-9s   %-7.2f   %-10s";

	/**
	 * Returns a ArrayList of VMs
	 * @param doc of the ApiCall
	 * @return ArrayList of VMs
	 * @throws XPathExpressionException
	 */
	public static ArrayList<VirtualMachine> read(Document doc) throws XPathExpressionException {

		ArrayList<VirtualMachine> vms = new ArrayList<>();

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile("//virtualmachine");

		XPathExpression count_xp = xpath.compile("//count/text()");
		String count = (String) count_xp.evaluate(doc, XPathConstants.STRING);
		if (count.length() > 0) {
			//System.out.println("Number of VMs = " + count);
		}

		

		String[] properties = new String[] { "id", "name", "cpunumber", "cpuused", "state", "hostid", "memory" };
		//System.out.println(String.format(formatheader, "id", "name", "ipaddress", "cpunumber", "cpuused", "state"));
		//System.out.println("----------------------------------------------------------------------------------------------------------");

		NodeList item_list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < item_list.getLength(); i++) {
			Node item = item_list.item(i);
			String[] property_values = new String[properties.length+1];
			for (int j = 0; j < properties.length; j++) {
				String property_name = properties[j];
				XPathExpression xp_property = xpath.compile(property_name + "/text()");
				String property_value = (String) xp_property.evaluate(item, XPathConstants.STRING);
				property_values[j] = property_value;
			}
			XPathExpression pathtonic = xpath.compile("//nic");
			NodeList nic = (NodeList) pathtonic.evaluate(doc, XPathConstants.NODESET);
			Node item2 = nic.item(i);
			String ipadd = "ipaddress";
			XPathExpression xp_property2 = xpath.compile(ipadd + "/text()");
			property_values[properties.length] =(String) xp_property2.evaluate(item2, XPathConstants.STRING);
			VirtualMachine vm = new VirtualMachine(property_values);
			//System.out.println(vm);
			vms.add(vm);

		}
		//System.out.println();

		return vms;

	}
}
