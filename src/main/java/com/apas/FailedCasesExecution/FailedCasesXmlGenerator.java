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
	private String fileName = System.getProperty("user.dir") + "/testng.xml";

	public FailedCasesXmlGenerator(Map<String, List<String>> classesAndMethodsMap) {
		listeners.add("com.apas.Listeners.SuiteListener");
		listeners.add("com.apas.Listeners.TestAnnotationListener");

		this.classesAndMethodsMap = classesAndMethodsMap;
	}

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

			// output DOM XML to console
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

	private Node addListenersTag(Document doc, List<String> listenerNames) {
		Element listeners = doc.createElement("listeners");
		for (String listenerName : listenerNames) {
			listeners.appendChild(addListenerElements(doc, listeners, "listener", listenerName));
		}
		return listeners;
	}

	private Node addListenerElements(Document doc, Element element, String name, String value) {
		Element node = doc.createElement(name);
		node.setAttribute("class-name", value);
		return node;
	}

	private Node addTestTag(Document doc, String name) {
		Element test = doc.createElement("test");
		test.setAttribute("name", name);
		test.appendChild(addClassesTag(doc));
		return test;
	}

	private Node addClassesTag(Document doc) {
		Element classes = doc.createElement("classes");
		Set<String> classNames = classesAndMethodsMap.keySet();
		for (String className : classNames) {
			classes.appendChild(addClassTags(doc, classes, "class", className));
		}
		return classes;
	}

	private Node addClassTags(Document doc, Element element, String name, String className) {
		Element givenClass = doc.createElement(name);
		givenClass.setAttribute("name", className);
		givenClass.appendChild(addMethodsTag(doc, className));
		return givenClass;
	}

	private Node addMethodsTag(Document doc, String className) {
		Element methods = doc.createElement("methods");
		List<String> methodNames = classesAndMethodsMap.get(className);
		for (String methodName : methodNames) {
			methods.appendChild(includeMethods(doc, methods, "include", methodName));
		}
		return methods;
	}

	private Node includeMethods(Document doc, Element element, String name, String methodName) {
		Element node = doc.createElement(name);
		node.setAttribute("name", methodName);
		return node;
	}

	public void runFailedTestCasesXml() throws IOException {
		TestNG runner = new TestNG();
		List<String> suitefiles = new ArrayList<String>();
		suitefiles.add(fileName);
		System.out.println("Running Failed Cases XML");
		runner.setTestSuites(suitefiles);
	}

	public void deleteExistingXml() throws IOException {
		File file = new File(fileName);
		if (file.delete()) {
			System.out.println("Failed test cases file deleted successfully!");
		}
	}
}