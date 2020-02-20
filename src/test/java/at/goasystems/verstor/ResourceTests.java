package at.goasystems.verstor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class ResourceTests {

	private static final Logger logger = LoggerFactory.getLogger(ResourceTests.class);

	@Test
	void testResourceSerialization() {

		String expected = "{\"resourceid\":\"\",\"metadata\":{\"resourcemimetype\":\"\",\"resourceextension\":\"\",\"originmimetype\":\"\",\"originextension\":\"\"},\"localizedfiles\":[{\"isocode\":\"de_DE\",\"file\":\".\"}]}";
		Resource r = generate();
		/*
		 * If Json should be formatted with new lines and indents add
		 * ".setPrettyPrinting()" to GsonBuilder.
		 */
		Gson g = new GsonBuilder().create();
		String json = g.toJson(r);
		assertTrue(json != null);
		assertEquals(json, expected);
		logger.debug("Json string is as follows:{}{}", System.lineSeparator(), json);
	}

	private Resource generate() {
		Resource r = new Resource();
		r.setMetadata(new MetaData());
		r.addFile(new LocalizedFile("de_DE", "."));
		return r;
	}

}
