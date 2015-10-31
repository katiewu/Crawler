package edu.upenn.cis455.crawler;

/**
 * XPathCrawlerFactory - return singleton XPathCrawler
 * @author Jingyuan
 *
 */
public class XPathCrawlerFactory {
	public XPathCrawler getCrawler() {
		return new XPathCrawler();
	}
}
