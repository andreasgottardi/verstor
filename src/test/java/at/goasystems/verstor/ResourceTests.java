package at.goasystems.verstor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class ResourceTests {

	private static final Logger logger = LoggerFactory.getLogger(ResourceTests.class);

	@Test
	void testResourceSerialization() throws MalformedURLException {
		Resource r = new Resource();
		r.setMetadata(new MetaData());
		File tmp = new File("logs/library-test-2020-02-14.log");

		r.addFile(new LocalizedFile("de_DE", new File(tmp.getAbsoluteFile().toURI().toASCIIString())));
		Gson g = new GsonBuilder().setPrettyPrinting().create();
		String json = g.toJson(r);
		assertTrue(json != null);
		logger.debug("Json string is as follows:{}{}", System.lineSeparator(), json);
	}

}
