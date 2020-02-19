package at.goasystems.verstor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class VerstorTest {

	private static final Logger logger = LoggerFactory.getLogger(VerstorTest.class);

	@Test
	public void testAddResource() {

		/* Create repository directory. */
		Verstor jge = new Verstor();
		Git git = jge.createRepository();
		String[] isocodes = { "de_DE", "en_US", "es_ES", "fr_FR", "it_IT", };
		Resource res1 = generate("res1", isocodes);
		Resource res2 = generate("res2", isocodes);
		jge.addResource(git, res1);
		jge.addResource(git, res2);
		assertTrue(new File(git.getRepository().getDirectory().getParent(), res1.getResourceid()).exists());
		assertTrue(new File(git.getRepository().getDirectory().getParent(), res2.getResourceid()).exists());

		/* Cleanup */
		cleanup(git);
	}

	@Test
	public void testRemoveResource() {

		/* Create repository directory. */
		Verstor jge = new Verstor();
		Git git = jge.createRepository();
		String[] isocodes = { "de_DE", "en_US", "es_ES", "fr_FR", "it_IT", };
		Resource res1 = generate("res1", isocodes);
		Resource res2 = generate("res2", isocodes);
		jge.addResource(git, res1);
		jge.addResource(git, res2);
		assertTrue(new File(git.getRepository().getDirectory().getParent(), res1.getResourceid()).exists());
		assertTrue(new File(git.getRepository().getDirectory().getParent(), res2.getResourceid()).exists());

		jge.removeResource(git, "res1");

		assertFalse(new File(git.getRepository().getDirectory().getParent(), res1.getResourceid()).exists());

		/* Cleanup */
		cleanup(git);
	}

	@Test
	@Disabled("Logic in development.")
	public void testExportFileFromCommit() {

		/* Create repository directory. */
		Verstor jge = new Verstor();
		Git git = jge.createRepository();
		String[] isocodes = { "de_DE", "en_US", "es_ES", "fr_FR", "it_IT", };
		Resource res1 = generate("res1", isocodes);
		Resource res2 = generate("res2", isocodes);
		jge.addResource(git, res1);
		jge.addResource(git, res2);
		assertTrue(new File(git.getRepository().getDirectory().getParent(), res1.getResourceid()).exists());
		assertTrue(new File(git.getRepository().getDirectory().getParent(), res2.getResourceid()).exists());

		jge.removeResource(git, "res1");

		assertFalse(new File(git.getRepository().getDirectory().getParent(), res1.getResourceid()).exists());

		jge.logDev(git);

		/* Cleanup */
		cleanup(git);
	}

	private void cleanup(Git git) {
		String syspropkeeprepos = System.getProperty("keepTestRepos");
		boolean keeprepos = false;
		if (syspropkeeprepos != null) {
			keeprepos = Boolean.parseBoolean(syspropkeeprepos);
		}
		if (!keeprepos) {
			try {
				FileUtils.deleteDirectory(git.getRepository().getDirectory().getParentFile());
			} catch (IOException e) {
				logger.error("Error", e);
			}
		}
	}

	private Resource generate(String resource, String[] languages) {
		Resource r = new Resource(resource);
		r.setMetadata(new MetaData("text/plain", ".txt", "", ""));
		for (String language : languages) {
			r.addFile(new LocalizedFile(language, "testdata/" + resource + "/" + language));
		}
		return r;
	}
}
