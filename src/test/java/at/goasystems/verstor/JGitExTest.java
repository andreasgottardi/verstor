package at.goasystems.verstor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JGitExTest {

	private static final Logger logger = LoggerFactory.getLogger(JGitExTest.class);

	@Test
	public void testAddFilesToMaster() {

		/* Create repository directory. */
		JGitEx jge = new JGitEx();
		Git git = jge.createRepository();
		Resource resource = generate();
		jge.addResource(git, resource);
		assertTrue(new File(git.getRepository().getDirectory().getParent(), resource.getResourceid()).exists());

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

	private Resource generate() {
		Resource r = new Resource("res1");
		r.setMetadata(new MetaData("text/plain", "", ""));
		r.addFile(new LocalizedFile("de_DE", new File("testdata/res1/de_DE")));
		r.addFile(new LocalizedFile("en_US", new File("testdata/res1/en_US")));
		r.addFile(new LocalizedFile("es_ES", new File("testdata/res1/es_ES")));
		r.addFile(new LocalizedFile("fr_FR", new File("testdata/res1/fr_FR")));
		r.addFile(new LocalizedFile("it_IT", new File("testdata/res1/it_IT")));
		return r;
	}
}
