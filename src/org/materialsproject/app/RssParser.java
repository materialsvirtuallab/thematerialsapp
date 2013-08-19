package org.materialsproject.app;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import java.io.IOException;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class RssParser {
	/** Called when the activity is first created. */
	StringBuilder rssResult = new StringBuilder();
	List<String> allTitles = new ArrayList<String>();
	List<String> allContent = new ArrayList<String>();
	String allKeys = "";
	boolean item = false;

	public RssParser(String url) {
		try {
			URL rssUrl = new URL(url);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			RSSHandler rssHandler = new RSSHandler();
			xmlReader.setContentHandler(rssHandler);
			InputSource inputSource = new InputSource(rssUrl.openStream());
			xmlReader.parse(inputSource);

		} catch (IOException e) {
			rssResult.append(e.getMessage());
		} catch (SAXException e) {
			rssResult.append(e.getMessage());
		} catch (ParserConfigurationException e) {
			rssResult.append(e.getMessage());
		}
	}

	private class RSSHandler extends DefaultHandler {

		public void startElement(String uri, String localName, String qName,
				Attributes attrs) throws SAXException {
			if (localName.equals("item")) {
				item = true;
			}
			if ((localName.equals("title") || localName.startsWith("encoded")) && item) {
				rssResult = new StringBuilder();
			}
			allKeys += localName + ":";

		}

		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {
			if (localName.equals("item")) {
				item = false;
			} else if (item && localName.equals("title")) {
				allTitles.add(rssResult.toString());
			} else if (item && localName.startsWith("encoded")) {
				allContent.add(rssResult.toString());
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String cdata = new String(ch, start, length);
			if (item) {
				rssResult.append((cdata.trim()).replaceAll("\\s+", " ") + "\t");
			}

		}

	}
}