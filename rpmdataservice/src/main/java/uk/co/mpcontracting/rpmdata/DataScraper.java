package uk.co.mpcontracting.rpmdata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmdata.model.DataProcessor;
import uk.co.mpcontracting.rpmdata.model.page.IndexPage;

@Slf4j
@Component
public class DataScraper {
	
	@Value("${scraper.root.page}")
	private String rootPage;
	
	@Value("${scraper.output.file}")
	private String outputFilename;
	
	@Value("${ftp.passive}")
	private boolean ftpPassive;
	
	@Value("${ftp.server}")
	private String ftpServer;
	
	@Value("${ftp.directory}")
	private String ftpDirectory;
	
	@Value("${ftp.file}")
	private String ftpFile;
	
	@Value("${ftp.username}")
	private String ftpUsername;
	
	@Value("${ftp.password}")
	private String ftpPassword;

	@Scheduled(cron="${scraper.cron}")
	public void runScraper() {
		log.info("Running scraper - " + new Date());
		
		File outputFile = new File(outputFilename);
		scrapeRpmSite(outputFile);
		byte[] data = readDataFile(outputFile);
		ftpFile(data);
	}
	
	private void scrapeRpmSite(File outputFile) {
		log.info("Scraping data from - " + rootPage);
		log.info("Output file - " + outputFile.getAbsolutePath());
		
		DataProcessor dataProcessor = new DataProcessor(outputFile, true);
		
		try {
			new IndexPage().parse(rootPage, dataProcessor);
		} catch (Exception e) {
			log.error("Exception scraping data from root page - " + rootPage, e);
		} finally {
			dataProcessor.close();
		}
		
		log.info("Scraping finished - " + new Date());
	}
	
	private byte[] readDataFile(File outputFile) {
		log.info("Reading data from file - " + outputFile);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		try (FileInputStream inputStream = new FileInputStream(outputFile)) {
			int nextByte;
			
			while ((nextByte = inputStream.read()) != -1) {
				outputStream.write(nextByte);
			}
		} catch (Exception e) {
			log.error("Error reading data file - " + outputFile);
			
			throw new RuntimeException(e);
		}
		
		return outputStream.toByteArray();
	}
	
	private void ftpFile(byte[] data) {
		log.info("FTPing data file to - " + ftpServer + "/" + ftpDirectory + "/" + ftpFile);
		
		FTPClient ftpClient = new FTPClient();
		
		try {
			ftpClient.connect(ftpServer);

			if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				ftpClient.login(ftpUsername, ftpPassword);

				if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
					if (ftpPassive) {
						log.info("Setting FTP passive mode");
						ftpClient.enterLocalPassiveMode();
					}
					
					ftpClient.changeWorkingDirectory(ftpDirectory);

					if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
						ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
						
						try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
							if (!ftpClient.storeFile(ftpFile, inputStream)) {
								throw new IOException("Unable to write file - " + ftpFile + " - false returned from store file");
							}
							
							if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
								throw new IOException("Unable to write file - " + ftpFile + " - " + ftpClient.getReplyString());
							}
						}
					} else {
						throw new IOException("Unable to change to working directory - " + ftpDirectory + " - " + ftpClient.getReplyString());
					}
				} else {
					throw new IOException("Unable to login to server - " + ftpServer + " - " + ftpClient.getReplyString());
				}
			} else {
				throw new IOException("Unable to connect to FTP server - " + ftpServer + " - " + ftpClient.getReplyString());
			}
		} catch (Exception e) {
			log.error("Unable to FTP file to - " + ftpServer + "/" + ftpDirectory + "/" + ftpFile, e);
		} finally {
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (Exception e) {
					log.error("Unable to disconnect from FTP server - " + ftpServer, e);
				}
			}
		}
		
		log.info("Data file successfully FTPd to - " + ftpServer + "/" + ftpDirectory + "/" + ftpFile);
	}
}
