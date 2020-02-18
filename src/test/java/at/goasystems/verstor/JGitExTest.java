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
		Resource res1 = generate("res1");
		Resource res2 = generate("res2");
		jge.addResource(git, res1);
		jge.addResource(git, res2);
		assertTrue(new File(git.getRepository().getDirectory().getParent(), res1.getResourceid()).exists());
		assertTrue(new File(git.getRepository().getDirectory().getParent(), res2.getResourceid()).exists());

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

	private Resource generate(String resource) {
		Resource r = new Resource(resource);
		r.setMetadata(new MetaData("text/plain", "", ""));
		r.addFile(new LocalizedFile("de_DE", new File("testdata/" + resource + "/de_DE")));
		r.addFile(new LocalizedFile("en_US", new File("testdata/" + resource + "/en_US")));
		r.addFile(new LocalizedFile("es_ES", new File("testdata/" + resource + "/es_ES")));
		r.addFile(new LocalizedFile("fr_FR", new File("testdata/" + resource + "/fr_FR")));
		r.addFile(new LocalizedFile("it_IT", new File("testdata/" + resource + "/it_IT")));
		return r;
	}
}
