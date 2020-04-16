package at.goasystems.verstor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class VerstorTest {

	private static final Logger logger = LoggerFactory.getLogger(VerstorTest.class);

	@Test
	public void testAddResource() {

		/* Create repository directory. */
		Verstor jge = new Verstor();
		Git git = jge.createRepository(genTmpRepoDir());
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
		Git git = jge.createRepository(genTmpRepoDir());
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
	public void testCommits() {

		/* Create repository directory. */
		Verstor jge = new Verstor();
		Git git = jge.createRepository(genTmpRepoDir());
		String[] isocodes = { "de_DE", "en_US", "es_ES", "fr_FR", "it_IT", };
		Resource res1 = generate("res1", isocodes);
		Resource res2 = generate("res2", isocodes);
		jge.addResource(git, res1);
		jge.addResource(git, res2);

		assertTrue(new File(git.getRepository().getDirectory().getParent(), res1.getResourceid()).exists());
		assertTrue(new File(git.getRepository().getDirectory().getParent(), res2.getResourceid()).exists());

		try {
			res1.getLocalizedfiles().get(0).setUri(VerstorTest.class.getResource("/testdata/res1/de_DE.new").toURI());
		} catch (URISyntaxException e) {
			logger.error("Can't set resource.", e);
		}
		jge.addResource(git, res1);

		List<Commit> commits = jge.getCommits(git, "");
		assertTrue(commits.size() == 3);
		commits = jge.getCommits(git, "res1");
		assertTrue(commits.size() == 2);
		commits = jge.getCommits(git, "res2");
		assertTrue(commits.size() == 1);

		/* Cleanup */
		cleanup(git);
	}

	@Test
	public void testExportFileFromCommit() {

		/* Create repository directory. */
		Verstor jge = new Verstor();
		Git git = jge.createRepository(genTmpRepoDir());
		String[] isocodes = { "de_DE", "en_US", "es_ES", "fr_FR", "it_IT", };
		Resource res1 = generate("res1", isocodes);
		Resource res2 = generate("res2", isocodes);
		jge.addResource(git, res1);
		jge.addResource(git, res2);
		assertTrue(new File(git.getRepository().getDirectory().getParent(), res1.getResourceid()).exists());
		assertTrue(new File(git.getRepository().getDirectory().getParent(), res2.getResourceid()).exists());

		jge.removeResource(git, "res1");

		assertFalse(new File(git.getRepository().getDirectory().getParent(), res1.getResourceid()).exists());

		List<Commit> commits = jge.getCommits(git, "");

		assertTrue(commits.size() == 3);
		File exportfile = new File(System.getProperty("java.io.tmpdir"), "export.zip");
		try (FileOutputStream fos = new FileOutputStream(exportfile)) {
			jge.exportFileFromBranch(git, commits.get(1).getHash(), res1, fos);
		} catch (IOException e) {
			logger.error("Error writing to zip file.", e);
		}
		assertTrue(exportfile.exists());
		try {
			FileUtils.forceDelete(exportfile);
		} catch (IOException e) {
			logger.error("Error deleting file.", e);
		}
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
			try {
				r.addFile(new LocalizedFile(language,
						VerstorTest.class.getResource("/testdata/" + resource + "/" + language).toURI()));
			} catch (URISyntaxException e) {
				logger.error("Cant add file to resource.");
			}
		}
		return r;
	}

	/**
	 * Generates a temporary directory file object. This is only a virtual "File"
	 * object. The directory is not actually created on the disk. This has to be
	 * done by the consumers of this function.
	 * 
	 * @return
	 */
	private File genTmpRepoDir() {

		String temporaryId = Long.toString(new GregorianCalendar().getTimeInMillis());
		File directory = new File(System.getProperty("java.io.tmpdir"), temporaryId);
		return directory;
	}
}
