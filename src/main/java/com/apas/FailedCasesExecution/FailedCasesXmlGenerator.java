package com.apas.FailedCasesExecution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.testng.TestNG;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class FailedCasesXmlGenerator {

	private Map<String, List<String>> classesAndMethodsMap = new HashMap<String, List<String>>();
	private List<String> listeners = new ArrayList<String>();
	private String fileName = System.getProperty("user.dir") + "/failed-tests.xml";

	/**
	 * Parameterized class constructor to initialize instance variables
	 * @param: Takes a map with Keys as class names and values as list of failed method from these classes
	 */
	public FailedCasesXmlGenerator(Map<String, List<String>> classesAndMethodsMap) {
		listeners.add("com.apas.Listeners.SuiteListener");
		listeners.add("com.apas.Listeners.TestAnnotationListener");
		this.classesAndMethodsMap = classesAndMethodsMap;
	}

	/**
	 * Generates an XML file of testng.xml format which contains failed methods (and their respective class)
	 * that have failed during the execution
	 */
	public void generateFailedCasesXml() {
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder icBuilder;
		try {
			icBuilder = icFactory.newDocumentBuilder();
			Document doc = icBuilder.newDocument();
			Element mainRootElement = doc.createElement("suite");
			mainRootElement.setAttribute("name", "APAS_Automation_FailedCases_ReExecution");
			doc.appendChild(mainRootElement);
			mainRootElement.appendChild(addListenersTag(doc, listeners));
			mainRootElement.appendChild(addTestTag(doc, "Failed_Re_Execution"));

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			File file = new File(fileName);
			if (file.createNewFile()) {
				System.out.println("Failed test cases file created successfully!");
			}
			StreamResult target = new StreamResult(fileName);
			transformer.transform(source, target);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * It appends the listeners tag in XML file. 
	 * @param doc: Variable of Document type
	 * @param listenerNames: List of listener names to add in XML file
	 * @return: Returns Node type variable
	 */
	private Node addListenersTag(Document doc, List<String> listenerNames) {
		Element listeners = doc.createElement("listeners");
		for (String listenerName : listenerNames) {
			listeners.appendChild(addListenerElements(doc, "listener", listenerName));
		}
		return listeners;
	}

	/**
	 * It appends the listener tag with listener details under listeners tag. 
	 * @param doc: Variable of Document type
	 * @param name: Name of the tag to create
	 * @param listenerName: Name of the listenaer to add in file
	 * @return: Returns Node type variable
	 */
	private Node addListenerElements(Document doc, String name, String listenerName) {
		Element node = doc.createElement(name);
		node.setAttribute("class-name", listenerName);
		return node;
	}

	/**
	 * It appends the test tag in XML file. 
	 * @param doc: Variable of Document type
	 * @param name: Name of the test
	 * @return: Returns Node type variable
	 */
	private Node addTestTag(Document doc, String name) {
		Element test = doc.createElement("test");
		test.setAttribute("name", name);
		test.appendChild(addClassesTag(doc));
		return test;
	}

	/**
	 * It appends the classes tag in XML file. 
	 * @param doc: Variable of Document type
	 * @return: Returns Node type variable
	 */
	private Node addClassesTag(Document doc) {
		Element classes = doc.createElement("classes");
		Set<String> classNames = classesAndMethodsMap.keySet();
		for (String className : classNames) {
			classes.appendChild(addClassTags(doc, "class", className));
		}
		return classes;
	}

	/**
	 * It appends the class tag with class details inside it. 
	 * @param doc: Variable of Document type
	 * @param name: Name of the tag to create
	 * @param className: Name of the class having failed method(s)
	 * @return: Returns Node type variable
	 */
	private Node addClassTags(Document doc, String name, String className) {
		Element givenClass = doc.createElement(name);
		givenClass.setAttribute("name", className);
		givenClass.appendChild(addMethodsTag(doc, className));
		return givenClass;
	}

	/**
	 * It appends the methods tag in XML file. 
	 * @param doc: Variable of Document type
	 * @param className: Name of the class whose failed methods are to be added
	 * @return: Returns Node type variable
	 */
	private Node addMethodsTag(Document doc, String className) {
		Element methods = doc.createElement("methods");
		List<String> methodNames = classesAndMethodsMap.get(className);
		for (String methodName : methodNames) {
			methods.appendChild(includeMethods(doc, "include", methodName));
		}
		return methods;
	}

	/**
	 * It appends the include tag with method details under methods tag. 
	 * @param doc: Variable of Document type
	 * @param name: Name of the node to create
	 * @param methodName: Name of the failed method to include
	 * @return: Returns Node type variable
	 */
	private Node includeMethods(Document doc, String name, String methodName) {
		Element node = doc.createElement(name);
		node.setAttribute("name", methodName);
		return node;
	}

	// Executes the given file as TestNG Suite. 
	public void runFailedTestCasesXml() throws IOException {
		TestNG runner = new TestNG();
		List<String> suitefiles = new ArrayList<String>();
		suitefiles.add(fileName);
		System.out.println("Running Failed Cases XML");
		runner.setTestSuites(suitefiles);
	}

	// Deletes the existing XML file.
	public void deleteExistingXml() throws IOException {
		File file = new File(fileName);
		if (file.delete()) {
			System.out.println("Failed test cases file deleted successfully!");
		}
	}
}