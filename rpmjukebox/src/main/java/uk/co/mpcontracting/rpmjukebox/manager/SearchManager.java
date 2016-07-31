package uk.co.mpcontracting.rpmjukebox.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.model.Artist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.search.ArtistField;
import uk.co.mpcontracting.rpmjukebox.search.TrackField;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.search.TrackSort;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.DataParser;

@Slf4j
@Component
public class SearchManager implements Constants {

	@Autowired
    private SettingsManager settingsManager;
    
    @Getter private List<String> genreList;
    @Getter private List<String> yearList;
    @Getter private List<TrackSort> trackSortList;
    
    private Analyzer analyzer;
    
    private Directory artistDirectory;
    private IndexWriter artistWriter;
    private SearcherManager artistManager;
    
    private Directory trackDirectory;
    private IndexWriter trackWriter;
    private SearcherManager trackManager;
	
    public void initialise() {
    	log.info("Initialising SearchManager");
    	
    	try {
    		// Initialise the indexes
    		analyzer = new SimpleAnalyzer();
    		BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);

    		artistDirectory = FSDirectory.open(settingsManager.getFileFromConfigDirectory(ARTIST_INDEX_DIRECTORY).toPath());
    		IndexWriterConfig artistWriterConfig = new IndexWriterConfig(analyzer);
    		artistWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
            artistWriter = new IndexWriter(artistDirectory, artistWriterConfig);
    		artistManager = new SearcherManager(artistWriter, null);

    		trackDirectory = FSDirectory.open(settingsManager.getFileFromConfigDirectory(TRACK_INDEX_DIRECTORY).toPath());
            IndexWriterConfig trackWriterConfig = new IndexWriterConfig(analyzer);
    		trackWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
            trackWriter = new IndexWriter(trackDirectory, trackWriterConfig);
            trackManager = new SearcherManager(trackWriter, null);
            
            // Initialise the filters and sorts
            genreList = new ArrayList<String>();
        	yearList = new ArrayList<String>();
        	trackSortList = Arrays.asList(TrackSort.values());
        	
        	// See if we already have valid indexes, if not, build them
        	if (getArtistById(1) == null || getTrackById(1) == null) {
        		DataParser.parse(this, settingsManager.getDataFile(), genreList, yearList);
            	commitIndexes();
        	}

            log.info("SearchManager initialised");
    	} catch (Exception e) {
    		log.error("Error initlising SearchManager", e);
    	}
    }
    
    private void commitIndexes() {
    	log.info("Committing indexes");
    	
    	try {
    		artistWriter.commit();
    		trackWriter.commit();
    		
            artistManager.maybeRefreshBlocking();
            trackManager.maybeRefreshBlocking();
            
            log.info("Indexes committed");
        } catch (Exception e) {
            log.info("Unable to commit indexes", e);
        }
    }
    
    public void addArtist(Artist artist) {
    	Document document = new Document();
    	
    	document.add(new StringField(ArtistField.ARTISTID.name(), Integer.toString(artist.getArtistId()), Field.Store.YES));
        document.add(new StringField(ArtistField.ARTISTNAME.name(), artist.getArtistName(), Field.Store.YES));
        document.add(new StringField(ArtistField.ARTISTIMAGE.name(), nullIsBlank(artist.getArtistImage()), Field.Store.YES));
        document.add(new StringField(ArtistField.BIOGRAPHY.name(), nullIsBlank(artist.getBiography()), Field.Store.YES));
        document.add(new StringField(ArtistField.MEMBERS.name(), nullIsBlank(artist.getMembers()), Field.Store.YES));
        
        try {
            artistWriter.addDocument(document);
        } catch (Exception e) {
            log.error("Unable to index artist - " + artist.getArtistId());
        }
    }
    
    public void addTrack(Track track) {
    	Document document = new Document();
        
        // Keywords
        document.add(new TextField(TrackField.KEYWORDS.name(), 
    		stripNonAlphaNumerics(track.getArtistName()) + " " + stripNonAlphaNumerics(track.getAlbumName()) + " " + 
    		stripNonAlphaNumerics(track.getTrackName()), Field.Store.YES));
        
        // Result data
        document.add(new StringField(TrackField.ARTISTID.name(), Integer.toString(track.getArtistId()), Field.Store.YES));
        document.add(new StringField(TrackField.ARTISTNAME.name(), track.getArtistName(), Field.Store.YES));
        document.add(new StringField(TrackField.ARTISTIMAGE.name(), nullIsBlank(track.getArtistImage()), Field.Store.YES));
        document.add(new StoredField(TrackField.ALBUMID.name(), track.getAlbumId()));
        document.add(new StringField(TrackField.ALBUMNAME.name(), track.getAlbumName(), Field.Store.YES));
        document.add(new StringField(TrackField.ALBUMIMAGE.name(), nullIsBlank(track.getAlbumImage()), Field.Store.YES));
        document.add(new StoredField(TrackField.YEAR.name(), track.getYear()));
        document.add(new StringField(TrackField.TRACKID.name(), Integer.toString(track.getTrackId()), Field.Store.YES));
        document.add(new StringField(TrackField.TRACKNAME.name(), track.getTrackName(), Field.Store.YES));
        document.add(new StoredField(TrackField.NUMBER.name(), track.getNumber()));
        document.add(new StringField(TrackField.LOCATION.name(), track.getLocation(), Field.Store.YES));
        document.add(new StringField(TrackField.ISPREFERRED.name(), Boolean.toString(track.isPreferred()), Field.Store.YES));
        
        for (String genre : track.getGenres()) {
        	document.add(new StringField(TrackField.GENRE.name(), genre, Field.Store.YES));
        }

        // Sorts
        document.add(new StringField(TrackSort.DEFAULTSORT.name(), stripWhitespace(track.getArtistName(), false) + padInteger(track.getAlbumId()) + 
        	padInteger(track.getNumber()) + padInteger(track.getYear()), Field.Store.NO));
        document.add(new StringField(TrackSort.ARTISTSORT.name(), padInteger(track.getYear()) + stripWhitespace(track.getArtistName(), false), Field.Store.NO));
        document.add(new StringField(TrackSort.ALBUMSORT.name(), padInteger(track.getYear()) + stripWhitespace(track.getAlbumName(), false), Field.Store.NO));
        document.add(new StringField(TrackSort.TRACKSORT.name(), padInteger(track.getYear()) + stripWhitespace(track.getTrackName(), false), Field.Store.NO));
        
        try {
            trackWriter.addDocument(document);
        } catch (Exception e) {
            log.error("Unable to index track - " + track.getTrackId());
        }
    }
    
    public List<Track> search(TrackSearch trackSearch) {
    	return null;
    }
    
    public Artist getArtistById(int artistId) {
    	if (artistManager == null) {
            throw new RuntimeException("Cannot search before artist index is initialised");
        }

    	IndexSearcher artistSearcher = null;
    	
    	try {
    		artistSearcher = artistManager.acquire();
    		//TopDocs results = artistSearcher.search(NumericRangeQuery.newIntRange(ArtistField.ARTISTID.name(), 1, artistId, artistId, true, true), 1);
    		TopDocs results = artistSearcher.search(new TermQuery(new Term(ArtistField.ARTISTID.name(), Integer.toString(artistId))), 1);

    		if (results.totalHits < 1) {
    			return null;
    		}
    		
    		return getArtistByDocId(artistSearcher, results.scoreDocs[0].doc);
    	} catch (Exception e) {
            log.error("Unable to run get artist by id", e);
            
            return null;
        } finally {
        	try {
	        	trackManager.release(artistSearcher);
        	} catch (Exception e) {
        		log.warn("Unable to release artist searcher");
        	}
	        
        	artistSearcher = null;
    	}
    }
    
	public Track getTrackById(int trackId) {
		if (trackManager == null) {
            throw new RuntimeException("Cannot search before track index is initialised");
        }
    	
    	IndexSearcher trackSearcher = null;
    	
    	try {
    		trackSearcher = trackManager.acquire();
    		//TopDocs results = trackSearcher.search(NumericRangeQuery.newIntRange(TrackField.TRACKID.name(), 1, trackId, trackId, true, true), 1);
    		TopDocs results = trackSearcher.search(new TermQuery(new Term(TrackField.TRACKID.name(), Integer.toString(trackId))), 1);

    		if (results.totalHits < 1) {
    			return null;
    		}
    		
    		return getTrackByDocId(trackSearcher, results.scoreDocs[0].doc);
    	} catch (Exception e) {
            log.error("Unable to run get track by id", e);
            
            return null;
        } finally {
        	try {
	        	trackManager.release(trackSearcher);
        	} catch (Exception e) {
        		log.warn("Unable to release track searcher");
        	}
	        
        	trackSearcher = null;
    	}
	}

    private Artist getArtistByDocId(IndexSearcher artistSearcher, int docId) throws Exception {
    	Document document = artistSearcher.doc(docId);
    	return new Artist(
			Integer.parseInt(document.get(ArtistField.ARTISTID.name())),
			document.get(ArtistField.ARTISTNAME.name()),
			document.get(ArtistField.ARTISTIMAGE.name()),
			document.get(ArtistField.BIOGRAPHY.name()),
			document.get(ArtistField.MEMBERS.name())
		);
    }
    
    private Track getTrackByDocId(IndexSearcher trackSearcher, int docId) throws Exception {
    	Document document = trackSearcher.doc(docId);
    	return new Track(
			Integer.parseInt(document.get(TrackField.ARTISTID.name())),
			document.get(TrackField.ARTISTNAME.name()),
			document.get(TrackField.ARTISTIMAGE.name()),
			Integer.parseInt(document.get(TrackField.ALBUMID.name())),
			document.get(TrackField.ALBUMNAME.name()),
			document.get(TrackField.ALBUMIMAGE.name()),
			Integer.parseInt(document.get(TrackField.YEAR.name())),
			Integer.parseInt(document.get(TrackField.TRACKID.name())),
			document.get(TrackField.TRACKNAME.name()),
			Integer.parseInt(document.get(TrackField.NUMBER.name())),
			document.get(TrackField.LOCATION.name()),
			Boolean.parseBoolean(document.get(TrackField.ISPREFERRED.name())),
			Arrays.asList(document.getValues(TrackField.GENRE.name()))
    	);
    }
    
    private Query buildKeywordsQuery(String keywords) {
        // Split into whole words with the last word having
        // a wildcard '*' on the end
    	Builder builder = new BooleanQuery.Builder();

        for (StringTokenizer tokens = new StringTokenizer(keywords, " "); tokens.hasMoreTokens();) {
            String token = tokens.nextToken();

            if (tokens.hasMoreElements()) {
                builder.add(new TermQuery(new Term(TrackField.KEYWORDS.name(), token)), BooleanClause.Occur.MUST);
            } else {
                builder.add(new WildcardQuery(new Term(TrackField.KEYWORDS.name(), (token + "*"))), BooleanClause.Occur.MUST);
            }
        }

        return builder.build();
    }
	
	private String nullIsBlank(String string) {
    	if (string == null) {
    		return "";
    	}
    	
    	return string;
    }
    
    private String padInteger(int toPad) {
        String string = "0000000000" + toPad;
        
        return string.substring(string.length() - 10);
    }
    
    private String stripNonAlphaNumerics(String string) {
    	if (string == null) {
            return ("");
        }
    	
    	StringBuilder builder = new StringBuilder();
    	
    	for (int i = 0; i < string.length(); i++) {
			char nextChar = string.charAt(i);
			
			if (nextChar == ' ' && (builder.length() == 0 || builder.charAt(builder.length() - 1) != ' ')) {
				builder.append(nextChar);
			}
			else if (Character.isAlphabetic(nextChar) || Character.isDigit(nextChar)) {
				builder.append(nextChar);
			}
		}
    	
    	return builder.toString().trim();
    }
    
    private String stripWhitespace(String string, boolean keepSpaces) {
        if (string == null) {
            return ("");
        }
        
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < string.length(); i++) {
            char nextChar = string.charAt(i);
            
            if (keepSpaces && (nextChar == ' ')) {
            	builder.append(nextChar);
            }
            else if (!Character.isWhitespace(nextChar)) {
            	builder.append(nextChar);
            }
        }
        
        return builder.toString().trim();
    }
}
