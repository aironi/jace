package org.silverduck.jace.common.xml;

/**
 * An utility class for XML related functions
 */
public class XmlUtils {

    /**
     * Removes the namespace information from a given XML String.
     * This method should be used only when the namespace information is not known beforehand. Copied from
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
