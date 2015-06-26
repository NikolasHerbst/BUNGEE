package net.datapipe.CloudStack;

import net.datapipe.CloudStack.CloudStack;

import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class CLI {
  public static CloudStack factory() {
    String secret = System.getenv("ESTRAT_SECRET");
    String apikey = System.getenv("ESTRAT_APIKEY");
    if(secret == null || apikey == null) {
      System.out.println("I need the environment variables ESTRAT_SECRET and ESTRAT_APIKEY");
      System.exit(1);
    }

    CloudStack client = new CloudStack(secret,apikey);
    return client;
  }

  public static HashMap<String,String> args_to_options(String args[]) {
    return args_to_options(args,0);
  }

  public static HashMap<String,String> args_to_options(String args[], int offset) {
    HashMap<String,String> options = new HashMap<String,String>();
    for(int i = offset; i < args.length; i++) {
      String arg_options[] = args[i].split("=", 2);
      if(arg_options.length == 2) {
        options.put(arg_options[0], arg_options[1]);
      } else {
        System.out.println("Malformed argument = "+args[i]);
        System.exit(1);
      }
    }
    return options;
  }

  public static void printDocument(Document reply, String items, String properties[]) throws XPathExpressionException {
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    XPathExpression expr = xpath.compile(items);

    XPathExpression count_xp = xpath.compile("//count/text()");
    String count = (String)count_xp.evaluate(reply, XPathConstants.STRING);
    if(count.length() > 0) {
      System.out.println("Count = "+count);
    }

    for(int i = 0; i < properties.length; i++) {
      if(i > 0) {
        System.out.print("\t");
      }
      System.out.print(properties[i]);
    }
    System.out.println("");
    System.out.println("-------------------------------------------------------------------------------");

    NodeList item_list = (NodeList)expr.evaluate(reply, XPathConstants.NODESET);
    for (int i = 0; i < item_list.getLength(); i++) {
      Node item = item_list.item(i);

      for(int j = 0; j < properties.length; j++) {
        String property_name = properties[j];
        XPathExpression xp_property = xpath.compile(property_name+"/text()");
        String property_value = (String)xp_property.evaluate(item, XPathConstants.STRING);
        if(j > 0) {
          System.out.print("\t");
        }
	System.out.print(property_value);
      }
      System.out.println("");
    }
  }

  public static void printSomething(Document reply) throws XPathExpressionException {
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();

    System.out.println("root element = "+reply.getDocumentElement().getNodeName());
    XPathExpression xp_property = xpath.compile("/*/*");
    NodeList root_items = (NodeList)xp_property.evaluate(reply, XPathConstants.NODESET);
    for (int i = 0; i < root_items.getLength(); i++) {
      Node item = root_items.item(i);
      System.out.println("sub element = "+item.getNodeName());
    }
  }
}
