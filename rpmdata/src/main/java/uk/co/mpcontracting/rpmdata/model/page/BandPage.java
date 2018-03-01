package uk.co.mpcontracting.rpmdata.model.page;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.mpcontracting.rpmdata.model.DataProcessor;

public class BandPage extends AbstractPage {
	private static Logger log = LoggerFactory.getLogger(BandPage.class);

	@Override
	public void parse(String url, DataProcessor dataProcessor) throws Exception {
		log.info("Parsing BandPage - " + url);
		
		try {
			Document document = Jsoup.parse(new URL(url), TIMEOUT_MILLIS);
			Element mainContent = document.select("div#mainContent").first();
			Element bandElement = mainContent.select("div#biobox").first();
			
			if (bandElement != null) {
				// Create band
				String bandName = bandElement.select("h1").first().ownText();
				String bandImage = "http://jukebox.rpmchallenge.com" + bandElement.select("img").first().attr("src");
				Iterator<Element> elements = bandElement.select("p").iterator();
				
				dataProcessor.processBandInfo(bandName, (!bandImage.contains("banddef.png") ? bandImage : ""), 
					format(elements.next()).html(), format(elements.next()).html(), elements.next().ownText(), elements.next().ownText());
				
				// Create albums
				String albumName = null;
				String albumImage = null;
				int albumYear = -1;
				
				Element albumBoxParent = findParentOfAlbumBox(mainContent);

				for (Element nextChild : albumBoxParent.children()) {
					if ("h2".equals(nextChild.tagName())) {
						String ownText = nextChild.ownText();

						int startYear = ownText.lastIndexOf('(');
						int endYear = ownText.lastIndexOf(')');
						
						if (startYear > -1 && endYear > -1) {
							albumName = ownText.substring(0, startYear);
							albumYear = Integer.parseInt(ownText.substring(startYear + 1, endYear));
						} else {
							albumName = ownText.trim();
							albumYear = -1;
						}
					}
					
					if ("p".equals(nextChild.tagName())) {
						Element albumImageElement = nextChild.select("img").first();
						
						if (albumImageElement != null) {
							albumImage = "http://jukebox.rpmchallenge.com" + albumImageElement.attr("src");
						}
					}

					if ("tracksbox".equals(nextChild.className())) {
						String preferredTrackName = null;
						
						for (Element trackElement : nextChild.select("div.track")) {
							if (!trackElement.select("strong").isEmpty()) {
								preferredTrackName = trackElement.ownText();
							}
						}

						String albumId = null;
						
						if (albumImage != null && !albumImage.contains("albumdef.png")) {
							int lastSlashIndex = albumImage.lastIndexOf('/');
							int lastDotIndex = albumImage.lastIndexOf('.');
							
							albumId = albumImage.substring(lastSlashIndex + 1, lastDotIndex);
						}
						
						dataProcessor.processAlbumInfo(albumId, albumName, (albumId != null ? albumImage : ""), 
							albumYear, preferredTrackName);
					}
				}
				
				// Create tracks
				for (Element element : document.select("param[name=movie]")) {
					String playlistUrl = parseQueryString(element.attr("value"), "&").get("playlist_url");

					if (playlistUrl != null) {
						Document playlist = null;
						
						try (InputStream inputStream = new URL(url + playlistUrl).openStream()) {
							playlist = Jsoup.parse(inputStream, "UTF-8", "", Parser.xmlParser());
						} catch (FileNotFoundException e) {
							log.warn("Unable to find url - " + url + playlistUrl);
						}
						
						if (playlist != null) {
							for (Element track : playlist.select("track")) {
								String location = track.select("location").text();

								dataProcessor.processTrackInfo(track.select("album").text(), track.select("title").text(), location);
							}
						}
					}
				}
				
				// Write the band
				dataProcessor.writeBand();
			}
		} catch (Exception e) {
			if (e instanceof HttpStatusException && ((HttpStatusException)e).getStatusCode() >= 500) {
				log.warn("Unable to fetch url - " + url + " - " + ((HttpStatusException)e).getStatusCode());
				
				throw e;
			} else {
				log.warn("Unable to fetch url - " + url + " - " + e.getMessage());
			}
		}
	}
	
	private Element findParentOfAlbumBox(Node node) {
	    for (int i = 0; i < node.childNodeSize(); i++) {
	        Node nextChild = node.childNode(i);

            if ("#comment".equals(nextChild.nodeName())) {
                if ("<!-- div albumbox -->".equals(nextChild.toString().trim()) && nextChild.parent() instanceof Element) {
                    return (Element)nextChild.parent();
                }
            }
            
            Element parent = findParentOfAlbumBox(nextChild);
            
            if (parent != null) {
                return parent;
            }
	    }

	    return null;
	}

	private Element format(Element element) {
		for (Element child : element.children()) {
			if (child.tagName().equals("strong") || child.tagName().equals("img")) {
				child.remove();
			}
		}

		// Remove leading BR
		for (int i = 0; i < element.childNodeSize(); i++) {
			Node node = element.childNode(i);
			String nodeName = node.nodeName();

			if ("br".equalsIgnoreCase(nodeName)) {
				node.remove();
				i--;
			} else if ("#text".equalsIgnoreCase(nodeName)) {
				break;
			}
		}
		
		// Remove trailing BR
		for (int i = element.childNodeSize() - 1; i >= 0; i--) {
			Node node = element.childNode(i);
			String nodeName = node.nodeName();
			
			if ("br".equalsIgnoreCase(nodeName)) {
				node.remove();
				i++;
			} else if ("#text".equalsIgnoreCase(nodeName)) {
				break;
			}
		}

		return element;
	}
}
