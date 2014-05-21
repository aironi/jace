package org.silverduck.jace.common.xml;

public class XmlUtils {

    /**
     * Removes namespaces from xml. Only to be used when namespaces ARE NOT KNOWN beforehand. Copied from
     * http://stackoverflow.com/questions/4661154/how-do-i-remove-namespaces-from-xml-using-java-dom in dirty fashion.
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
