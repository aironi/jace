package org.silverduck.jace.common.xml;

public class XmlUtils {

    /**
     * A dirty method for removing namespaces from a given Xml String. Only to be used when namespaces ARE NOT KNOWN beforehand. Copied from
     * http://stackoverflow.com/questions/4661154/how-do-i-remove-namespaces-from-xml-using-java-dom
     * 
     * @param xmlString
     *            XML without name space definitions
     * @return
     */
    public static String removeNameSpaces(String xmlString) {
        return xmlString.replaceAll("xmlns.*?(\"|\').*?(\"|\')", "").replaceAll("xsi.*?(\"|').*?(\"|')", "")
            .replaceAll("(<)(\\w+:)(.*?>)", "$1$3").replaceAll("(</)(\\w+:)(.*?>)", "$1$3");
    }
}
