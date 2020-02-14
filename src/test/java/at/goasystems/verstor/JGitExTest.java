package at.goasystems.verstor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JGitExTest {

	private static final Logger logger = LoggerFactory.getLogger(JGitExTest.class);

	@Test
	public void testGitInitMaster() {

		/* Create repository directory. */
		JGitEx jge = new JGitEx();
		Git git = jge.createTestRepo();

		/* Assert the commits. */
		try {
			Iterable<RevCommit> log = git.log().call();
			String[] files = jge.getFilesForMaster();
			int i = files.length - 1;
			for (Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
				RevCommit rev = iterator.next();
				assertTrue(("File " + files[i] + " added.").compareTo(rev.getFullMessage()) == 0);
				i--;
			}
			git.close();
		} catch (IllegalStateException | GitAPIException e) {
			logger.error("Error", e);
		}

		/* Cleanup */
		cleanup(git);
	}

	@Test
	public void testGitInitBranched() {

		/* Create repository directory. */
		JGitEx jge = new JGitEx();
		Git git = jge.createTestRepo();
		git = jge.createBranchAndAddFiles(git, "branch1");

		/* Assert the commits. */
		try {

			Iterable<RevCommit> log = git.log().call();

			String[] files = jge.getFilesForMaster();

			int i = files.length - 1;
			for (Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
				RevCommit rev = iterator.next();
				assertTrue(("File " + files[i] + " added.").compareTo(rev.getFullMessage()) == 0);
				logger.debug(rev.getFullMessage());
				i--;
			}

			/* Change to created branch and test again. */
			git.checkout().setName("branch1").call();

			files = jge.getFilesForBranch();

			i = files.length - 1;
			for (Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
				RevCommit rev = iterator.next();
				assertTrue(("File " + files[i] + " added.").compareTo(rev.getFullMessage()) == 0);
				logger.debug(rev.getFullMessage());
				i--;
			}
			git.checkout().setName("master").call();
			git.close();
		} catch (IllegalStateException | GitAPIException e) {
			logger.error("Error", e);
		}

		/* Cleanup */
		cleanup(git);
	}

	@Test
	public void getGetFileFromBranch() {

		String targetbranch = "branch1";

		/* Create repository directory. */
		JGitEx jge = new JGitEx();
		Git git = jge.createTestRepo();
		git = jge.createBranchAndAddFiles(git, targetbranch);

		File branchfile1 = new File(System.getProperty("java.io.tmpdir"), "branch1file1.txt");
		jge.exportFileFromBranch(git, targetbranch, branchfile1);

		assertTrue(branchfile1.exists());
		assertTrue(branchfile1.length() == 24,
				String.format("Expecting: %d, Real length: %d", 24, branchfile1.length()));

		/* Cleanup */
		cleanup(git);
	}

	@Test
	public void testGitLog() {

		JGitEx jge = new JGitEx();
		Git git = jge.createTestRepo();
		String name = "";
		try {
			Iterable<RevCommit> log = git.log().call();

			RevCommit rev = log.iterator().next();
			while (log.iterator().hasNext()) {
				rev = log.iterator().next();
			}

			name = rev.getName();
			git.close();
		} catch (IllegalStateException | GitAPIException e) {
			logger.error("Error", e);
		}

		try {
			git.checkout().setCreateBranch(true).setName(jge.getNewUniqueId()).setStartPoint(name).call();
		} catch (GitAPIException e) {
			logger.error("Error", e);
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
}
