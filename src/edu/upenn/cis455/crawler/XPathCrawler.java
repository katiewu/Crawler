package edu.upenn.cis455.crawler;

import java.util.List;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.Webpage;

/**
 * XPathCrawler - crawl the web
 * @author cis455
 *
 */
public class XPathCrawler {
	String seedURL;
	String storepath;
	int maxSize;
	int noFile = Integer.MAX_VALUE;
	URLFrontier urlFrontier;
	DBWrapper db;
	
	public XPathCrawler(){
		
	}
	
	public XPathCrawler(String seedURL, String storepath, int maxSize){
		this.seedURL = seedURL;
		this.storepath = storepath;
		this.maxSize = maxSize;
		urlFrontier = new URLFrontier(maxSize);
		db = new DBWrapper(storepath);
		db.setup();
		WebpageDownloader.setup(db);
	}
	
	public XPathCrawler(String seedURL, String storepath, int maxSize, int noFiles){
		this(seedURL, storepath, maxSize);
		this.noFile = noFile;
	}
	
	/**
	 * crawl the web, starting with a seed URL
	 */
	public void crawl(){
		int size = 0;
		urlFrontier.pushURL(seedURL);
		size++;
		while(!urlFrontier.isEmpty()){
			// get current url
			String url = urlFrontier.popURL();
			// check whether url meets with robot.txt requirement
			while(!RobotManager.checkDelay(url)){
				urlFrontier.pushURL(url);
				url = urlFrontier.popURL();
			}
			RobotManager.addRobot(url);
			RobotManager.setCurrentTime(url);
			// download webpage
			WebpageDownloader.download(url);
			size++;
			if(size>=noFile) break;
			System.out.println("downloading: "+url);
			db.ProcessXpath(url);
			// extract all the links in the webpage
			List<String> linklist = Processor.extractLink(url);
			for(String link:linklist){
				System.out.print("     extract link: "+link);
				// apply filter to each webpage (whether the url has already been crawled,
				// or meets with robot.txt requirements
				if(urlFrontier.filter(link)){
					if(RobotManager.isValid(link)) {
						System.out.println("valid");
						urlFrontier.pushURL(link);
					}
					else{
						System.out.println("robot invalid");
					}
				}
				else{
					System.out.println("filter fail");
				}
			}
		}
		System.out.println(size);
	}
	
	public void close(){
		db.close();
	}
	
	/* specify the requirements
	* seedURL: seed of URL
	* filepath: the directory to store the webpages
	* maxSize: the maximum size of webpage content
	* (optional)fileno: maximum number of files to be crawled
	*/
	public static void main(String[] args){
		if(args.length == 0){
			System.out.println("You need to specify the arguments.");
			System.out.println("name: Wu Jingyuan");
			System.out.println("pennkey: wujingyu");
			return;
		}
		else if(args.length == 3){
			String seedURL = args[0];
			String filepath = args[1];
			int maxSize = Integer.parseInt(args[2]);
			XPathCrawler crawler = new XPathCrawler(seedURL, filepath, maxSize);
			crawler.crawl();
			crawler.close();
		}
		else if(args.length == 4){
			String seedURL = args[0];
			String filepath = args[1];
			int maxSize = Integer.parseInt(args[2]);
			int fileno = Integer.parseInt(args[3]);
			XPathCrawler crawler = new XPathCrawler(seedURL, filepath, maxSize, fileno);
			crawler.crawl();
			crawler.close();
		}
		else{
			System.out.println("The number of arguments is wrong.");
		}
	}
}
